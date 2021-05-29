package com.example.wings.network;

import android.os.AsyncTask;
import android.util.Log;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * Purpose:                 Makes the Directions API network request given the needed parameters!
 *
 * Used by:         WingsMap.java (in model.helpers package)
 */
public class WingsClient {
    private static final String TAG = "WingsClient";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/";

    private AsyncHttpClient client;
    JSONObject result;


    public WingsClient(){
        client = new AsyncHttpClient();
    }

    public void makeDirectionRequest(LatLng startLocation, LatLng destination, JsonHttpResponseHandler handler){
        String apiUrl = getDirectionsURL(startLocation, destination);
        Log.d(TAG, "in makeDirectionRequest(): apiURL=" + apiUrl);

        //The handler automatically handles the response of the api call, e.g. onSuccess() or fail...
        client.get(apiUrl, handler);
    }

    private String getDirectionsURL(LatLng startLocation, LatLng destination){
        Log.d(TAG, "in getDirectionsURL()");
        String startStr = "origin=" + startLocation.latitude + "," + startLocation.longitude;
        String endStr = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=walking";
        String parameters = startStr + "&" + endStr + "&" + mode;
        String output = "json";

        return BASE_URL+output + "?" + parameters + "&key=AIzaSyC1c3vYFZDb2Ebr1uZbtt-IjGNzARXgVho";
    }


    public JSONObject testDirectionRequest(LatLng startLocation, LatLng destination){
        Log.d(TAG, "in testDirectionRequest()");
        //DownloadTask downloadTask = new DownloadTask();         //postexecute() will call ParserTask automatically
        CountDownLatch latch = new CountDownLatch(1);

        String apiUrl = getDirectionsURL(startLocation, destination);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject result = downloadURL(apiUrl);
                    setResult(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Log.d(TAG, "testDirectionRequest() is waiting...");
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void setResult(JSONObject newResult){
        result = newResult;
    }

    //Purpose:          Makes the network request to the Google Directions API given the correct URL, returns the entire JSONObject as a string
    private JSONObject downloadURL(String urlStr) throws IOException {
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
        JSONObject jsonObject = null;
        Log.d(TAG, "in downloadURL(): data=" + data);
        try {
            jsonObject = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
