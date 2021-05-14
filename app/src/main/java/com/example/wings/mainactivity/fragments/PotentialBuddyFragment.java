package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.dialogs.ConfirmBuddyRequestDialog;
import com.example.wings.mainactivity.fragments.dialogs.ConfirmDestinationDialog;
import com.example.wings.models.Buddy;
import com.example.wings.models.BuddyRequest;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

//Purpose:      To display a selected other user's information when looking for a buddy --> displays map, routes, locations, ialogbox, etc
//              will display different Dialogs depending on who is calling this Fragment. This Fragment is only accessed in special occasions essentially

public class PotentialBuddyFragment extends Fragment implements ConfirmBuddyRequestDialog.ResultListener {
    private static final String TAG = "PotentialBuddyFragment";
    public static final String KEY_USERID = "potentialBuddyId";

    private MAFragmentsListener listener;

    private String potentialBuddyId;
    private ParseUser potentialBuddy;
    private Buddy potentialBuddyInstance;

    private SupportMapFragment mapFragment;
    private WingsMap wingsMap;

    private LatLng potentialBuddyLocation;
    private LatLng potentialBuddyDestination;

    public PotentialBuddyFragment() {}


    //Methods to override from ConfirmBuddyRequestDialog resultlistener:
    @Override
    //Purpose:      Create a BuddyRequest where current user is the sender, other user is the receiver, go back to ChooseBuddyFragment, increment the BuddyRequest in UserBuddyRequestsFragment
    public void onAccept(Buddy potentialBuddy) {
        Log.d(TAG, "onAccept()");
        Toast.makeText(getContext(), "accept button pressed", Toast.LENGTH_SHORT).show();

        //TODO: Should technically check if we've already sent a request to this Buddy
        //1.) Get current user's buddy instance:
        ParseUser user = ParseUser.getCurrentUser();
        Buddy buddyInstance = (Buddy) user.getParseObject(User.KEY_BUDDY);
        try {
            buddyInstance.fetchIfNeeded();

            //2.) Create and save BuddyRequest:
            BuddyRequest request = new BuddyRequest(buddyInstance, potentialBuddy);
            request.saveInBackground();

            //3.) Get buddyInstance's list of sentBuddyRequests:
            List<BuddyRequest> sentRequests = buddyInstance.getSentRequests();
            sentRequests.add(request);
            buddyInstance.setSentRequests(sentRequests);
            buddyInstance.saveInBackground();

            //4.) Save the receivedRequest in the potentialBuddy now --> later this is how its received:
            List<BuddyRequest> othersReceivedRequests = potentialBuddy.getReceivedRequests();
            othersReceivedRequests.add(request);
            potentialBuddy.setReceivedRequests(othersReceivedRequests);
            potentialBuddy.saveInBackground();


            //5.) Go back to ChooseBuddyFrag
            listener.toChooseBuddyFragment();
        } catch (ParseException e) {
            Log.d(TAG, "onAccept: error getting buddy request I think, error=" + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    //Purpose:      Just go back to the ChooseBuddyFragment
    public void onReject() {
        Toast.makeText(getContext(), "reject button pressed", Toast.LENGTH_SHORT).show();
        listener.toChooseBuddyFragment();
    }


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

    public static PotentialBuddyFragment newInstance(String param1, String param2) {
        PotentialBuddyFragment fragment = new PotentialBuddyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            potentialBuddyId = getArguments().getString(KEY_USERID);
            Log.d(TAG, "onCreate():  potentialBuddyId =" + potentialBuddyId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //1.) Initialize view:
        View view = inflater.inflate(R.layout.fragment_potential_buddy, container, false);

        //2.) Initialize map fragment:
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initalize potentialBuddy:
        if(potentialBuddyId != null) {
            queryPotentialBuddy();
        }
        else{
            Log.d(TAG, "potentialBuddyId = null");
        }
        ExtendedFloatingActionButton fabBack = view.findViewById(R.id.fabBack);
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
            }
        });
    }

    private void queryPotentialBuddy(){
        Log.d(TAG, "in queryPotentialBuddy(): potentialBuddyId="+ potentialBuddyId);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ParseUser.KEY_OBJECT_ID, potentialBuddyId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    Log.d(TAG, "in queryPotentialBuddy(): success!: response=" + objects.toString());
                    setPotentialBuddy(objects.get(0));
                    setUp();
                }
            }
        });
    }

    private void setUp(){
        if(potentialBuddy != null) {
            potentialBuddyInstance = (Buddy) potentialBuddy.getParseObject(User.KEY_BUDDY);
            try {
                //Initialize potentialBuddyDestination:
                potentialBuddyInstance.fetchIfNeeded();
                WingsGeoPoint destinationGeoPoint = (WingsGeoPoint) potentialBuddyInstance.getParseObject(Buddy.KEY_DESTINATION);
                destinationGeoPoint.fetchIfNeeded();
                potentialBuddyDestination = new LatLng(destinationGeoPoint.getLatitude(), destinationGeoPoint.getLongitude());

                //Initialized potentialBuddyLocation:
                WingsGeoPoint currLocationGeoPoint = (WingsGeoPoint) potentialBuddy.getParseObject(User.KEY_CURRENTLOCATION);
                currLocationGeoPoint.fetchIfNeeded();
                potentialBuddyLocation = new LatLng(currLocationGeoPoint.getLatitude(), currLocationGeoPoint.getLongitude());

                //Map the route of potentialBuddyLoc --> potentialBuddyDestination:
                wingsMap.setMarker(potentialBuddyLocation, BitmapDescriptorFactory.HUE_BLUE);
                wingsMap.route(potentialBuddyLocation, potentialBuddyDestination);

                //Show dialog:
                makeConfirmBuddyRequestDialog();

            } catch (ParseException e) {
                Log.d(TAG, "onViewCreated(): error fetching buddy instance");
                e.printStackTrace();
            }
        }
    }
    private void setPotentialBuddy(ParseUser otherUser){
        Log.d(TAG, "setPotentialBuddy()");
        potentialBuddy = otherUser;
    }

    private void loadMap(GoogleMap map){
        wingsMap = new WingsMap(map, getContext(), getViewLifecycleOwner());   //automatically constantly shows current location

    }

    //Purpose:      Creates a ConfirmDestinationDialog with the given destination string to display
    public void makeConfirmBuddyRequestDialog(){
        Log.d(TAG, "makeConfirmBuddyRequestDialog(): potentialBuddyId=" + potentialBuddyId);
        ConfirmBuddyRequestDialog dialog = ConfirmBuddyRequestDialog.newInstance(potentialBuddyId);
        dialog.setTargetFragment(PotentialBuddyFragment.this, 1);
        dialog.show(getFragmentManager(), "ConfirmBuddyRequestDialogTag");
    }


}