package com.example.wings.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wings.mainactivity.fragments.home.BuddyHomeFragment;

public class CheckProximityWorker extends Worker {
    private static final String TAG = "CheckProximityWorker";
    private static final String KEY_PROXIMITY_RESULT = "closeEnough?";

    public CheckProximityWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork()");
        boolean result = BuddyHomeFragment.checkNearEnough();
        Log.d(TAG, "doWork(): result = " + result);
        Data output = new Data.Builder().putBoolean(KEY_PROXIMITY_RESULT, result).build();
        return Result.success(output);
    }
}
