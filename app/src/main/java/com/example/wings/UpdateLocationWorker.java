package com.example.wings;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.concurrent.CountDownLatch;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class UpdateLocationWorker extends Worker {
    public static final String TAG = "UpdateLocationWorker";
    private static final String KEY_RESULTSTRING = "result_string";
    private static final String KEY_SENDCOUNTER = "worker_counter";
    private static final String KEY_GETCOUNTER = "activity_counter";

    private LocationCallback locationCallback = new LocationCallback() {
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("UpdateLocationWorker", "Location update: latitude = " + latitude + "  longitude = " + longitude);
                setResultMessage("latitude = " + latitude + "  longitude = " + longitude);
            }
        }
    };
    double longitude, latitude;
    String result;
    CountDownLatch latch = new CountDownLatch(1);               //to for all tasks to finish
    CountDownLatch waitForParse = new CountDownLatch(1);        // used for getCurrentLocation to wait for updateCur
    FusedLocationProviderClient flpClient;
    Context context;

    public UpdateLocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        flpClient = LocationServices.getFusedLocationProviderClient(context);

    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "in doWork(): Sending data to Server started!");
/*
        Data data = getInputData();
        int counter = data.getInt(KEY, 0);
        counter++;
        String message = "counter = " + counter;
        makeStatusNotification(message, getApplicationContext());

        //set output:
        Data output = new Data.Builder()
                .putString(OUTPUT_KEY, message)
                .putInt(COUNTER, counter)
                .build();
        return Result.success(output);*/

        result = "";

        //1.) Get the counter passed in
        Data data = getInputData();
        int counter = data.getInt(KEY_GETCOUNTER, 0);
        Log.d(TAG, "in doWork(): counter received = " + counter);


        //  getCurrentLocation();               //will update the "result" field
        //2.) Get the current location using the flpClient:
        doTask();

        try {
            Log.d(TAG, "doWork() is waiting for doTask()");
            latch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "doWork encountered error while trying latch.await(): e=" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        //just return failure if the getcurrentloction() returned null, will try again since the worker is infinitely called
        if (result.equals("null")) {
            Log.d(TAG, "in doWork(): result = null, returns Result.failure()");
            return Result.failure();
        }

        counter++;
        result += "\n Output #" + counter;

        Log.d(TAG, "in doWork(): After doTask() result= " + result);
        //set output:
        Data output = new Data.Builder()
                .putString(KEY_RESULTSTRING, result)
                .putInt(KEY_SENDCOUNTER, counter)
                .build();
        return Result.success(output);
    }


    //I'm keeping this so I can do Notifications later in the app since ik it works already!
    static void makeStatusNotification(String message, Context context) {

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "some name";
            String description = "some description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel("some id", name, importance);
            channel.setDescription(description);

            // Add the channel
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "some id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("some notification title")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[0]);

        // Show the notification
        NotificationManagerCompat.from(context).notify(1, builder.build());
    }

    @Override
    //Automatically called when worker is stopped for any reason  --> e.g. close files, etc
    public void onStopped() {
        super.onStopped();
    }


    public void doTask() {
        Log.d(TAG, "in doTask()");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission check failed!");
            return;
        }
        Task<Location> task = flpClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);
        task.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location currLocation = task.getResult();
                if (currLocation != null) {
                    double latitude = currLocation.getLatitude();
                    double longitude = currLocation.getLongitude();
                    Log.d(TAG, "doTask(): latitude = " + latitude + "  longitude = " + longitude);
                    Log.d(TAG, "doTask(): calling updateUserLocation now...");
                    updateUserLocation(latitude, longitude);

                    //This section will not execute until waitForParse counts down (counts down when updateUserLocation is done)
                    try {
                        waitForParse.await();
                        Log.d(TAG, "doTask() is waiting until updateUserLocation() is done...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    setResultMessage("latitude = " + latitude + "  longitude = " + longitude);          //will countdown the overall "latch"
                } else {
                    Log.d(TAG, "doTask(): currLocation == null");
                    setResultMessage("null");
                }
            }
        });
    }

    //-----------All location stuff:
    public void getCurrentLocation() {
        Log.d(TAG, "in getCurrentLocation()");
        LocationRequest locationRequest = new LocationRequest();

        //1.) Fill the request --> set high accuracy location, set update and fastest interval (from class constants)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);


        //2.) Start building a LocationSettingsRequest object to make send the LocationRequest:
     /*   LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        //3.) Get the SettingsClient object to interact with the settings --> can tell whether certain settings are on/off
        SettingsClient settingsClient = LocationServices.getSettingsClient(getApplicationContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);*/


        //4.) Obtain the FusedLocationProviderClient --> to interact with the location provider:
        // FusedLocationProviderClient FLPClient = getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission check failed!");
            return;
        }
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    public void setResultMessage(String newMessage){
        Log.d(TAG, "in setResultMessage(): newMessage = " + newMessage);
        result = newMessage;
        latch.countDown();
    }
    private void setLatitude(double newLat){
        latitude = newLat;
    }
    private void setLongitude(double newLong){
        longitude = newLong;
    }

    private void updateUserLocation(double latitude, double longitude){
        Log.d(TAG, "in updateUserLocation(): latitude = " + latitude + "        longitude = " + longitude);

        //Get the user's WingsGeoPoint and update it:
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null){
            Log.d(TAG, "updateUserLocation(): currentUser = " + currentUser.getString(User.KEY_FIRSTNAME));
            WingsGeoPoint currLocation = (WingsGeoPoint) currentUser.getParseObject(User.KEY_CURRENTLOCATION);

            //if its never been initialized:
            if(currLocation == null){
                Log.d(TAG, "updateUserLocation(): currLocation == null");
                currLocation = new WingsGeoPoint(currentUser, latitude, longitude);
                currentUser.put(User.KEY_CURRENTLOCATION, currLocation);
                currentUser.saveInBackground();
            }
            else {  //otherwise don't instantiate a new object and just update!
                Log.d(TAG, "updateUserLocation(): currLocation is NOT null");
                currLocation.put(WingsGeoPoint.KEY_LOCATION, new ParseGeoPoint(latitude, longitude));
                currLocation.put(WingsGeoPoint.KEY_LATITUDE, latitude);
                currLocation.put(WingsGeoPoint.KEY_LONGITUDE, longitude);
                currLocation.saveInBackground();
            }
        }
        else{
            Log.d(TAG, "updateUserLocation():   currentUser = null, return failure!");;
            //TODO: make the worker fail here
        }

        //Tell getCurrentLocation() that we are done updating the Parse database, so they can move on!
        waitForParse.countDown();
    }
}
