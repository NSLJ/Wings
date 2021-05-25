package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.adapters.ChooseBuddyAdapter;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.dialogs.RespondBuddyRequestDialog;
import com.example.wings.models.Buddy;
import com.example.wings.models.BuddyMeetUp;
import com.example.wings.models.BuddyRequest;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose:         The screen shown to the user to manage their buddy requests. Shows all the buddy requests they sent, and the ones they have yet to respond to
 */


public class UserBuddyRequestsFragment extends Fragment{
    private static final String TAG = "UserBuddyRequestsFragment";

    private MAFragmentsListener listener;
    private ParseUser currentUser = ParseUser.getCurrentUser();
    private ParseGeoPoint currentLocation;

    //Views:
    private RecyclerView rvSentRequests;
    private RecyclerView rvReceivedRequests;
    private Button btnBack;
    ChooseBuddyAdapter sentRequestsAdapter;
    ChooseBuddyAdapter receivedRequestAdapter;

    //Models:
    //Sent requests:
    List<ParseUser> peopleSentTo;
    List<Double> distanceFromSent;
    List<ParseUser> peopleReceivedFrom;
    List<Double> distanceFromReceivers;

    List<BuddyRequest> sentRequests;
    List<BuddyRequest> receivedRequests;

    public UserBuddyRequestsFragment() {
    }


    public static UserBuddyRequestsFragment newInstance(String param1, String param2) {
        UserBuddyRequestsFragment fragment = new UserBuddyRequestsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_buddy_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvSentRequests = view.findViewById(R.id.rvSent);                    //displays the receivers sent those request TO --> no clickability
        rvReceivedRequests = view.findViewById(R.id.rvReceived);            //displays the Senders these requests came from
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //just go to homeFragment bc why not:               //TODO: go bach to previous fragment instead
                listener.toBuddyHomeFragment(BuddyHomeFragment.KEY_FIND_BUDDY_MODE);
            }
        });
        peopleSentTo = new ArrayList<>();
        distanceFromSent = new ArrayList<>();
        peopleReceivedFrom = new ArrayList<>();
        distanceFromReceivers = new ArrayList<>();


        //Set on click listener:
        //2.) Create an ChooseBuddyAdapter.OnClickListener to create a ChooseBuddyAdapter -- will be used by both recycler views as tapping on any row does the same thing
        ChooseBuddyAdapter.OnClickListener senderOnClickListener = new ChooseBuddyAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                //I think position = corresponds to the indexes in the List<> models but may be backwards or something:
                Toast.makeText(getContext(), "You are waiting for this Buddy to respond!", Toast.LENGTH_SHORT).show();
            }
        };

        //2.) Create an ChooseBuddyAdapter.OnClickListener to create a ChooseBuddyAdapter -- will be used by both recycler views as tapping on any row does the same thing
        ChooseBuddyAdapter.OnClickListener receiverOnClickListener = new ChooseBuddyAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                //I think position = corresponds to the indexes in the List<> models but may be backwards or something:
                Toast.makeText(getContext(), "You clicked on a request, going to PotentialBuddyFragment + some specific dialog to show", Toast.LENGTH_SHORT).show();
                BuddyRequest requestInQuestion = receivedRequests.get(position);
                ParseUser thisUser = peopleReceivedFrom.get(position);
                try {
                    thisUser.fetchIfNeeded();
                    String objectId = thisUser.getObjectId();
                    listener.toConfirmBuddyHomeFragment(ConfirmBuddyHomeFragment.KEY_ANSWER_MODE, objectId, requestInQuestion.getObjectId());
                   // listener.toPotentialBuddyFragment(objectId, PotentialBuddyFragment.KEY_SHOW_RESPONDREQUEST, requestInQuestion.getObjectId());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        sentRequestsAdapter = new ChooseBuddyAdapter(getContext(), peopleSentTo, distanceFromSent, senderOnClickListener);
        receivedRequestAdapter = new ChooseBuddyAdapter(getContext(), peopleReceivedFrom, distanceFromReceivers, receiverOnClickListener);

        //set up both rv's:
        rvSentRequests.setAdapter(sentRequestsAdapter);
        rvSentRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        rvReceivedRequests.setAdapter(receivedRequestAdapter);
        rvReceivedRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        //initialize the current user's currentLocation to find distance:
        WingsGeoPoint currLocationGeoPoint = (WingsGeoPoint) currentUser.getParseObject(User.KEY_CURRENTLOCATION);
        try {
            currLocationGeoPoint.fetchIfNeeded();
            currentLocation = currLocationGeoPoint.getLocation();

        } catch (ParseException e) {
            Log.d(TAG, "onViewCreated(): error trying to fetch the WingsGeoPoint currLocation");
            e.printStackTrace();
        }

        getRequests();      //initializes sentRequests and receivedRequests fields
        getParseUsers(sentRequests, false);                  //As the sentRequests, I want to display the receivers of who I sent my requests to --> false  //Initializes all models depending on looking for sender or receiver
        getParseUsers(receivedRequests, true);
    }

    //Purpose:      To initialize/get the sentRequests and receivedRequests
    public void getRequests() {
        Buddy buddyInstance = (Buddy) currentUser.getParseObject(User.KEY_BUDDY);
        try {
            buddyInstance.fetchIfNeeded();

            sentRequests = buddyInstance.getSentRequests();
            receivedRequests = buddyInstance.getReceivedRequests();
        } catch (ParseException e) {
            Log.d(TAG, "getRequests(): error fetching buddy instance, e=" + e.getMessage());
            e.printStackTrace();
        }
    }

    //Purpose:      take out the ParseUser and find the distances b/w our currentLocation and the other ParseUser's currentLocation, assume our currentLocation is initialized.
    public void getParseUsers(List<BuddyRequest> requestList, boolean getSender) {
        List<ParseUser> result = new ArrayList<>();
        List<Double> distancesList = new ArrayList<>();         //use setter methods to set them appropriately at the end

        for (int i = 0; i < requestList.size(); i++) {
            BuddyRequest currentRequest = requestList.get(i);
            if (getSender) {
                try {
                    currentRequest.fetchIfNeeded();

                    //Get the senders of each BuddyRequest
                    Buddy currentSenderBuddy = currentRequest.getSender();
                    currentSenderBuddy.fetchIfNeeded();
                    ParseUser currentSenderUser = currentSenderBuddy.getUser();

                    //Add found user to List:
                    result.add(currentSenderUser);


                    //Now that we have the user --> find the distance bewteen:
                    //Find the ParseGeoPoint of the otherUser:
                    WingsGeoPoint otherCurrLocationGeoPoint = (WingsGeoPoint) currentSenderUser.getParseObject(User.KEY_CURRENTLOCATION);
                    otherCurrLocationGeoPoint.fetchIfNeeded();
                    ParseGeoPoint otherCurrLocation = otherCurrLocationGeoPoint.getLocation();

                    double currDistance = currentLocation.distanceInKilometersTo(otherCurrLocation) * 1000;     //*1000 to get meters

                    //Add the found distance to our List<Double>
                    distancesList.add(currDistance);

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            } else {
                //Get the receivers of each BuddyRequest
                //Get the senders of each BuddyRequest
                try {
                    currentRequest.fetchIfNeeded();

                    Buddy currentReceiverBuddy = currentRequest.getReceiver();
                    currentReceiverBuddy.fetchIfNeeded();
                    ParseUser currentReceiverUser = currentReceiverBuddy.getUser();

                    //Add found user to List:
                    result.add(currentReceiverUser);


                    //Now that we have the user --> find the distance bewteen:
                    //Find the ParseGeoPoint of the otherUser:
                    WingsGeoPoint otherCurrLocationGeoPoint = (WingsGeoPoint) currentReceiverUser.getParseObject(User.KEY_CURRENTLOCATION);
                    otherCurrLocationGeoPoint.fetchIfNeeded();
                    ParseGeoPoint otherCurrLocation = otherCurrLocationGeoPoint.getLocation();

                    double currDistance = currentLocation.distanceInKilometersTo(otherCurrLocation) * 1000;     //*1000 to get meters

                    //Add the found distance to our List<Double>
                    distancesList.add(currDistance);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        //Once results are initialized --> set them in the class fields:
        if (!getSender) {         //If I got the receivers -->
            //save to the sender models:
            setPeopleSentTo(result);
            setDistanceFromSent(distancesList);
        } else {
            //save to the receiver models:
            setPeopleReceivedFrom(result);
            setDistanceFromReceivers(distancesList);
        }
    }

    private void setPeopleSentTo(List<ParseUser> result) {
        peopleSentTo.addAll(result);
        sentRequestsAdapter.notifyDataSetChanged();
    }

    private void setDistanceFromSent(List<Double> distancesList) {
        distanceFromSent.addAll(distancesList);
        sentRequestsAdapter.notifyDataSetChanged();
    }

    private void setPeopleReceivedFrom(List<ParseUser> result) {
        peopleReceivedFrom.addAll(result);
        receivedRequestAdapter.notifyDataSetChanged();
    }

    private void setDistanceFromReceivers(List<Double> distancesList) {
        distanceFromReceivers.addAll(distancesList);
        receivedRequestAdapter.notifyDataSetChanged();
    }

}