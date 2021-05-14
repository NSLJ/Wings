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
import com.example.wings.models.Buddy;
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
public class PotentialBuddyFragment extends Fragment {
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
}