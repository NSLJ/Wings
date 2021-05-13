package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wings.mainactivity.fragments.dialogs.ConfirmDestinationDialog;
import com.example.wings.R;
import com.example.wings.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.Buddy;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.ArrayList;
import java.util.List;


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
    private ExtendedFloatingActionButton fabChooseBuddy;

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
    //Purpose:      on confirmation --> current user accepts to be a buddy --> set User's isBuddy field to true, and instantiate being a Buddy
    public void onAccept() {
        Log.d(TAG, "confirmationDialogBox onAccept(): creating Buddy instance");
        //1.) Create a new Buddy instance and link it to this user:
        Buddy buddy = new Buddy(currUser, new WingsGeoPoint(currUser, destination.latitude, destination.longitude));
        buddy.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Log.d(TAG, "onAccept(): no Error saving Buddy!");
                }
                else{
                    Log.d(TAG, "onAccept(): no Error saving Buddy!");
                }
            }
        });
        currUser.put(User.KEY_ISBUDDY, true);       //update isBuddy = true and link the buddy object!
        currUser.put(User.KEY_BUDDY, buddy);

        //2.) Show the BuddyRequest FloatingActionButton in MainActivity, and the fabChooseBuddy button on HomeFrag!
        fabChooseBuddy.setVisibility(View.VISIBLE);         //navigates to ChooseBuddyFrag from HomeFrag
        listener.setBuddyRequestBttn(true);                 //navigates to PotentialBuddyFragment from any frag

        //3.) automatically go to the ChooseBuddyFrag!
        listener.toChooseBuddyFragment();
    }

    @Override
    //Clear the destination
    public void onReject() {
        Log.d(TAG, "confirmationDialogBox onReject(): erasing the queriedDestination");
        WingsGeoPoint currDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_QUERIEDDESTINATION);
        currDestination.setLatitude(0);
        currDestination.setLongitude(0);
        currDestination.setLocation(0,0);
        currUser.put(User.KEY_QUERIEDDESTINATION, currDestination);
        currDestination.saveInBackground();
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
        fabChooseBuddy = (ExtendedFloatingActionButton) view.findViewById(R.id.fabChooseBuddy);

        //1.) Check if the user currently needs a buddy --> check their isBuddy field --> if is a buddy do they hasBuddy?
        if(currUser.getBoolean(User.KEY_ISBUDDY)) {   //if they are a buddy
            Log.d(TAG, "onViewCreated(): current user IS a Buddy!");
            Buddy buddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
            try {
                buddyInstance.fetchIfNeeded();

                if (!buddyInstance.getHasBuddy()) {      //if they don't have a buddy but they want one, we need to show th button
                    Log.d(TAG, "onViewCreated(): This buddy is still looking for a buddy!");
                    fabChooseBuddy.setVisibility(View.VISIBLE);
                } else {
                    fabChooseBuddy.setVisibility(View.INVISIBLE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else {
            fabChooseBuddy.setVisibility(View.INVISIBLE);     //automatically invisible until the user wants to be a Buddy
        }


        //2.) Set on click listeners:
        // 2a.) fabChooseBuddy --> To go to choose buddy frag from homefrag!
        fabChooseBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.toChooseBuddyFragment();
            }
        });

        //2b.) btnSearch --> search for and route to a location
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.) Maps and draws route to the destination in the etSearchBar:
                searchArea();

                try {   //just for now bc DialogBox below will halt the thread, and we need to make sure seachArea() executes all the way first
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //2.) Show dialog ONLY if the address inputted in the bar was valid
                //Dialog asks to confirm the destination inputted to start becoming a buddy!
                List<Address> possibleAddresses = wingsMap.getPossibleAddresses(etSearchBar.getText().toString());
                Log.d(TAG, "btnSearch.onClick(): possibleAddressses = " + possibleAddresses.toString());
                if(possibleAddresses != null) {
                    makeConfirmDestinationDialog(etSearchBar.getText().toString());
                }

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
            Log.d(TAG, "loadMap(): initialDestination.latitide = " + initalDestination.getLatitude());

            //Check if there is already a destination we should be routing to + asking for destination confirmation  with Dialog
            if (initalDestination.getLatitude() != 0) {
                Log.d(TAG, "loadMap(): an inital destination exists! Routing to it...");
                destination = new LatLng(initalDestination.getLatitude(), initalDestination.getLongitude());
                wingsMap.routeFromCurrentLocation(destination);


                //if we are already a buddy --> no need to display a Dialog
                //else we are not a buddy, yet we are querying a location --> show dialog to ask if they want to confirm being a buddy:
                boolean isBuddy = currUser.getBoolean(User.KEY_ISBUDDY);
                if(!isBuddy){
                    //See if there is a destinationString we can display in the Display, if not, just use the LatLng
                    String checkDestinationStr = currUser.getString(User.KEY_DESTINATIONSTR);
                    Log.d(TAG, "loadMap(): checkDestinationStr=" +checkDestinationStr);
                    if(!checkDestinationStr.equals("default")){
                        makeConfirmDestinationDialog(checkDestinationStr);
                    }
                    else{
                        makeConfirmDestinationDialog(Double.toString(initalDestination.getLatitude()) + ", " + Double.toString(initalDestination.getLongitude()));
                    }
                }
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
        }
        else {
            wingsMap.routeFromCurrentLocation(destinationTxt);
        }
    }

    //Purpose:      Creates a ConfirmDestinationDialog with the given destination string to display
    public void makeConfirmDestinationDialog(String destinationTxt){
        ConfirmDestinationDialog confirmDestDialog = ConfirmDestinationDialog.newInstance(destinationTxt);
        confirmDestDialog.setTargetFragment(HomeFragment.this, 1);
        confirmDestDialog.show(getFragmentManager(), "ConfirmDestinationDialogTag");
    }
}