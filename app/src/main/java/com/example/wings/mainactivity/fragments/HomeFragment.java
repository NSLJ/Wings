package com.example.wings.mainactivity.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wings.ConfirmDestinationDialog;
import com.example.wings.DataParser;
import com.example.wings.R;
import com.example.wings.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.example.wings.models.WingsRoute;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseUser;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;


import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * HomeFragment.java
 * Purpose:         This displays the default screen of the app!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */

public class HomeFragment extends Fragment implements ConfirmDestinationDialog.ResultListener {
    private static final String TAG = "HomeFragment";
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 3000;

    ParseUser currUser = ParseUser.getCurrentUser();
    private MAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private FloatingActionButton fabEndRoute;

    Location currentLocation;
    private WingsMap wingsMap;

    private SupportMapFragment mapFragment;

    //Routing stuff:
    private LatLng startLocation;
    private LatLng destination;

    ArrayList<LatLng> markerPoints;
    MarkerOptions startMarker;
    MarkerOptions endMarker;

    List<LatLng> mapCoordinates;            //after we get the route, stores all intermediate coordinates (LatLngs) needed to get from startLocation to destination (froom decoding the polyline)
    //layout views:
    Button btnSearch;
    EditText etSearchBar;


    @Override
    public void onAccept() {
        listener.toChooseBuddyFragment();
    }

    @Override
    //Clear the destination
    public void onReject() {
        WingsGeoPoint currDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_QUERIEDDESTINATION);
        currDestination.setLatitude(0);
        currDestination.setLongitude(0);
        currDestination.setLocation(0,0);
        currUser.put(User.KEY_QUERIEDDESTINATION, currDestination);
        currDestination.saveInBackground();
        fabEndRoute.setVisibility(View.INVISIBLE);
    }

    public HomeFragment() {}    // Required empty public constructor

    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement MAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }

    @Override
    /**
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_home.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //1.) Initialize view:
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //2.) Initialize map fragment:
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        markerPoints = new ArrayList<>();

        //3.) Error check mapFragment
        if (mapFragment != null) {

            //3a.) getMapAsync() --> initializes maps system and view:
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    //when map is ready --> load it
                    loadMap(googleMap);                 //initializes everything on map!
                }
            });
        }
        else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
        return view;
    }


    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSearch = view.findViewById(R.id.btnSearch);
        etSearchBar = view.findViewById(R.id.etSearchBar);
        fabEndRoute = (FloatingActionButton) view.findViewById(R.id.fabEndRoute);

        fabEndRoute.setVisibility(View.INVISIBLE);        //TODO: Just for now bc there is no reason for it

        //To delete/cancel a route
        fabEndRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset destination to 0:
                WingsGeoPoint currDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_QUERIEDDESTINATION);
                currDestination.setLatitude(0);
                currDestination.setLongitude(0);
                currDestination.setLocation(0,0);
                currUser.put(User.KEY_QUERIEDDESTINATION, currDestination);
                currDestination.saveInBackground();
                fabEndRoute.setVisibility(View.INVISIBLE);
            }
        });

        //set listener: onClick() --> search for and route to a location
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchArea();
                //fabEndRoute.setVisibility(View.VISIBLE);    //TODO: only do this when user is for sure on a route and not just clicking the button many times


                //Testing creation of dialog:
                /*final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.confirm_destination_dialog);
                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(getContext(),"Dismissed..!!",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.show();*/
                /*try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //This code below will not call until the latch counts down all the way:
                ConfirmDestinationDialog confirmDestDialog = ConfirmDestinationDialog.newInstance(etSearchBar.getText().toString());
                confirmDestDialog.setTargetFragment(HomeFragment.this, 1);
                confirmDestDialog.show(getFragmentManager(), "ConfirmDestinationDialogTag");*/
            }
        });
    }

    //Purpose:          Initializes our "map" field, starts continuously checking for location updates
    protected void loadMap(GoogleMap map) {
        Log.d(TAG, "in loadMap():");

        wingsMap = new WingsMap(map, getContext(), getViewLifecycleOwner());   //automatically constantly shows current location

        //Check if there is a stored queriedDestination to map to:
        ParseUser currUser = ParseUser.getCurrentUser();
        WingsGeoPoint initalDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_QUERIEDDESTINATION);

        //If the user's "queriedDestination" field is not yet declared:
        if (initalDestination == null) {
            initalDestination = new WingsGeoPoint(currUser, 0, 0);
            currUser.put(User.KEY_QUERIEDDESTINATION, initalDestination);
            currUser.saveInBackground();
        }
        try {
            //Actually fetch the data:
            initalDestination.fetchIfNeeded();

            //Check if there is already a destination we should be routing to:
            if (initalDestination.getLatitude() != 0) {
                Log.d(TAG, "loadMap(): an inital destination exists! Routing to it...");
                destination = new LatLng(initalDestination.getLatitude(), initalDestination.getLongitude());
                wingsMap.routeFromCurrentLocation(destination);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    //Purpose:          Called when user clicks on the search button, Get the text in the Searchbar, use Geocoder to find the LatLng, set a marker at that LatLng, attempt to make a Network request to the Google Directions API
    private void searchArea() {
        Log.d(TAG, "in searchArea()");

        String destinationTxt = etSearchBar.getText().toString();
        if(destinationTxt.equals("")){
            Toast.makeText(getContext(), "You didn't enter anything!", Toast.LENGTH_SHORT).show();
            return;
        }
        wingsMap.routeFromCurrentLocation(destinationTxt);
    }
}