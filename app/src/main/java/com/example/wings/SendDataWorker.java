package com.example.wings;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wings.models.User;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class SendDataWorker extends Worker {
    public static final String TAG = "SendDataWorker";
    private int pin;

    public SendDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Sending data to Server started!");
        try {
            sendData();
        } catch (ParseException e) {
            Log.e(TAG, "Error with sendData(): e="+ e.getLocalizedMessage());
            return Result.retry();          //calls sendData() again
        }
        return Result.success();
    }

    public void sendData() throws ParseException {
        pin += 1;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.KEY_FIRSTNAME, "Josephine");
        List<ParseUser> users = query.find();
        ParseUser user = users.get(0);
        Log.d(TAG, "sendData():  user=" + user.getString(User.KEY_FIRSTNAME));

        user.logIn(user.getUsername(), "Jo123456");
        pin = user.getInt(User.KEY_PIN);
        user.put(User.KEY_PIN, pin+1);
        user.save();
    }


}
