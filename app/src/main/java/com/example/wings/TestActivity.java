package com.example.wings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Constraints;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

public class TestActivity extends AppCompatActivity {

    private static final String TAG_SEND_DATA = "in TestActivity, sending data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        SettingUpPeriodicWork();
    }
    private void SettingUpPeriodicWork() {
        // Create Network constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)      //device must be connected to internet
                .setRequiresBatteryNotLow(true)                     //device's battery can't be low, storage/memory can't be low
                .setRequiresStorageNotLow(true)
                .build();

        //Every minute, call the SendDataWorker class
        PeriodicWorkRequest periodicSendDataWork =
                new PeriodicWorkRequest.Builder(SendDataWorker.class, 1, TimeUnit.MINUTES)
                        .addTag(TAG_SEND_DATA)
                        .setConstraints(constraints)        //only do the request if these constraints are met
                        // setting a backoff on case the work needs to retry
                        //.setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(periodicSendDataWork);      //to start the request
    }

}