package com.example.wings;

import android.os.AsyncTask;
import android.util.Log;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.example.wings.mainactivity.fragments.HomeFragment;
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

public class GetDirectionsTask extends AsyncTask<LatLng, Void, String> {
    private static final String TAG = "GetDirectionsTask";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/";

    private List<WingsRoute> result = null;



    @Override
    //s = the entire JSONObject result from the api call, but it's a string --> Parser task will
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        Log.d(TAG, "onPostExecute()");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject;
                List<WingsRoute> routes = null;

                try {
                    jsonObject = new JSONObject(json);        //Makes the String representing a JSONObject into an actual JSONObject
                    DataParser parser = new DataParser();           //DataParser does the actual parsing of the JSONObject
                    setResult(parser.parse(jsonObject));              //returns all possible routes from the JSONObject
                    Log.d(TAG, "onPostExecute() is done!");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    //Purpose:          called when you use "execute()", calls downloadURL to make the actual request to the Google Directions API, data = the entire JSON Object result
    protected String doInBackground(LatLng[] locations) {
        LatLng startLocation = locations[0];
        LatLng destination = locations[1];

        String apiUrl = getDirectionsURL(startLocation, destination);
        Log.d(TAG, "doInBackground(): apiUrl to use=" + apiUrl);
        String data = "";

        try {
            data = downloadURL(apiUrl);
        } catch (IOException e) {
            Log.d(TAG, "downloadURL() exception, error = " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return data;
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

    private void setResult(List<WingsRoute> newResult){
        result.addAll(newResult);
    }
    public List<WingsRoute> getResult(){
        return result;
    }
}


