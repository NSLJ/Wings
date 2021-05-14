package com.example.wings;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.example.wings.models.WingsRoute;
import com.example.wings.network.WingsClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Headers;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class WingsMap {
    private static final String TAG = "WingsMap";
    private static final long UPDATE_INTERVAL = 5000;       //Can change later so other classes can change it
    private static final long FASTEST_INTERVAL = 3000;
    private static final String KEY_SEND_LOCATIONS = "wingsMap_locations";
    private static final String KEY_RESULT = "getRoutesWorker_result";

    private ParseUser currentUser;      //B/c we need to make tons of updates to the Parse
    private WingsClient client;
    private Context context;
    private LifecycleOwner lifecycleOwner;

    //Fields:
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private UiSettings mapUI;

    //For routing:
    private LatLng currentLocation;           //always on hand
    private LatLng startLocation;
    private LatLng destination;
    private boolean routeDrawn;
    private Polyline polylineDrawn;
    private List<WingsRoute> allRoutes;
    private boolean hasRoutes;           //bc maps may not always be used just for routing
    private WingsRoute chosenRoute;     //the route constantly tracking and animating

    //Drawing stuff
    private ArrayList<LatLng> markerPoints;
    private MarkerOptions startMarker;
    private MarkerOptions endMarker;

    public WingsMap(GoogleMap map, Context context, LifecycleOwner fragLifecycleOwner){
        this.map = map;
        this.context = context;
        lifecycleOwner = fragLifecycleOwner;

        currentUser = ParseUser.getCurrentUser();
        client = new WingsClient();
        chosenRoute = null;
        allRoutes = new ArrayList<>();
        hasRoutes = false;
        routeDrawn = false;

        openAppSetUp();
    }

    //Purpose:     The setup used when to automatically track and draw current location Set up all map's settings of interaction, look, zoom, etc. Just do things by default
    public void openAppSetUp(){
        // Get the saved current location from database, move map to it
        WingsGeoPoint initalLoc = (WingsGeoPoint) currentUser.getParseObject(User.KEY_CURRENTLOCATION);

        //If this is a new user --> there is no WingsGeoPoint listed
        if(initalLoc == null){
            initalLoc = new WingsGeoPoint(currentUser, 0, 0);
            currentUser.put(User.KEY_QUERIEDDESTINATION, initalLoc);
            currentUser.saveInBackground();
        }
        try {
            //Actually fetch the data:
            initalLoc.fetchIfNeeded();

            //initialize currentLocation:
            Log.d(TAG, "openAppSetUp(): initialLoc.latitide = " + initalLoc.getLatitude());
            currentLocation = new LatLng(initalLoc.getLatitude(), initalLoc.getLongitude());
            Log.d(TAG, "openAppSetUp(): location from parse: " + initalLoc.getLatitude() + " , " + initalLoc.getLongitude());

        } catch (ParseException parseException) {
            Log.d(TAG, "openAppSetUp(): could not fetch the current user's currentLocation field or mapDestination field");
            parseException.printStackTrace();
        }
        generalSetUp();
    }

    /*
        Possible map displays:
        1.) just the current location and nothing else (openAppSetup)
        2.) From the current location to a given destination:
                 - destination either from:
                    - already in Parse database (b/c was already mapping to it) --> LatLng
                    - or from the search bar --> Text --> conversion

        3.) From a given start location and destination + my current location
                - destination only ever from Parse database, will never need
     */

    //Purpose:          Automatically finds and draws the route from current location to the given destination, draws the first route
    public void routeFromCurrentLocation(LatLng destination){
        route(currentLocation, destination);
    }

    //Purpose:          Finds and chooses the first Address found from the given destination text. Routes from currentLocation, return the destination so HomeFragment can use it!
    public LatLng routeFromCurrentLocation(String destinationTxt){
        //Get possible addresses and choose the first one for now:
        LatLng foundDestination = null;

        List<Address> possibleAddresses = getPossibleAddresses(destinationTxt);
        if(possibleAddresses == null){
            Toast.makeText(context, "No addresses exist!", Toast.LENGTH_SHORT).show();
        }
        else {
            foundDestination = new LatLng(possibleAddresses.get(0).getLatitude(), possibleAddresses.get(0).getLongitude());

            //Because this is a new destination for the user --> save it into parse, and the text of destination to display in later fragments
            setUserQueriedDestination(foundDestination);
            setUserDestinationString(destinationTxt);

            //2.) Find all routes from current location to destination, choose first route to draw
            routeFromCurrentLocation(foundDestination);
        }
        return foundDestination;
    }

    //Purpose:      To use Geocoder to get the List<Address> that correlate to the given destination string
    public List<Address> getPossibleAddresses(String destination){
        List<Address> possibleAddresses = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context.getApplicationContext());

        //1.) Get all possible addresses from the geocoder:
        try {
            possibleAddresses = geocoder.getFromLocationName(destination, 10);
        } catch (IOException e) {
            Log.d(TAG, "getPossibleAddresses(): error with geocode to List<Address>, error=" + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return possibleAddresses;
    }

    //Purpose:          Automatically finds draws the first route from given start location and destination, initializes the allRoutes field
    public void route(LatLng startLocation, LatLng destination){
        Log.d(TAG, "route()");

        //1.) If there is already a route drawn, clear the map before re-drawing on it:
        if(routeDrawn){
            removeRouteDrawn();
            clearAllRoutes();
        }

        this.startLocation = startLocation;
        this.destination = destination;

        //Make the api call and handle the response --> if success --> use the DataParser class to parse all routes into WingsRoute objects
        client.makeDirectionRequest(startLocation, destination, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "route() - makeDirectionRequest() - success!");
                JSONObject jsonObject = json.jsonObject;

                DataParser parser = new DataParser();           //DataParser does the actual parsing of the JSONObject
                List<WingsRoute> result = parser.parse(jsonObject);
                setAllRoutes(result);                           //set allRoutes field = result
                drawRoute(0);                       //draw the first route
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "route(): making DirectionRequest failed, response=" + response);
            }
        });
    }

    //Purpose:      removes the route drawn, but technically route is still tracked
    public void removeRouteDrawn(){
        map.clear();
        routeDrawn = false;
    }

    //Purpose:      actually remove all instance of routes and destinations
    public void removeRoute(){
        clearAllRoutes();
        chosenRoute = null;
        startLocation = null;
        destination = null;
        removeRouteDrawn();
    }


    //All helper methods:

    //Purpose:      Automatically set up the map this way.
    private void generalSetUp(){
        // 1.) Permissions check:
        // SHOULD never have to worry about permissions as MainActivity does it for us, TODO:would be best to create method to call the MainActivity to ask for permissons again instead of assumming
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "openAppSetUp(): Permissions are not granted to do the map");
            return;
        }

        //2.) Enable the "my-location-layer" + all settings
        map.setMyLocationEnabled(true);

        //Set UI of google map:
        mapUI = map.getUiSettings();
        mapUI.setMyLocationButtonEnabled(true);
        mapUI.setZoomControlsEnabled(true);
        mapUI.setZoomGesturesEnabled(true);

        //3.) move map camera to current location --> may need to change later
        //map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
       // map.animateCamera(CameraUpdateFactory.zoomTo(12));      //12 = how much to zoom to, can make constant
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        //4.) Start constantly getting + displaying current location:
        startLocationUpdates();             //To constantly get current location and display update on the map
    }
    //Purpose:          Obtains the current location of the user to display on the map constantly, DOES NOT do anything with the Parse database. This is simply to display on the map ONLY!
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        //2.) Start building a LocationSettingsRequest object to make send the LocationRequest:
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        //3.) Get the SettingsClient object to interact with the settings --> can tell whether certain settings are on/off
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);

        //3a.) Check if our LocationRequest was fulfilled
        settingsClient.checkLocationSettings(locationSettingsRequest);

        //4.) Check if we have granted permissions for "ACCESS_FINE_LOCATION" and "ACCESS_COURSE_LOCATION", MainActivity should have done this already
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "startLocationUpdates(): Permissions are not granted to do the map");
            return;
        }

        // Obtain the FusedLocationProviderClient --> to interact with the location provider:
        // to request updates on the current location constantly on a Looper Thread
        getFusedLocationProviderClient(context).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    //When obtains the location update --> updates our Location field, "currentLocation"
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());        //our method
                    }
                },
                Looper.myLooper()
        );
    }
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());        //to change map display automatically
        }
    }


    //Purpose:      draws the route given the position of which route wanted in allRoutes. Assumes the allRoutes field is already initalized!
    private void drawRoute(int routePosition){
        Log.d(TAG, "drawRoute()");
        chosenRoute = allRoutes.get(routePosition);
        PolylineOptions lineOptions = chosenRoute.getLineOptions();

        //default settings to style polyline, can change later:
        lineOptions.width(8);
        lineOptions.color(Color.BLUE);
        lineOptions.geodesic(true);

        setMarker(destination);
        routeDrawn = true;
        polylineDrawn = map.addPolyline(lineOptions);

    }

    private void setMarker(LatLng destination){
        Log.d(TAG, "setMaker()");
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(destination);
        map.addMarker(markerOptions);
       // map.animateCamera(CameraUpdateFactory.newLatLng(destination));
      //  map.animateCamera(CameraUpdateFactory.zoomTo(9));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 12));
    }

    //All setters and getters
    public void setAllRoutes(List<WingsRoute> routes){
        Log.d(TAG, "setAllRoutes()");
        if(routes == null){
            Log.d(TAG, "setAllRoutes(): routes is null.");
            return;
        }
        Log.d(TAG, "setAllRoutes(): routes is not null");
        allRoutes.addAll(routes);
        hasRoutes = true;
    }
    public List<WingsRoute> getAllRoutes(){
        return allRoutes;
    }
    public void clearAllRoutes(){
        allRoutes.clear();
        hasRoutes = false;
    }
    public boolean hasRoutes(){
        return hasRoutes;
    }

    //Save the destination in Parse Database, technically should be doing this here
    private void setUserQueriedDestination(LatLng destination){
        Log.d(TAG, "setUserQueriedDestination()");

        WingsGeoPoint initialDestination = (WingsGeoPoint) currentUser.getParseObject(User.KEY_QUERIEDDESTINATION);
        //if the queriedDestination field is still the default value of (0, 0) --> create new object:
        //if its still the default value of (0,0):
        try {
            initialDestination.fetchIfNeeded();

            if(initialDestination.getLatitude() == 0 && initialDestination.getLongitude() == 0){
                Log.d(TAG, "updateUserLocation(): default destination");
                WingsGeoPoint currDestination = new WingsGeoPoint(currentUser, destination.latitude, destination.longitude);
                currentUser.put(User.KEY_QUERIEDDESTINATION, currDestination);
                currentUser.saveInBackground();
            }
            else {  //otherwise don't instantiate a new object and just update!
                Log.d(TAG, "setQueriedDestionation(): destination was not default value!");
                initialDestination.put(WingsGeoPoint.KEY_LOCATION, new ParseGeoPoint(destination.latitude, destination.longitude));
                initialDestination.put(WingsGeoPoint.KEY_LATITUDE, destination.latitude);
                initialDestination.put(WingsGeoPoint.KEY_LONGITUDE, destination.longitude);
                initialDestination.saveInBackground();
            }
        } catch (ParseException e) {
            Log.d(TAG, "setUserQueriedDestination(): error fetching initialDestination = " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Save the destination in Parse Database, technically should be doing this here
    private void setUserDestinationString(String destinationTxt){
        Log.d(TAG, "setUserDestinationString()");
        currentUser.put(User.KEY_DESTINATIONSTR, destinationTxt);
        currentUser.saveInBackground();
    }

    public LatLng getCurrentLocation(){
        return currentLocation;
    }


    //Saving just in case I need it still:
    /*public void getAllRoutes(LatLng startLocation, LatLng destination){
        //Package data to send the counter
        double[] locations = new double[]{startLocation.latitude, startLocation.longitude, destination.latitude, destination.longitude};
        Data data = new Data.Builder()
                .putDoubleArray(KEY_SEND_LOCATIONS, locations)       //send the counter
                .build();

        //Create the request
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UpdateLocationWorker.class)
                .setInputData(data)         //send data
                //.setInitialDelay(5, TimeUnit.SECONDS)      //wait 5 seconds before doing it         //TODO: don't hardcode, make this time frame a constant
                .build();

        //Queue up the request
        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "sendDataWorker request",
                        ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                        (OneTimeWorkRequest) request
                );

        //Listen to information from the request
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                .observe(lifecycleOwner, new Observer<WorkInfo>() {

                    //called every time WorkInfo is changed
                    public void onChanged(@Nullable WorkInfo workInfo) {

                        //If workInfo is there and it is succeeded --> update the text
                        if (workInfo != null) {
                            //Check if finished:
                            if(workInfo.getState().isFinished()){
                                if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                                    //we can use the allRoutes field now!
                                    drawRoute(0);       //just draw the first route for now
                                }
                                else {
                                    Log.d(TAG, "Request didn't succeed, status=" + workInfo.getState().name());
                                }
                            }
                        }
                    }
                });

    }

*/

}
