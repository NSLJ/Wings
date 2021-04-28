package com.example.wings.mainactivity.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * HomeFragment.java
 * Purpose:         This displays the default screen of the app!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */
@RuntimePermissions     //required by PermissionsDispatcher
public class HomeFragment extends Fragment {

    private MAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private Button chooseBuddyBttn;

    Location currentLocation;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private LocationRequest locationRequest;            //Used for requests to FusedLocationClient (used to get location info)

    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private final static String KEY_LOCATION = "location";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public HomeFragment() {
    }    // Required empty public constructor

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

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            //Get the current location
            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        //2.) Initialize map fragment:
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        //3.) Error check mapFragment
        if (mapFragment != null) {

            //3a.) getMapAsync() --> initializes maps system and view:
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    //when map is ready --> load it
                    loadMap(googleMap);
                }
            });
        } else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
        return view;
    }


    //Purpose:          Initializes our "map" field, starts continuously checking for location updates
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;

        //1.) Call our getMyLocation() method with permissions
       // HomeFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);

        //2.) Call our startLocationUpdates() method with permissions
       // HomeFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);

        //3.) Set on click listener for the map:
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //When clicked on map --> initialize marker options:
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);         //Set position of marker:
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);    //Set title of marker:
                googleMap.clear();           //Remove all previous markers:

                //Animating to zoom the marker:
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                //add marker on map:
                googleMap.addMarker(markerOptions);
            }
        });
    }


    /**
     * Purpose:         to update our state with our current location
     * @param savedInstanceState
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, currentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
        //@NeedsPermission = required by PermissionsDispatcher when a method will use
        //"Manifest.permission.ACCESS_FINE_LOCATION"     = to access a precise location
    void getMyLocation() {
        //1.) Enable the "my-location layer" --> continuously draws the current location/bearing, displays UI controls for use
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);           //returns a UiSettings object = encapsulates all the settings of the GoogleMap that user has set

        //2.) Get a FusedLocationProviderClient --> to interact with the location provider:
        //"getFusedLocationProviderClient()" is inherited from LocationServices class (extends from all Objects)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());

        //3.) Get the best most recent location currently available + update the our Location field, "currentLocation"
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);        //our method
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        locationRequest = new LocationRequest();

        //1.) Fill the request --> set high accuracy location, set update and fastest interval (from class constants)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        //2.) Start building a LocationSettingsRequest object to make send the LocationRequest:
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        //3.) Get the SettingsClient object to interact with the settings --> can tell whether certain settings are on/off
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());

        //3a.) Check if our LocationRequest was fulfilled
        settingsClient.checkLocationSettings(locationSettingsRequest);


        //4.) Obtain the FusedLocationProviderClient --> to interact with the location provider:

        //  Check if we have granted permissions for "ACCESS_FINE_LOCATION" and "ACCESS_COURSE_LOCATION":
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Obtain the FusedLocationProviderClient --> to interact with the location provider:
        // to request updates on the current location constantly on a Looper Thread
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    //When obtains the location update --> updates our Location field, "currentLocation"
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());        //our method
                    }
                },
                Looper.myLooper()
        );


    }


    @Override
    //Required by PermissionsDispatcher, handles all the permission results
    //Basically just passes the info to the hanlder created for us by PermissionsDispatcher
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        HomeFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * Purpose:         updates our Location field, "mCurrentLocation", displays toast of update
     * @param location
     */
    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }

        // Report to the UI that the location was updated
        currentLocation = location;
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Changes the Fragment to the ChooseBuddyFragment via the MainActivity!

        chooseBuddyBttn = view.findViewById(R.id.registerBttn);
        chooseBuddyBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Display the connection status

        if (currentLocation != null) {
            Toast.makeText(getContext(), "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);            //animates the movement of the camera to the position in this Camera Update
        } else {
            Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        HomeFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
    }
}