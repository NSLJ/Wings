package com.example.wings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;


import static java.util.jar.Pack200.Unpacker.PROGRESS;

public class TestActivity extends AppCompatActivity {
    public static final String KEY = "key";
    public static final String OUTPUT_KEY = "output";
    public static final String TAG_SEND_DATA = "in TestActivity, sending data";
    private static final String TAG = "TestActivity";
    public static final String COUNTER = "counter";

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    LifecycleOwner owner = this;
    TextView textView;
    Button startBtn;
    Button stopBtn;

    int counter = 0;
    boolean keepLooping;
    //LocationManager locationManager;
    //static String latitudeStr, longitudeStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textView = findViewById(R.id.textView);
        startBtn = findViewById(R.id.button);
        stopBtn = findViewById(R.id.button3);


        //Begin all request stuff:
        stopAllRequests();
        //On click --> start the request
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked");

                //check if we have permissions, if not request it
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        !=PackageManager.PERMISSION_GRANTED){
                    //request permissions if don't have them
                    ActivityCompat.requestPermissions(
                            TestActivity.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                }
                else{
                    //startLocationService();
                    keepLooping = true;
                    setOneTimeWork();
                }
            }


                //Attempt for InfiniteSendWorker, it didn't work but for later if needed:
                /*OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(InfiniteSendWorker.class)
                        .build();

                WorkManager.getInstance(getApplicationContext())
                        .enqueueUniqueWork(
                        "request #1",
                        ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                        (OneTimeWorkRequest) request
                );

                WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                        .observe(owner, new Observer<WorkInfo>() {

                            //called every time WorkInfo is changed
                            public void onChanged(@Nullable WorkInfo workInfo) {
                                if (workInfo != null) {
                                    Data progress = workInfo.getProgress();
                                    int counter = progress.getInt(COUNTER, 0);
                                    String result = progress.getString(OUTPUT_KEY);

                                    textView.setText(result);
                                }
                            }
                        });*/
            //}

        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllRequests();
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                //startLocationService();
                keepLooping = true;
                setOneTimeWork();
            }
            else{
                Toast.makeText(this, "Permission was denied!", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void setCounter(int newCounter){
        counter = newCounter;
    }
    private void stopAllRequests(){
        keepLooping = false;
        WorkManager.getInstance(getApplicationContext()).cancelAllWork();       //probably will need to refine by Tags so specific requests get canceled
        textView.setText("All request stopping..");
    }

    /**
     * Purpose:     Makes a OneTimeWorkRequest infinitely
     */
    private void setOneTimeWork(){

        //Package data to send the counter
        Data data = new Data.Builder()
                .putInt(KEY, counter)       //send the counter
                .build();

        //Create the request
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SendDataWorker.class)
                .setInputData(data)         //send data
                .setInitialDelay(5, TimeUnit.SECONDS)      //wait 20 seconds before doing it
                .build();

        //Queue up the request
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(
                        "sendDataWorker request",
                        ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                        (OneTimeWorkRequest) request
                );

        //Listen to information from the request
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                .observe(owner, new Observer<WorkInfo>() {

                    //called every time WorkInfo is changed
                    public void onChanged(@Nullable WorkInfo workInfo) {

                        //If workInfo is there and it is succeeded --> update the text
                        if (workInfo != null) {
                            //Check if finished:
                           if(workInfo.getState().isFinished()){   //prob check if succeeded too
                                if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                                    Log.d(TAG, "Request succeeded ");
                                   //get output:
                                   Data output = workInfo.getOutputData();
                                   String result = output.getString(OUTPUT_KEY);
                                   int newCounter = output.getInt(COUNTER, 0);
                                   setCounter(newCounter);

                                   textView.setText(result);
                                   if(keepLooping) {
                                       setOneTimeWork();               //to infinitely do it!
                                   }
                               }
                                else {
                                    Log.d(TAG, "Request didn't succeed, status=" + workInfo.getState().name());
                                    setOneTimeWork();
                                }
                           }
                                    /*
                                    String status = workInfo.getState().name();
                                    incrementCounter();
                                    textView.setText(status + " #" + counter + "\n");*/
                        }
                    }
                });
    }

}