package com.example.wings;

//Purpose:      To continuously listen to all BuddyRequests --> notify when someone has accepted, notify when you have received

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wings.mainactivity.MainActivity;
import com.example.wings.models.Buddy;
import com.example.wings.models.BuddyRequest;
import com.example.wings.models.User;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class HandleBuddyRequestsWorker extends Worker {
    private static final String TAG = "HandlerBuddyRequestsWorker";
    private static final String KEY_INITIAL_COUNT = "initialSizeReceivedRequests";  //this is to
    private static final String KEY_CHANGED = "receivedRequestsChanged?";
    private static final String KEY_NUM_RECEIVED = "responseTotalReceived";
    private static final String KEY_APPROVED_REQUEST = "whichSentRequest?";
    private static final String KEY_ISAPPROVED = "anyRequestsApproved?";

    private CountDownLatch latch = new CountDownLatch(1);
    private Context context;
    private ParseUser currentUser;
    private Buddy currentBuddyInstance;

    public HandleBuddyRequestsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

       /* currentUser = ParseUser.getCurrentUser();
        currentBuddyInstance = (Buddy) currentUser.getParseObject(User.KEY_BUDDY);
        try {
            currentBuddyInstance.fetchIfNeeded();
        } catch (ParseException e) {
            Log.d(TAG, "in constructor: currentBuddyInstance couldn't be fetched");
            e.printStackTrace();
        }
        latch.countDown();*/
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "in doWork()");

        //1.) Get the size of received requests
        Data data = getInputData();
        int initialNumRequests = data.getInt(KEY_INITIAL_COUNT, -2);
        Log.d(TAG, "in doWork(): initialNumReq="+initialNumRequests);

        try {
            currentUser = ParseUser.getCurrentUser();
            currentUser.fetchIfNeeded();
            currentBuddyInstance = (Buddy) currentUser.getParseObject(User.KEY_BUDDY);
            currentBuddyInstance.fetchIfNeeded();
            latch.countDown();
        } catch (ParseException e) {
            Log.d(TAG, "in constructor: currentBuddyInstance couldn't be fetched");
            e.printStackTrace();
        }


        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<BuddyRequest> receivedRequests = currentBuddyInstance.getList(Buddy.KEY_RECIEVEDREQUESTS);
        Log.d(TAG, "in doWork() receivedRequests=" + receivedRequests.toString());
        int numReceived = receivedRequests.size();
        Log.d(TAG, "in doWork() numReceived=" + numReceived);
        boolean numRequestsChanged = false;

        if(numReceived != initialNumRequests && initialNumRequests > -1){
            numRequestsChanged = true;
        }


        //2.) Get all sentRequests and check if any of them are approved:
        List<BuddyRequest> sentRequests = currentBuddyInstance.getSentRequests();
        Log.d(TAG, "in doWork() sentRequests=" + sentRequests.toString());
        boolean isApproved = false;
        int approvedRequestIndex = -1;
        for(int i = 0; i < sentRequests.size(); i++){
            BuddyRequest currSentRequest = sentRequests.get(i);
            try {
                currSentRequest.fetchIfNeeded();

                if(currSentRequest.getIsConfirmed()){
                    Log.d(TAG, "in doWork() in isConfirmed");

                    isApproved = true;
                   approvedRequestIndex = i;
                    break;
                }
            } catch (ParseException e) {
                Log.d(TAG, "error fetching BuddyRequest");
                e.printStackTrace();
            }

        }

        Data output =  new Data.Builder()
                .putBoolean(KEY_CHANGED, numRequestsChanged)
                .putInt(KEY_NUM_RECEIVED, numReceived)
                .putBoolean(KEY_ISAPPROVED, isApproved)
                .putInt(KEY_APPROVED_REQUEST, 0/*approvedRequestIndex*/ )
                .build();

        return Result.success(output);

    }
}
