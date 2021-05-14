package com.example.wings;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.wings.models.WingsRoute;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GetRoutesWorker extends Worker {
    private static final String TAG = "GetRoutesWorker";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/";
    private static final String KEY_GET_LOCATIONS = "wingsMap_locations";
    private static final String KEY_RESULT = "getRoutesWorker_result";


    private List<WingsRoute> result = null;
    private WingsMap map;
    private CountDownLatch latch = new CountDownLatch(1);


    public GetRoutesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams, WingsMap map) {
        super(context, workerParams);
        this.map = map;
    }

    @NonNull
    @Override
    //Purpose:          Expect to the get the double[] location = startLocation.latitude, startLocation.longitude, destination.latitude, and destination.longitude in that order
    public Result doWork() {
        Log.d(TAG, "in doWork()");
        Data data = getInputData();
        double[] locations = data.getDoubleArray(KEY_GET_LOCATIONS);
        LatLng startLocation = new LatLng(locations[0], locations[1]);     //should probably error check the received locations
        LatLng destination = new LatLng(locations[2], locations[4]);

        //1.) Make the api call --> get back the JSONObject as a String for some reason:
        String jsonResult = getJsonResult(startLocation, destination);

        try {
            Log.d(TAG, "doWork() is waiting for api call to complete");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CountDownLatch secondLatch = new CountDownLatch(1);
        //2.) parse it
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);        //Makes the String representing a JSONObject into an actual JSONObject
            DataParser parser = new DataParser();           //DataParser does the actual parsing of the JSONObject
            result = parser.parse(jsonObject);              //returns all possible routes from the JSONObject
            secondLatch.countDown();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.d(TAG, "Waiting again to parse through everything...");
            secondLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //3.) just use the WingsMap setter method:
        map.setAllRoutes(result);
        return Result.success();
    }

    public String getJsonResult(LatLng startLocation, LatLng destination){
        Log.d(TAG, "in getJsonResult()");
        String apiUrl = getDirectionsURL(startLocation, destination);
        String data = "";

        try {
            data = downloadURL(apiUrl);
        } catch (IOException e) {
            Log.d(TAG, "downloadURL() exception, error = " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        latch.countDown();
        return data;
    }


    //Helper methods here:

    private String getDirectionsURL(LatLng startLocation, LatLng destination){
        Log.d(TAG, "in getDirectionsURL()");
        String startStr = "origin=" + startLocation.latitude + "," + startLocation.longitude;
        String endStr = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=walking";
        String parameters = startStr + "&" + endStr + "&" + mode;
        String output = "json";

        return BASE_URL+output + "?" + parameters + "&key=AIzaSyC1c3vYFZDb2Ebr1uZbtt-IjGNzARXgVho";
    }


    //Purpose:          Makes the network request to the Google Directions API given the correct URL, returns the entire JSONObject as a string
    private String downloadURL(String urlStr) throws IOException {
        String data = "";
        InputStream inStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inStream = urlConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuffer.append(line);
                line = bufferedReader.readLine();
            }

            data = stringBuffer.toString();
            bufferedReader.close();
        }catch(IOException e){
            Log.d(TAG, "in downloadURL(): exception=" + e.getLocalizedMessage());
        }
        finally {
            inStream.close();
            urlConnection.disconnect();
        }

        Log.d(TAG, "in downloadURL(): data=" + data);
        return data;
    }
}
