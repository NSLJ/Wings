package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.models.helpers.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.dialogs.ConfirmSendRequestDialog;
import com.example.wings.mainactivity.fragments.dialogs.RespondBuddyRequestDialog;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyRequest;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.WingsGeoPoint;
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

//TODO: create another dialog fragment and have other fragments give this one parameters of which dialogbox to show
//TODO: Figure out more hoe dialog boxes work, make them prettier, now how to still interact with the background, or at least not just disapper by touching the screen --> can leave us on this screen
//TODO: Make Titles for this fragment bc looking at a ton of maps really gets confusing for screen to screen

public class PotentialBuddyFragment extends Fragment implements ConfirmSendRequestDialog.ResultListener, RespondBuddyRequestDialog.ResultListener{
    private static final String TAG = "PotentialBuddyFragment";
    private static final String KEY_USERID = "potentialBuddyId";
    private static final String KEY_DIALOG = "dialogTypeToShow";
    private static final String KEY_BUDDYREQUESTID = "buddyRequestId";

    public static final String KEY_SHOW_CONFIRMSEND = "confirmSendDialog";
    public static final String KEY_SHOW_RESPONDREQUEST = "respondRequestDialog";            //will be publically accessed so other frags and activites cand easily go to

    private MAFragmentsListener listener;

    private String potentialBuddyId;
    private String buddyRequestId;
    private String dialogToShow;
    DialogFragment dialog;

    private ParseUser potentialBuddy;
    private Buddy potentialBuddyInstance;

    private SupportMapFragment mapFragment;
    private WingsMap wingsMap;
    private TextView tvTitle;

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
            dialogToShow = getArguments().getString(KEY_DIALOG);
            Log.d(TAG, "onCreate(): dialogKey=" + dialogToShow);
            buddyRequestId = getArguments().getString(KEY_BUDDYREQUESTID);
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
        //set title accordingly to what purpose is, initialize the dialog to show accordingly
        tvTitle = view.findViewById(R.id.tvTitle);
        if(dialogToShow.equals(KEY_SHOW_CONFIRMSEND)){
            tvTitle.setText("Send Request to this Buddy?");
            dialog = ConfirmSendRequestDialog.newInstance(potentialBuddyId);
        }

        if(dialogToShow.equals(KEY_SHOW_RESPONDREQUEST)){
            tvTitle.setText("Respond to this Buddy Request");
            dialog = RespondBuddyRequestDialog.newInstance(potentialBuddyId, buddyRequestId);
        }


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
                wingsMap.setMarker(potentialBuddyLocation, BitmapDescriptorFactory.HUE_BLUE, true, "Their current location");
                wingsMap.route(potentialBuddyLocation, potentialBuddyDestination, true);

                //Show dialog after showing map:
                showDialog(dialogToShow);

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
        wingsMap = new WingsMap(map, getContext(), getViewLifecycleOwner(), false);   //automatically constantly shows current location

    }

    //Purpose:      Creates a ConfirmDestinationDialog with the given destination string to display
  /*  public void makeConfirmBuddyRequestDialog(){
        Log.d(TAG, "makeConfirmBuddyRequestDialog(): potentialBuddyId=" + potentialBuddyId);
        ConfirmSendRequestDialog dialog = ConfirmSendRequestDialog.newInstance(potentialBuddyId);
        dialog.setTargetFragment(PotentialBuddyFragment.this, 1);
        dialog.show(getFragmentManager(), "ConfirmBuddyRequestDialogTag");
    }*/

    public void showDialog(String tag){
        dialog.setTargetFragment(PotentialBuddyFragment.this, 1);
        dialog.show(getFragmentManager(), tag);
    }


    //Overrided for RespondBuddyRequestDialog
    //Methods that are called when RespondBuddyRequest tells us whether current user chose to accept or not accept the request
    @Override
    public void onAccept(Buddy confirmedBuddy, String buddyRequestId) {     //given the Buddy we just confirmed with
        //Accept the request -->
        //Make BuddyMeetUp
        //Get buddyInstance
       /* ParseUser currentUser = ParseUser.getCurrentUser();
        Buddy currBuddy = (Buddy) currentUser.getParseObject(User.KEY_BUDDY);
        try {
            currBuddy.fetchIfNeeded();

            BuddyMeetUp buddyMeetUp = new BuddyMeetUp(confirmedBuddy, currBuddy, );
            buddyMeetUp.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "we saved the buddyMeetUp instance ok!");
                    }
                    else{
                        Log.d(TAG, "error saving the buddyMeetUp, error=" +e.getMessage());
                    }
                }
            });

            //Change the Buddy fields:
            currBuddy.setHasBuddy(true);
            currBuddy.setOnMeetup(true);
            currBuddy.setBuddyMeetUp(buddyMeetUp);
            currBuddy.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "we saved curBuddy ok!");
                    }
                    else{
                        Log.d(TAG, "error saving currBuddy error=" +e.getMessage());
                    }
                }
            });

            confirmedBuddy.setHasBuddy(true);
            confirmedBuddy.setOnMeetup(true);
            confirmedBuddy.setBuddyMeetUp(buddyMeetUp);
            confirmedBuddy.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "we saved confirmedBuddy ok!");
                    }
                    else{
                        Log.d(TAG, "error saving confirmedBuddy error=" +e.getMessage());
                    }
                }
            });

            //4.) Get rid of the BuddyRequest entirely (by making everything empty)
            currBuddy.setReceivedRequests(new ArrayList<>());
            currBuddy.setSentRequests(new ArrayList());
            confirmedBuddy.setReceivedRequests(new ArrayList());
            confirmedBuddy.setSentRequests(new ArrayList());
            //Technically should go through each BuddyRequest in each list and make null/empty

            //if valid id:
            if(!buddyRequestId.equals("none")) {
                ParseQuery<BuddyRequest> query = ParseQuery.getQuery(BuddyRequest.class);
                query.whereEqualTo(BuddyRequest.KEY_OBJECT_ID, buddyRequestId);
                query.findInBackground(new FindCallback<BuddyRequest>() {
                    @Override
                    public void done(List<BuddyRequest> objects, ParseException e) {
                        BuddyRequest requestInQuestion =  objects.get(0);
                        try {
                            requestInQuestion.delete();
                        } catch (ParseException parseException) {
                            parseException.printStackTrace();
                        }
                    }
                });

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Directly go to HomeFragment to start the BuddyMeetUp!
        listener.toHomeFragment(HomeFragment.KEY_ONTRIP);
        Toast.makeText(getContext(), "Start meetup with your buddy!", Toast.LENGTH_LONG).show();*/
    }

    @Override
    public void onReject(String buddyRequestId) {
        if(!buddyRequestId.equals("none")) {
            //1.) Get buddyInstance:
            ParseUser currentUser = ParseUser.getCurrentUser();
            Buddy currBuddy = (Buddy) currentUser.getParseObject(User.KEY_BUDDY);
            try {
                currBuddy.fetchIfNeeded();

                //Get all receivedRequests:
                List<BuddyRequest> receivedRequests = currBuddy.getReceivedRequests();
                for(int i = 0; i < receivedRequests.size(); i++){
                    BuddyRequest currRequest = receivedRequests.get(i);
                    if(currRequest.getObjectId().equals(buddyRequestId)){
                        receivedRequests.remove(i);
                        break;
                    }
                }

                currBuddy.setReceivedRequests(receivedRequests);
                currBuddy.saveInBackground();
            }catch(ParseException e){
                Log.d(TAG, "onReject() after RespondBuddyRequestDialog");
            }

            //Take this Buddy Request out of the list of ReceivedRequests!


        //Get rid of the BuddyRequest instance entirely since it was rejected?
        //if valid id:
            ParseQuery<BuddyRequest> query = ParseQuery.getQuery(BuddyRequest.class);
            query.whereEqualTo(BuddyRequest.KEY_OBJECT_ID, buddyRequestId);
            query.findInBackground(new FindCallback<BuddyRequest>() {
                @Override
                public void done(List<BuddyRequest> objects, ParseException e) {
                    BuddyRequest requestInQuestion = objects.get(0);
                    try {
                        requestInQuestion.delete();
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                }
            });
        }
    }


    //Methods to override from ConfirmSendRequestDialog resultlistener:
    @Override
    //Purpose:      Create a BuddyRequest where current user is the sender, other user is the receiver, go back to ChooseBuddyFragment, increment the BuddyRequest in UserBuddyRequestsFragment
    public void onAccept(Buddy potentialBuddy) {
        /*Log.d(TAG, "onAccept()");

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
        }*/

    }

    @Override
    //Purpose:      Just go back to the ChooseBuddyFragment
    public void onReject() {
        Toast.makeText(getContext(), "reject button pressed", Toast.LENGTH_SHORT).show();
        listener.toChooseBuddyFragment();
    }

}