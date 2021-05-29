package com.example.wings.mainactivity.fragments.home;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.models.helpers.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.List;


import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * HomeFragment.java
 * Purpose:         This displays the home screen of the app! Depending on who (what Context) called this, the layout and functionality will be different!
 *
 *
 */

public class HomeFragment extends Fragment{
    private static final String TAG = "HomeFragment";
    private static final String KEY_MODE = "whatMode?";
    public static final String KEY_BASIC = "basicMode";         //not on a trip mode -> able to search for destination, for buddies, e.g. everything before a confirmation of buddy
    public static final String KEY_ONTRIP = "tripMode";         //everything after confirmation of buddy


    ParseUser currUser = ParseUser.getCurrentUser();
    private String mode;
    private MAFragmentsListener listener;

    //Fields to track if in "tripMode":
    Buddy userBuddyInstance;
    BuddyMeetUp meetUpInstance;
    BuddyTrip buddyTrip;
    ParseUser otherUser;

    //For displaying map
    private WingsMap wingsMap;
    private SupportMapFragment mapFragment;
    private LatLng destination;

    //Views:
    private Button btnSearch;
    private EditText etSearchBar;
    private ExtendedFloatingActionButton fabChooseBuddy;
    private ExtendedFloatingActionButton fabCancelBuddy;
    private RelativeLayout chooseDestinationOverlay;

    private View mainView;

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
     * Purpose:     Ensure to initialize the "mode" field once this Fragment is created. mode = whether the fragment should function as if user is currently on a buddy trip, or is looking to be on one (default)
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            mode = getArguments().getString(KEY_MODE);
            Log.d(TAG, "onCreate():  mode =" + mode);
        }
    }
    @Override
    /**
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_home.xml"--> ensure to display and setup the map
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //1.) Figure out which layout needed:
        if(mode.equals(KEY_BASIC)){
            mainView = inflater.inflate(R.layout.fragment_default_home, container, false);
            setMapFragment();
        }

        return mainView;
    }

    //Purpose:      Used to toggle between layouts while user is in HomeFragment
    private void setViewLayout(int id){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(id, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(mainView);
    }

    private void setMapFragment(){
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        //Error check mapFragment
        if (mapFragment != null) {
            // getMapAsync() --> initializes maps system and view:
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if(mode != null) {
                        //when map is ready --> load it
                        if (mode.equals(KEY_BASIC)) {
                            basicLoadMap(googleMap);                 //= looking for a buddy
                        }
                        else{
                            tripLoadMap(googleMap);                 /// = already has a buddy and on a trip
                        }
                    }
                    else{
                        Log.d(TAG, "onCreateView() mode is null!");
                    }
                }
            });
        }
        else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //1.) Connect Views:
        btnSearch = view.findViewById(R.id.btnSearch);
        etSearchBar = view.findViewById(R.id.etSearchBar);
        fabChooseBuddy = (ExtendedFloatingActionButton) view.findViewById(R.id.fabChooseBuddy);
        fabCancelBuddy = view.findViewById(R.id.fabCancelBuddy);

        chooseDestinationOverlay = view.findViewById(R.id.chooseDestinationOverlay);
        chooseDestinationOverlay.setVisibility(View.INVISIBLE);
        //2b.) fabCancelBuddy --> get rid of all stuff that isn't just User stuff --> cancels everything
        fabCancelBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals(KEY_ONTRIP)) {      //if mode = onTrip then reset the buddy associated with automatically
                    Log.d(TAG, "resetting other user: " + otherUser.get(User.KEY_FIRSTNAME));
                    resetUser(otherUser);     //TODO: Really important! Find way to erase buddy request for the other buddy! BroadcastReceiver? This doesn't work because the otherUser isn't authenticated
                }
                resetUser(currUser);
            }
        });

        //2.) Check which mode we're displaying:
        if(mode.equals(KEY_BASIC)){
            basicSetUp();
        }
        else{
            //Everything should be not toggleable --> doesn't set listeners:
            fabChooseBuddy.setVisibility(View.INVISIBLE);     //automatically invisible until the user wants to be a Buddy
            fabCancelBuddy.setVisibility(View.VISIBLE);         //to cancel the trip or meetup
        }
    }

    private void tripLoadMap(GoogleMap map) {
        Log.d(TAG, "in basicLoadMap():");
        wingsMap = new WingsMap(map, getContext(), getViewLifecycleOwner(), false, false);   //automatically constantly shows current location

        //Figure out what we routing to and from:
        //BuddyMeetUp or BuddyTrip?
        userBuddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
        try {
            userBuddyInstance.fetchIfNeeded();

            if(userBuddyInstance.getOnMeetup()){
                //Get the destination in the BuddyMeetUp (= the otherBuddy's currentLocation)
                meetUpInstance = userBuddyInstance.getBuddyMeetUpInstance();
                otherUser = meetUpInstance.getReceiverBuddy().getUser();
                WingsGeoPoint otherCurrentGeoPoint = (WingsGeoPoint) otherUser.getParseObject(User.KEY_CURRENTLOCATION);
                otherCurrentGeoPoint.fetchIfNeeded();

                destination = new LatLng(otherCurrentGeoPoint.getLatitude(), otherCurrentGeoPoint.getLongitude());
            }
            else{
                //get the destination in the BuddyTrip
                buddyTrip = userBuddyInstance.getBuddyTripInstance();
                WingsGeoPoint destinationGeoPoint = buddyTrip.getDestination();

                destination = new LatLng(destinationGeoPoint.getLatitude(), destinationGeoPoint.getLongitude());
            }

            wingsMap.routeFromCurrentLocation(destination, true, "Trip destination");
            /*if(wingsMap.isNearEnough()){
                if(meetUpInstance != null) {
                    makeConfirmBuddyDialog();
                }
                else{
                    makeConfirmSafeArrivalDialog();
                }
            }*/
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void basicSetUp(){
        //2.) Is the current user a buddy?
        if(currUser.getBoolean(User.KEY_ISBUDDY)) {   //if they are a buddy
            Log.d(TAG, "onViewCreated(): current user IS a Buddy!");

            // --> Get the buddyInstance:
            Buddy buddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
            try {
                buddyInstance.fetchIfNeeded();


                //2.) Check if they already have a buddy:
                //If not --> they still need one -->
                if (!buddyInstance.getHasBuddy()) {      //if they don't have a buddy but they want one, we need to show th button
                    Log.d(TAG, "onViewCreated(): This buddy is still looking for a buddy!");
                    fabChooseBuddy.setVisibility(View.VISIBLE);
                    fabCancelBuddy.setVisibility(View.VISIBLE);
                    //                tvTitle.setVisibility(View.VISIBLE);
                }
                else{
                    fabChooseBuddy.setVisibility(View.INVISIBLE);     //automatically invisible until the user wants to be a Buddy
                    fabCancelBuddy.setVisibility(View.INVISIBLE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //3.) Otherwise --> not a buddy, hide it all
        else {
            fabChooseBuddy.setVisibility(View.INVISIBLE);     //automatically invisible until the user wants to be a Buddy
            fabCancelBuddy.setVisibility(View.INVISIBLE);
//            tvTitle.setVisibility(View.INVISIBLE);
        }


        //2.) Set on click listeners:
        // 2a.) fabChooseBuddy --> To go to choose buddy frag from homefrag!
        fabChooseBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.toChooseBuddyFragment();
            }
        });


        //2c.) btnSearch --> search for and route to a location
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
                    //makeConfirmDestinationDialog(etSearchBar.getText().toString());
                    chooseDestinationOverlay.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void resetUser(ParseUser userToErase) {
        Log.d(TAG, "in resetUser()");
        //1.) Make all buttons stuff invisible:
        fabCancelBuddy.setVisibility(View.INVISIBLE);
        fabChooseBuddy.setVisibility(View.INVISIBLE);
        listener.setBuddyRequestBttn(false);

        //2.) Reset Buddy field and queriedDestination
        try {
            Buddy buddyInstance = (Buddy) userToErase.getParseObject(User.KEY_BUDDY);
            buddyInstance.fetchIfNeeded();
            buddyInstance.reset();

            WingsGeoPoint queriedDestination = (WingsGeoPoint) userToErase.getParseObject(User.KEY_QUERIEDDESTINATION);
            queriedDestination.fetchIfNeeded();
            queriedDestination.reset();
        } catch (ParseException e) {
            e.printStackTrace();
        }

       // userToErase.put(User.KEY_BUDDY, new Buddy());      //im sure there's a better way to erase it but not enough time rn
        userToErase.put(User.KEY_ISBUDDY, false);
        userToErase.put(User.KEY_DESTINATIONSTR, "default");
        userToErase.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Log.d(TAG, "resetUser(): successfully reset user");
                }
                else{
                    Log.d(TAG, "resetUser(): error reset user, error = " + e.getMessage());
                }
            }
        });

        //reset map:
        wingsMap.removeRoute();     //sets the map back to normal on this Fragment

        //remove destination:
       /* Buddy buddy = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
        try {
            buddy.fetchIfNeeded();
            buddy.setDestination(new WingsGeoPoint(currUser, 0, 0));
            buddy.saveInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }



    //Purpose:          Initializes our "map" field, starts continuously checking for location updates
    protected void basicLoadMap(GoogleMap map) {
        Log.d(TAG, "in basicLoadMap():");
        wingsMap = new WingsMap(map, getContext(), getViewLifecycleOwner(), false, false);   //automatically constantly shows current location

        //Check who we are: should we check queriedDestination or intendedDestination?
        //1.) Are we a Buddy? --> map the intendedDestination
        boolean isBuddy = currUser.getBoolean(User.KEY_ISBUDDY);
        if(isBuddy){
            //Map the intendedDestination:

            //1) Obtain the Buddy object:
                Buddy currBuddy = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
                try {
                    currBuddy.fetchIfNeeded();

                    //2.) Obtain the intendedDestination:
                    WingsGeoPoint intendedDestination = currBuddy.getDestination();
                    intendedDestination.fetchIfNeeded();

                    //3.) Save "destination" field to intendedDestination + route to it
                    destination = new LatLng(intendedDestination.getLatitude(), intendedDestination.getLongitude());

                    //wingsMap.routeFromCurrentLocation(destination, true);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

        }
        //2.) Else we are just a normal User --> map the queriedDestination if applicable!
        else{
            //Map the queriedDestination:

            //1.) Obtain the queriedDestination
             /*   WingsGeoPoint queriedDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_QUERIEDDESTINATION);
                try {
                    queriedDestination.fetchIfNeeded();

                    //2.) Check if we even have a queriedDestination (e.g. is NOT at default value (0,0)):
                    if(queriedDestination.getLatitude() != 0 && queriedDestination.getLongitude() != 0){
                        //2a.) if its filled --> save the "destination" field to queriedDestination + route to it + display Dialog to ask if they want to confirm that destination:
                        //1.) destination = ...
                            destination = new LatLng(queriedDestination.getLatitude(), queriedDestination.getLongitude());

                        //2.) route....
                            wingsMap.routeFromCurrentLocation(destination, true);

                        //3.) What kind of Dialog? With the destinationText or not?
                            String destinationTxt = currUser.getString(User.KEY_DESTINATIONSTR);

                        //3a.) if destinationTxt is still default value (= "default") --> just display the destination field (LatLng)
                            if(destinationTxt.equals("default")){
                                makeConfirmDestinationDialog(destination.latitude + ", " + destination.longitude);
                            }
                        //3b.) else --> show the destinationTxt
                            else{
                                makeConfirmDestinationDialog(destinationTxt);
                            }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/
            //2b.) else --> its not filled, do nothin
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
        //    destination = wingsMap.routeFromCurrentLocation(destinationTxt, true);
        }
    }
}