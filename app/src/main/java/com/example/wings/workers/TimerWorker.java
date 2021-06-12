package com.example.wings.workers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

//Purpose:      In order to time travels in the background --> notifies when timer is ober
public class TimerWorker extends Worker {
    private static String TAG = "TimerWorker";
    public static String KEY_TIME_WAIT_FOR = "how much time to wait for?";

    Context context;
    public TimerWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Data receivedData = getInputData();
        long timeToWait = receivedData.getLong(KEY_TIME_WAIT_FOR, -1);      //received in sec
        Log.d(TAG, "doWork(): timeToWait = " + timeToWait);

        if(timeToWait == -1){
            return Result.failure();
        }
        else {
            try {
                Thread.sleep(timeToWait*1000);          //*1000 to convert to ms
            } catch (InterruptedException e) {
                Log.e(TAG, "error waiting: time in ms = " + timeToWait*1000, e);
            }
            Log.d(TAG, "finished waiting!");
            return Result.success();
        }
    }
}
