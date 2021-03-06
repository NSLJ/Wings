package com.example.wings.mainactivity.fragments.home;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.mainactivity.fragments.dialogs.SendRequestStepsDialog;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.helpers.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * ConfirmBuddyHomeFragment.java
 * Purpose:                 This HomeFragment is used when the user must either respond to or send a BuddyRequest. Unlike other HomeFragments, this one is solely used to display BuddyRequest information and obtain a response back.
 *                          This HomeFragment has two modes: sending BuddyRequest or answering BuddyRequest. Due to the similarities in the types of funcitons, only one layout file is used and certain views' visibility are toggled.
 *
 * Layout file:     fragment_confirm_buddy_home.xml
 */
public class ConfirmBuddyHomeFragment extends Fragment {
    private static final String TAG = "ConfirmBuddyHomeFragment";
    public static final String KEY_MODE = "whatMode?";     //mode passed in
    public static final String KEY_OTHER_USER_ID = "otherUserId";
    public static final String KEY_BUDDYREQUESTID = "buddyRequestId";

    public static final String KEY_SEND_MODE = "modeSendRequest";
    public static final String KEY_ANSWER_MODE = "modeAnswerRequest";

    private MAFragmentsListener listener;
    private String mode;
    private String otherUserId;
    private String buddyRequestId;
    ParseUser currUser = ParseUser.getCurrentUser();
    ParseUser otherUser;
    Buddy otherBuddyInstance;
    BuddyRequest buddyRequestInstance;


    //Fields for mapping:
    private WingsMap wingsMap;
    private LatLng otherDestination;
    private LatLng otherCurrLocation;

    //Views:
    public static RelativeLayout requestOverlay;
    private SupportMapFragment mapFragment;
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvDistance;
    private RatingBar ratingBar;
    public static Button btnSendRequest;
    private Button btnBack;
    private ImageView ivAcceptRequest;
    private ImageView ivRejectRequest;
    public static TextView tvTripDestination;
    TextView tvLoad;

    public static Resources resource;
    public ConfirmBuddyHomeFragment() {}

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
    //Purpose:      Initalizes mode, otherUserId field.
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getString(KEY_MODE);
            otherUserId = getArguments().getString(KEY_OTHER_USER_ID);
            Log.d(TAG, "onCreate(): mode="+mode);

            if(mode.equals(KEY_ANSWER_MODE)){       //if we are answering the request --> a BuddyRequest already exists!, otherwise we are sending a request and a BuddyRequest does not yet exist
                buddyRequestId = getArguments().getString(KEY_BUDDYREQUESTID);
                Log.d(TAG, "onCreate(): buddyRequestId received=" +buddyRequestId);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_confirm_buddy_home, container, false);
        setMapFragment();
        return mainView;
    }

    private void setMapFragment(){
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        //Error check mapFragment
        if (mapFragment != null) {
            // getMapAsync() --> initializes maps system and view:
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    wingsMap = new WingsMap(googleMap, getContext(), getViewLifecycleOwner(), true, false);        //automatically constantly shows current location
                }
            });
        }
        else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resource = getResources();
        requestOverlay = view.findViewById(R.id.buddyRequestOverlay);
        ivProfilePic = view.findViewById(R.id.ivSendRequestPic);
        tvName = view.findViewById(R.id.tvSendRequestName);
        tvEmail = view.findViewById(R.id.tvSendRequestEmail);
        tvDistance = view.findViewById(R.id.tvSendRequestDistance);
        ratingBar = view.findViewById(R.id.rbSendRequestRating);
        btnSendRequest = view.findViewById(R.id.btnSendRequest);
        ivAcceptRequest = view.findViewById(R.id.ivAccept);
        ivRejectRequest = view.findViewById(R.id.ivReject);
        btnBack = view.findViewById(R.id.btnBack);
        tvTripDestination = view.findViewById(R.id.tvOtherBuddyId);
        tvLoad = view.findViewById(R.id.tvLoad);

        btnBack.setOnClickListener(new View.OnClickListener() {         //TODO: I think we should keep a history of fragment passing in MainActivity to just go to some "previous" fragment instead
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
            }
        });

        queryOtherUser(otherUserId);

    }

    private void queryOtherUser(String id) {
        Log.d(TAG, "in queryOtherUser(): otherUserId=" + id);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ParseUser.KEY_OBJECT_ID, id);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "in queryPotentialBuddy(): success!: response=" + objects.toString());
                    setOtherUser(objects.get(0));
                    drawOtherRoute();
                    if(mode.equals(KEY_ANSWER_MODE)) {
                        queryBuddyRequest();        //obtains the BuddyRequest inquestion, then populates all views
                    }
                    else{
                        setUp();
                        tvLoad.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    Log.d(TAG, "queryPotentialBuddy(): error=" + e.getLocalizedMessage());
                }
            }
        });
    }
    private void setOtherUser(ParseUser other){
        otherUser = other;
    }

    //Purpose:      draws the otherUser's route on the map relative to the current user's current location!
    private void drawOtherRoute(){
            Log.d(TAG, "drawOtherRoute()");
            otherBuddyInstance = (Buddy) otherUser.getParseObject(User.KEY_BUDDY);
            try {
                //Initialize otherUser's destination:
                otherBuddyInstance.fetchIfNeeded();
                WingsGeoPoint otherDestinationGeoPoint = (WingsGeoPoint) otherBuddyInstance.getParseObject(Buddy.KEY_DESTINATION);
                otherDestinationGeoPoint.fetchIfNeeded();
                otherDestination = new LatLng(otherDestinationGeoPoint.getLatitude(), otherDestinationGeoPoint.getLongitude());

                //Initialize otherUser's current location:
                WingsGeoPoint otherCurrLocationGeoPoint = (WingsGeoPoint) otherUser.getParseObject(User.KEY_CURRENTLOCATION);
                otherCurrLocationGeoPoint.fetchIfNeeded();
                otherCurrLocation = new LatLng(otherCurrLocationGeoPoint.getLatitude(), otherCurrLocationGeoPoint.getLongitude());

                Log.d(TAG, "drawOtherRoute(): otherCurrLocation=" + otherCurrLocation.toString());
                //Map the other user's route:
                wingsMap.setMarker(otherCurrLocation, BitmapDescriptorFactory.HUE_BLUE, true, "Their current location");
                wingsMap.route(otherCurrLocation, otherDestination, true, "Their destination");
                if(mode.equals(KEY_ANSWER_MODE)){
                    wingsMap.setMarker(otherCurrLocation, BitmapDescriptorFactory.HUE_GREEN, true, "Requested meetup location");
                }

                Buddy userBuddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
                userBuddyInstance.fetchIfNeeded();
                WingsGeoPoint currUserDestination = userBuddyInstance.getDestination();
                //wingsMap.setMarker(new LatLng(currUserDestination.getLatitude(), currUserDestination.getLongitude()), BitmapDescriptorFactory.HUE_BLUE, true, "Your Destination");
            } catch (ParseException e) {
                Log.d(TAG, "onViewCreated(): error fetching buddy instance");
                e.printStackTrace();
            }
    }

    private void queryBuddyRequest(){
        //Set the TripDestination textview:
        ParseQuery<BuddyRequest> query = ParseQuery.getQuery(BuddyRequest.class);
        query.whereEqualTo(BuddyRequest.KEY_OBJECT_ID, buddyRequestId);
        query.findInBackground(new FindCallback<BuddyRequest>() {
            @Override
            public void done(List<BuddyRequest> objects, ParseException e) {
                if (e == null) {
                    BuddyRequest requestInQuestion = objects.get(0);
                    setBuddyRequestInstance(requestInQuestion);
                    setUp();
                    tvLoad.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    private void setBuddyRequestInstance(BuddyRequest buddyRequest){
        buddyRequestInstance = buddyRequest;
    }

    //Purpose:      Populate views and handle visibility depending on mode field! Implements corresponding onclick listeners. Assumes otherUser, otherCurrLocation, and buddyRequestInstance is already initalized!
    private void setUp(){
        if(otherUser != null && otherCurrLocation != null) {
            //1.) Populate the views shared by both modes:
            try {
                //1a.) Calculate distance b/w user's current location and otherUser's current location
                ParseGeoPoint otherCurrLocationParse = new ParseGeoPoint(otherCurrLocation.latitude, otherCurrLocation.longitude);
                WingsGeoPoint currLocationGeoPoint = (WingsGeoPoint) currUser.getParseObject(User.KEY_CURRENTLOCATION);
                currLocationGeoPoint.fetchIfNeeded();
                ParseGeoPoint currentLocation = currLocationGeoPoint.getLocation();

                double distance = currentLocation.distanceInKilometersTo(otherCurrLocationParse) * 1000;      //* 1000 to convert to m

                //1b.) Fill in Views:
                double roundedDistance = Math.round(distance * 100.0) / 100.0;
                tvDistance.setText(Double.toString(roundedDistance) + " m away from your destination");

                ParseFile imageFile = otherUser.getParseFile(User.KEY_PROFILEPICTURE);
                if (imageFile != null) {
                    Glide.with(getContext()).load(imageFile.getFile()).into(ivProfilePic);
                }
                tvName.setText(otherUser.getString(User.KEY_FIRSTNAME));
                tvEmail.setText(otherUser.getString(User.KEY_EMAIL));
                ratingBar.setRating((float) otherUser.getDouble(User.KEY_RATING));

            } catch (ParseException e) {
                Log.d(TAG, "onViewCreated(): error fetching buddy instance");
                e.printStackTrace();
            }

            //2.) Special handlers depending on modes:
            if (mode.equals(KEY_SEND_MODE)) {           //check if in send request mode
                //Display Instructions through a dialog:
                SendRequestStepsDialog dialog = SendRequestStepsDialog.newInstance();
                dialog.setTargetFragment(ConfirmBuddyHomeFragment.this, 1);
                dialog.show(getFragmentManager(), "SendRequestStepsDialogTag");

                ivAcceptRequest.setVisibility(View.INVISIBLE);
                ivRejectRequest.setVisibility(View.INVISIBLE);
                btnSendRequest.setBackgroundColor(getResources().getColor(R.color.gray, null));     //disabled color of button

                btnSendRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Check if user has placed a marker on the map:
                        LatLng targetDestination = wingsMap.getClickedTargetDestination();
                        Log.d(TAG, "btnSendRequest is clicked: targetDestination = " + targetDestination.toString());
                        if(targetDestination != null && targetDestination.latitude == 0 && targetDestination.longitude == 0){            //default value
                            Toast.makeText(getContext(), "You have not placed a trip destination!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            sendRequest(otherBuddyInstance, targetDestination);                //Creates a BuddyRequest, navigates back to ChooseBuddyFragment
                        }
                    }
                });
            }

            else {                                  //in answer request mode
                btnSendRequest.setVisibility(View.INVISIBLE);
                WingsGeoPoint tripDestination = buddyRequestInstance.getDestination();
                tvTripDestination.setText("Trip Destination:  ("+ Math.round(tripDestination.getLatitude()*1000.0)/1000.0 +", " + Math.round(tripDestination.getLongitude()*1000.0)/1000.0+")");

                ivRejectRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bttnReject - onClick:");
                        onReject(buddyRequestId);                               //deletes the BuddyRequest, removes it from ReceivedRequests
                    }
                });

                ivAcceptRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bttnAccept - onClick():");
                        onAccept(otherBuddyInstance, buddyRequestId, tripDestination);             //Create a BuddyMeetUp, update both Buddy instances --> go to BuddyHomeFragment w/meetUp mode
                    }
                });
            }
        }
    }

    //Purpose:      Handlers for when in answer request mode. If accept the request --> make a BuddyMeetUp, update BuddyRequest
    public void onAccept(Buddy confirmedBuddy, String buddyRequestId, WingsGeoPoint tripDestination) {     //given the Buddy we just confirmed with
        Log.d(TAG, "onAccept(), mode= answering request");
        CountDownLatch waitForSaving = new CountDownLatch(4);           //so we wait for all Parse saving before navigating to next fragment at end of method

        try {
            //1.) Create a BuddyMeetUp:
            Buddy currBuddy = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
            currBuddy.fetchIfNeeded();
            BuddyMeetUp buddyMeetUp = new BuddyMeetUp(confirmedBuddy, currBuddy, tripDestination);       //other buddy is the sender, and current user is the receiver
            buddyMeetUp.save();

            //2.) Change the Buddy fields:
            //2a.) for current user's buddy instance:
            updateHaveBuddy(currBuddy, buddyMeetUp, waitForSaving);
            updateHaveBuddy(confirmedBuddy, buddyMeetUp, waitForSaving);

            //3.) Delete this BuddyRequest and go to BuddyHomeFrag:
            deleteRequestFromLists(buddyRequestId);
            buddyRequestInstance.delete();

            //4.) Package all necessary data:
            //1.) Package ParseObjects into a ParcelableObject to send as a Parcelable:
            ParcelableObject sendData = new ParcelableObject();
            sendData.setMode(BuddyHomeFragment.KEY_MEET_BUDDY_MODE);
           // sendData.setContextFrom("any string bc this is not MainActivity");
            sendData.setBuddyMeetUp(buddyMeetUp);
            sendData.setOtherBuddy(otherBuddyInstance);
            sendData.setOtherParseUser(otherUser);
            sendData.setCurrBuddy(currBuddy);

            listener.toBuddyHomeFragment(sendData);
            Toast.makeText(getContext(), "Start meetup with your buddy!", Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //Purpose;      helper method for the onAccept(), updates a given Buddy object to embody a BuddyMeetUp.
    private void updateHaveBuddy(Buddy buddyToUpdate, BuddyMeetUp buddyMeetUp, CountDownLatch latch){
        buddyToUpdate.setHasBuddy(true);
        buddyToUpdate.setOnMeetup(true);
        buddyToUpdate.setBuddyMeetUp(buddyMeetUp);
        buddyToUpdate.setReceivedRequests(new ArrayList<>());
        buddyToUpdate.setSentRequests(new ArrayList());
        try {
            buddyToUpdate.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //Purpose:      Handler for when in answer request mode, on reject --> remove this BuddyRequest from the list of receivedRequest + delete the entire BuddyRequest
    public void onReject(String buddyRequestId) {
        Log.d(TAG, "onReject():  remove the request from the list and delete the entire request!");

            //1.) Remove the BuddyRequest from the current user's list of ReceivedRequests:
            deleteRequestFromLists(buddyRequestId);

            //2.) Get rid of the BuddyRequest instance entirely, then navigate back:
            ParseQuery<BuddyRequest> query = ParseQuery.getQuery(BuddyRequest.class);
            query.whereEqualTo(BuddyRequest.KEY_OBJECT_ID, buddyRequestId);
            query.findInBackground(new FindCallback<BuddyRequest>() {
            @Override
                public void done(List<BuddyRequest> objects, ParseException e) {
                    if(e == null) {
                        BuddyRequest requestInQuestion = objects.get(0);
                        try {
                            requestInQuestion.delete();
                            listener.toCurrentHomeFragment();
                        } catch (ParseException parseException) {
                            parseException.printStackTrace();
                        }
                    }
                    else{
                        Log.d(TAG, "onReject(): error getting BuddyRequest(), error="+e.getLocalizedMessage());
                    }
                }
            });


    }


    //Purpose:      Handler for mode = sending request. This is called when the btnSendRequest button is clicked. Creates a BuddyRequest where current user is the sender, other user is the receiver, goes back to ChooseBuddyFragment, increment the BuddyRequest in UserBuddyRequestsFragment
    public void sendRequest(Buddy potentialBuddy, LatLng tripDestination) {
        Log.d(TAG, "onAccept()");
        //CountDownLatch waitForSaving = new CountDownLatch(3);

        WingsGeoPoint tripDestinationGeoPoint = new WingsGeoPoint(currUser, tripDestination.latitude, tripDestination.longitude);

        //1.) Get current user's buddy instance:
        Buddy buddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
        try {
            buddyInstance.fetchIfNeeded();

            //2.) Create and save BuddyRequest:
            BuddyRequest request = new BuddyRequest(buddyInstance, potentialBuddy, tripDestinationGeoPoint);
            request.save();

            //3.) Get buddyInstance's list of sentBuddyRequests:
            List<BuddyRequest> sentRequests = buddyInstance.getSentRequests();
            sentRequests.add(request);
            buddyInstance.setSentRequests(sentRequests);
            buddyInstance.save()/*InBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "sendRequest(): buddy sentRequest list saved successfully");
                        waitForSaving.countDown();
                    }
                }
            })*/;

            //4.) Save the receivedRequest in the potentialBuddy now --> later this is how its received:
            List<BuddyRequest> othersReceivedRequests = potentialBuddy.getReceivedRequests();
            othersReceivedRequests.add(request);
            potentialBuddy.setReceivedRequests(othersReceivedRequests);
            potentialBuddy.save()/*InBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "sendRequest(): buddy receivedRequests list saved succesfully");
                        waitForSaving.countDown();
                    }
                }
            })*/;

            //5.) Go back to ChooseBuddyFrag
           /* try {
                Log.d(TAG, "sendRequest(): MAFragmentsListner waiting for all parse updates to finish...");
                waitForSaving.await();
                //Thread.sleep(3500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            listener.toChooseBuddyFragment();
        } catch (ParseException e) {
            Log.d(TAG, "onAccept: error getting buddy request I think, error=" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Purpose:      Removes this buddyrequestId from both the receivedRequests and sentRequests field from currUser/otherUser
    private void deleteRequestFromLists(String buddyRequestId){
        //1.) Remove the BuddyRequest from the current user's list of ReceivedRequests:
        Buddy currBuddy = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
        try {
            currBuddy.fetchIfNeeded();
            List<BuddyRequest> receivedRequests = currBuddy.getReceivedRequests();
            for(int i = 0; i < receivedRequests.size(); i++){
                BuddyRequest currRequest = receivedRequests.get(i);
                if(currRequest.getObjectId().equals(buddyRequestId)){
                    receivedRequests.remove(i);
                    break;
                }
            }

            currBuddy.setReceivedRequests(receivedRequests);
            currBuddy.save();

            //2.) Remove the BuddyRequest from the other user's list of SentRequests:
            List<BuddyRequest> otherSentRequests = otherBuddyInstance.getSentRequests();
            for(int i = 0; i < otherSentRequests.size(); i++){
                BuddyRequest currRequest = otherSentRequests.get(i);
                if(currRequest.getObjectId().equals(buddyRequestId)){
                    otherSentRequests.remove(i);
                    break;
                }
            }
            otherBuddyInstance.setSentRequests(otherSentRequests);
            otherBuddyInstance.save();
        }catch(ParseException e){
            Log.d(TAG, "onReject() after RespondBuddyRequestDialog");
        }
    }

    public static void enableSendOverlay(LatLng targetDestination){
        Log.d(TAG, "enableSendOverlay()");
        btnSendRequest.setBackgroundColor(resource.getColor(R.color.logo_teal, null));        //enabled color
        tvTripDestination.setText("Trip Destination:  ("+ Math.round(targetDestination.latitude*1000.0)/1000.0 +", " + Math.round(targetDestination.longitude*1000.0)/1000.0+")");
        requestOverlay.setVisibility(View.VISIBLE);
    }

}