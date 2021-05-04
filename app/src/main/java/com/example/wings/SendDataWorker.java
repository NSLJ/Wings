package com.example.wings;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wings.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class SendDataWorker extends Worker{
    public static final String TAG = "SendDataWorker";
    private static final String OUTPUT_KEY = "output";
    private static final String COUNTER = "counter";
    private static final String KEY = "key";

    private LocationCallback locationCallback = new LocationCallback(){
        public void onLocationResult(LocationResult locationResult){
            super.onLocationResult(locationResult);
            if(locationResult != null && locationResult.getLastLocation() != null){
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("LocationService", "Location update: latitude = " + latitude + "  longitude = " + longitude);
            }
        }
    };

    //LocationManager locationManager;
    //String latitudeStr, longitudeStr;
    String result;
    CountDownLatch latch = new CountDownLatch(1);

    public SendDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Sending data to Server started!");
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

        //Get the counter passed in
        Data data = getInputData();
        int counter = data.getInt(KEY, 0);
        Log.d(TAG, "in doWork(): counter received = " + counter);
        counter++;

        getCurrentLocation();               //will update the "result" field
        try {
            Log.d(TAG, "doWork() is waiting for getCurrentLocation()");
            latch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "doWork encountered error while trying latch.await(): e=" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        result += "\n Output #" + counter;

        Log.d(TAG, "in doWork(): After getCurrentLocation result= " + result);
        //set output:
        Data output = new Data.Builder()
                .putString(OUTPUT_KEY, result)
                .putInt(COUNTER, counter)
                .build();
        return Result.success();
    }

    public void sendData() throws ParseException {
        /*ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.KEY_FIRSTNAME, "Josephine");
        List<ParseUser> users = query.find();
        ParseUser user = users.get(0);
        Log.d(TAG, "sendData():  user=" + user.getString(User.KEY_FIRSTNAME));

        user.logIn(user.getUsername(), "Jo123456");
        int pin = user.getInt(User.KEY_PIN);
        Log.d(TAG, "sendData():  pin=" + pin);

        int newPin = pin+1;
        user.put(User.KEY_PIN, newPin);
        user.save();*/
        Log.d(TAG, "in sendData()");
    }

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


    //-----------All location stuff:
    public void getCurrentLocation() {
        Log.d(TAG, "in getCurrentLocation()");
        LocationRequest locationRequest = new LocationRequest();

        //1.) Fill the request --> set high accuracy location, set update and fastest interval (from class constants)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //2.) Start building a LocationSettingsRequest object to make send the LocationRequest:
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        //3.) Get the SettingsClient object to interact with the settings --> can tell whether certain settings are on/off
        SettingsClient settingsClient = LocationServices.getSettingsClient(getApplicationContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);


        //4.) Obtain the FusedLocationProviderClient --> to interact with the location provider:
        FusedLocationProviderClient FLPClient = getFusedLocationProviderClient(getApplicationContext());


        //  Check if we have granted permissions for "ACCESS_FINE_LOCATION" and "ACCESS_COURSE_LOCATION":
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "in checkSelfPermission:  fine location permission = " + ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) + "               coarse permission = " + ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION));
            //ActivityCompat.requestPermissions(getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            //ContextCompat.
            return;
        }
        Looper.prepare();
        FLPClient.requestLocationUpdates(locationRequest, locationCallback /*new LocationCallback() {
            @Override
            //When obtains the location update --> updates our Location field, "currentLocation"
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "in onLocationResult(): ");
                Location currentLocation = locationResult.getLastLocation();        //our method
                String msg = "Updated Location: " +
                        Double.toString(currentLocation.getLatitude()) + "," +
                        Double.toString(currentLocation.getLongitude());
                Log.d(TAG, "in onLocationResult(): msg = " + msg);
                setResultMessage(msg);
            }
        }*/,
               null// Looper.myLooper()
        );
    }


    public void setResultMessage(String newMessage){
        Log.d(TAG, "in setResultMessage(): newMessage = " + newMessage);
        result = newMessage;
        latch.countDown();
    }
}
