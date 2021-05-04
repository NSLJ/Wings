package com.example.wings.startactivity;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wings.R;
import com.example.wings.SendDataWorker;
import com.example.wings.TestActivity;

import java.util.concurrent.TimeUnit;

public class InfiniteSendWorker extends Worker {
    public final String KEY_CONTEXT = "context";
    public final String KEY_TEXTVIEW = "tView";
    private static final String TAG = "InfiniteSendWorker";

    private int counter;

    public InfiniteSendWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        counter = 0;
    }

    @NonNull
    @Override
    public Result doWork() {

        return null;
    }

    //To continuously set SendDataWorker as a OneTimeRequest
    private void setOneTimeWork() {
        //1.) Package the "counter" to send into the request
        Data data = new Data.Builder()
                .putInt(TestActivity.KEY, counter)       //send the counter
                .build();

        //2.) Create the request, send in the Data object, have the request wait 5 sec before executing
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SendDataWorker.class)
                .setInputData(data)         //send data
                .setInitialDelay(5, TimeUnit.SECONDS)      //wait 20 seconds before doing it
                .build();

        //3.) Queue the request and start it
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(
                "SendDataWorker request from InfiniteSendWorker",
                ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                (OneTimeWorkRequest) request);



        //4.) Get the results from the request once it's succeeded:
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                .observe(ProcessLifecycleOwner.get(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {

                        //Check if finished:
                        if (workInfo.getState().isFinished()) {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {   //prob check if succeeded too
                                Log.d(TAG, "onChanged() in InfiniteSendWorker");
                                //get output:
                                Data output = workInfo.getOutputData();
                                String desc = output.getString(TestActivity.OUTPUT_KEY);
                                int newCounter = output.getInt(TestActivity.COUNTER, 0);
                                setCounter(newCounter);

                                //4.) Send Progress to the Activity so Activity can update the UI:
                                Data dataToActivity = new Data.Builder()
                                        .putInt(TestActivity.COUNTER, newCounter)
                                        .putString(TestActivity.OUTPUT_KEY, desc)
                                        .build();

                                setProgressAsync(dataToActivity);

                                //5.) Keep doing it!
                                setOneTimeWork();               //to infinitely do it!
                            }
                        }
                        //if it wasn't succeeded but still finished --> do it again
                        else {
                            setOneTimeWork();
                        }
                    }
                });



    }

    private void setCounter(int newCounter) {
        counter = newCounter;
    }

}
