package com.example.wings.models.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.home.BuddyHomeFragment;
import com.example.wings.mainactivity.fragments.home.ConfirmBuddyHomeFragment;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.example.wings.network.DataParser;
import com.example.wings.network.WingsClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Headers;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Purpose:             Model class that aids GREATLY in creating a GoogleMap, displaying it on the Context, and invoking any necessary functions corresponding to it. E.g. routing, drawing routes, updating Parse datavase
 *                      Utilizes WingsRoute to encapsulate all info relevant to a drawn route.
 *
 * Classes associated w/:       WingsRoute.java, WingsClient.java, DataParser.java, WingsGeoPoint.java
 *
 */
public class WingsMap {
    private static final String TAG = "WingsMap";
    private static final long UPDATE_INTERVAL = 5000;       //Can change later so other classes can change it
    private static final long FASTEST_INTERVAL = 3000;

    private MAFragmentsListener listener;

    private ParseUser currentUser;      //B/c we need to make tons of updates to the Parse
    private WingsClient client;
    private Context context;
    private LifecycleOwner lifecycleOwner;

    //Fields:
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
    private LatLng clickedTargetDestination = new LatLng(0,0);

    //Drawing stuff
    private Marker targetDestinationMarker;

    private double distanceFromCurLocation;
    public boolean isReady;
    private boolean trackOtherUser;
    private long currentEst = -1;
    private boolean isTimed = false;            //toggled when onMeetup or onBuddyTrip

    public WingsMap(GoogleMap map, Context context, LifecycleOwner fragLifecycleOwner, boolean isConfirmBuddyFrag, boolean trackOtherUser){
        this.map = map;
        this.context = context;
        lifecycleOwner = fragLifecycleOwner;

        currentUser = ParseUser.getCurrentUser();
        client = new WingsClient();
        chosenRoute = null;
        allRoutes = new ArrayList<>();
        hasRoutes = false;
        routeDrawn = false;
        this.trackOtherUser = trackOtherUser;                   //used specifically when mode = onTrip from BuddyHomeFrag
        isReady = true;
        openAppSetUp();

        if(isConfirmBuddyFrag) {            //only need to click on map if sending a request
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng clickedLocation) {
                    //Remove the marker if its already on the map, otherwise show the current marker
                    if (targetDestinationMarker == null) {
                        initializeTargetDestinationMarker(clickedLocation);
                    } else {
                        targetDestinationMarker.remove();
                        initializeTargetDestinationMarker(clickedLocation);
                    }

                    //if (isConfirmBuddyFrag) {
                        //update the overlay in confirmBuddyHomeFragment
                        ConfirmBuddyHomeFragment.enableSendOverlay(clickedLocation);
                  //  }
                }
            });
        }
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

    //Purpose:          Automatically finds and draws the route from current location to the given destination, draws the first route. animateMap = whether or not to move the map once done drawing route. animateMap = whether or not to move the map once done drawing route
    public void routeFromCurrentLocation(LatLng destination, boolean animateMap, String title){
        Log.d(TAG, "routeFromCurrentLocation");
        isReady = false;
        ParseGeoPoint curLoc = new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude);
        distanceFromCurLocation = curLoc.distanceInKilometersTo(new ParseGeoPoint(destination.latitude, destination.longitude))*1000;
        route(currentLocation, destination, animateMap, title);
    }

    //Purpose:          Finds and chooses the first Address found from the given destination text. Routes from currentLocation, return the destination so HomeFragment can use it! animateMap = whether or not to move the map once done drawing route
    public LatLng routeFromCurrentLocation(String destinationTxt, boolean animateMap, String title){
        //Get possible addresses and choose the first one for now:
        LatLng foundDestination = null;

        List<Address> possibleAddresses = getPossibleAddresses(destinationTxt);
        if(possibleAddresses == null || possibleAddresses.size() == 0){
            Toast.makeText(context, "No addresses exist!", Toast.LENGTH_SHORT).show();
        }
        else {
            foundDestination = new LatLng(possibleAddresses.get(0).getLatitude(), possibleAddresses.get(0).getLongitude());

            //Because this is a new destination for the user --> save it into parse, and the text of destination to display in later fragments
            setUserQueriedDestination(foundDestination);
            setUserDestinationString(destinationTxt);

            //2.) Find all routes from current location to destination, choose first route to draw
            routeFromCurrentLocation(foundDestination, animateMap, title);
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

    //Purpose:          Automatically finds draws the first route from given start location and destination, initializes the allRoutes field. animateMap = whether or not to move the map once done drawing route
    public void route(LatLng startLocation, LatLng destination, boolean animateMap, String title){
        Log.d(TAG, "route(): startLocation given = " + startLocation.toString() + "  destination given:  " + destination.toString());

        if(startLocation.latitude != 0 && startLocation.longitude != 0 && destination.latitude != 0 && destination.longitude != 0) {
            //1.) If there is already a route drawn, clear the map before re-drawing on it:
            if (routeDrawn) {
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
                    if (result == null || result.size() == 0) {
                        Log.d(TAG, "route(): result = null or size = 0");
                        Toast.makeText(context, "No directions available!", Toast.LENGTH_SHORT).show();
                    } else {
                        setAllRoutes(result);                           //set allRoutes field = result
                        drawRoute(0, animateMap, title);                       //draw the first route


                        //Are we keeping track of this route? e.g. onMeetup or onBuddyTrip?
                        if(isTimed){
                            long thisEta = result.get(0).getEst();

                            //Do we need to change/initialize the currentEst?
                            if (currentEst == -1 || thisEta > currentEst) {              //if this is the first time we are using it or if the new route --> a higher est than the old one
                                Log.d("jo", "WingsMap: currentEst = " + thisEta);
                                if(currentEst == -1){
                                    setCurrentEst(thisEta);
                                    BuddyHomeFragment.etaInitialized(thisEta);
                                }
                                else{       //(thisEta > currentEst) {
                                    //Then we changed the previous Eta --> clear the timer + restart with new eta!
                                    setCurrentEst(thisEta);
                                    BuddyHomeFragment.etaChanged(thisEta);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.d(TAG, "route(): making DirectionRequest failed, response=" + response);
                }
            });
        }
        else{
            Log.d(TAG, "route():    startLocation or destination were 0");
        }
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
        distanceFromCurLocation = 123456789;
        removeRouteDrawn();
    }


    //All helper methods:


    public boolean isNearEnough(int meters){
        return (distanceFromCurLocation < meters);
    }


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
                        onLocationUpdated(locationResult.getLastLocation());        //our method
                    }
                },
                Looper.myLooper()
        );
    }
    public void onLocationUpdated(@NonNull Location location) {
        if (location != null) {
            LatLng receivedLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "onLocationUpdated():  receivedLocation = " + receivedLocation.toString());

            //If we are not in the same spot, there is a destination, and we ARE routing from our current location --> re-route
            if((!currentLocation.equals(receivedLocation)) && destination != null && (destination.latitude != 0 && destination.longitude != 0) && startLocation.equals(currentLocation)){
                Log.d(TAG, "onLocationUpdated(): currentLocation != to location updated --> re-drawing route");
                currentLocation = receivedLocation;
                ParseGeoPoint curLoc = new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude);
                distanceFromCurLocation = curLoc.distanceInKilometersTo(new ParseGeoPoint(destination.latitude, destination.longitude))*1000;
                Log.d(TAG, "onLocationUpdated(): distanceFromCurrLocation changed = " + distanceFromCurLocation);
                Log.d(TAG, "onLocationUpdated(): currLocation = " + currentLocation.toString() + "    destination=" + destination.toString());
                route(receivedLocation, destination, false, "");
            }
            //currentLocation = receivedLocation;     //to change map display automatically
        }
    }


    //Purpose:      draws the route given the position of which route wanted in allRoutes. Assumes the allRoutes field is already initalized!
    private void drawRoute(int routePosition, boolean animateMap, String title){
        Log.d(TAG, "drawRoute()");
        chosenRoute = allRoutes.get(routePosition);
        PolylineOptions lineOptions = chosenRoute.getLineOptions();

        //default settings to style polyline, can change later:
        lineOptions.width(8);
        lineOptions.color(0xFF6EB2B7);        //color the route our logo color

        lineOptions.geodesic(true);

        if(!startLocation.equals(currentLocation)) {            //Did we map the route from currUser's current location? if not --> this must be the otherUser's destination
            setMarker(destination, BitmapDescriptorFactory.HUE_RED, animateMap, title + ":  (" + Math.round(destination.latitude * 1000.0) / 1000.0 + ", " + Math.round(destination.longitude * 1000.0) / 1000.0 + ")");
        }
        else{
            setMarker(destination, BitmapDescriptorFactory.HUE_RED, animateMap, title+ ":  (" + Math.round(destination.latitude * 1000.0) / 1000.0 + ", " + Math.round(destination.longitude * 1000.0) / 1000.0 + ")");
        }

        if(trackOtherUser){             //if wingsMap instance was called by a mode=onTrip BuddyHomeFrag --> must also show marker for otherUser's currentLocation
            BuddyHomeFragment.setOtherUserLocationMarker();
        }
        routeDrawn = true;
        polylineDrawn = map.addPolyline(lineOptions);
        isReady = true;
    }

    public boolean getIsReady(){
        return isReady;
    }
    public void setMarker(LatLng destination, float color, boolean animateMap, String title){
        Log.d(TAG, "setMarker()");
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(destination);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
        markerOptions.title(title);
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow();
       // map.animateCamera(CameraUpdateFactory.newLatLng(destination));
      //  map.animateCamera(CameraUpdateFactory.zoomTo(9));
        if(animateMap) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 12));
        }
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
    public void setIsTimed(boolean answer){
        isTimed = answer;
        if(!answer){                        //if we aren't timing this route anymore --> reset the currentEst
            currentEst = -1;
        }
    }
    public void setMAFragmentListener(MAFragmentsListener listener){
        this.listener = listener;
    }
    public void setCurrentEst(long newEst){
        currentEst = newEst;
    }
    public long getCurrentEst(){
        Log.d(TAG, "getCurrentEst(): currentEst = " + currentEst);
        return currentEst;
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

    private void initializeTargetDestinationMarker(LatLng clickedLocation){
        //Create a stylized marker, able to be dragged--> set clickedTargetDestination
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(clickedLocation);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.title("("+clickedLocation.latitude + ", " + clickedLocation.longitude + ")");
        markerOptions.draggable(true);
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.showInfoWindow();
                setClickedTargetDestination(marker.getPosition());
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.remove();
                setClickedTargetDestination(new LatLng(0, 0));
                return false;
            }
        });
        setClickedTargetDestination(clickedLocation);
        targetDestinationMarker = map.addMarker(markerOptions);
        targetDestinationMarker.showInfoWindow();
        Log.d(TAG, "initalizeTargetDestinationMarker(): targetDestinationMarker.isDraggable()= " + targetDestinationMarker.isDraggable());
    }
    //Save the destination in Parse Database, technically should be doing this here
    private void setUserDestinationString(String destinationTxt){
        Log.d(TAG, "setUserDestinationString()");
        currentUser.put(User.KEY_DESTINATIONSTR, destinationTxt);
        currentUser.saveInBackground();
    }
    public void setClickedTargetDestination(LatLng location){
        clickedTargetDestination = location;
    }
    public LatLng getClickedTargetDestination(){
        return clickedTargetDestination;
    }
    public LatLng getCurrentLocation(){
        return currentLocation;
    }
    public double getDistanceFromCurLocation(){
        return distanceFromCurLocation;
    }
    public void setDestination(LatLng newDestination){
        destination = newDestination;
    }
}
