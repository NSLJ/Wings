package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.adapters.ChooseBuddyAdapter;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.home.ConfirmBuddyHomeFragment;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * ChooseBuddyFragment.java
 * Purpose:         This is when the user needs to find buddies to request --> queries and displays all possible buddies. Displays each buddy with a RecyclerView.
 *
 * Classes associated w/:  ChooseBuddyAdapter.java, relevant model classes e.g. Buddy, WingsGeoPoint
 *
 * Layout files:        fragment_choose_buddy.xml,  item_choose_buddy
 */
public class ChooseBuddyFragment extends Fragment {
    private static final String TAG = "ChooseBuddyFragment";

    private static final long radiusDistance = 1000;    //in meters  avg walking speed is: 1000 m per 10 minutes ( 1000 meters per ten minutes )
    ParseUser user;
    Buddy buddyInstance;
    ParseGeoPoint currentLocation;
    ParseGeoPoint destination;

    private MAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private RecyclerView recyclerView;
    private ChooseBuddyAdapter buddyAdapter;
    private Button btnBack;
    private TextView tvLoadStatus;

    private  List<ParseUser> usersToDisplay;        //our models
    private List<Double> distancesList;


    public ChooseBuddyFragment() {}    // Required empty public constructor

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
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_choose_buddy.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_buddy, container, false);
    }

    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Toast.makeText(getContext(), "Loading potential buddies...", Toast.LENGTH_LONG).show();
        //1.) Connect views:
        usersToDisplay = new ArrayList<>();
        distancesList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.rvReviews);
        btnBack = view.findViewById(R.id.btnBack);
        tvLoadStatus = view.findViewById(R.id.tvLoadStatus);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toCurrentHomeFragment();
            }
        });

        //2.) Create an ChooseBuddyAdapter.OnClickListener to create a ChooseBuddyAdapter:
        ChooseBuddyAdapter.OnClickListener onClickListener = new ChooseBuddyAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                //I think position = corresponds to the indexes in the List<> models but may be backwards or something:
                ParseUser userClicked = usersToDisplay.get(position);
                String objectId = userClicked.getObjectId();
                Log.d(TAG, "onClick(): position = " + position + "   objectId to send = " + objectId);

                listener.toConfirmBuddyHomeFragment(ConfirmBuddyHomeFragment.KEY_SEND_MODE, objectId);
            }

            @Override
            public void goOtherProfile(ParseUser userToShow){
                listener.toOtherProfileFragment(userToShow);

                //TODO: implement back button function
            }
        };

        buddyAdapter = new ChooseBuddyAdapter(getContext(), usersToDisplay, distancesList, onClickListener);

        //2.) Set up recycler view:
        recyclerView.setAdapter(buddyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        //3.) Initialize user, and buddyInstance fields, obtain our user's currentLocation and intendedDestination in ParseGeoPoint type
        user = ParseUser.getCurrentUser();
        buddyInstance = (Buddy) user.getParseObject(User.KEY_BUDDY);
        try {
            buddyInstance.fetchIfNeeded();

            //Initialize currentLocation of Buddy/User:
            WingsGeoPoint currentWingsGeoPoint = (WingsGeoPoint) user.getParseObject(User.KEY_CURRENTLOCATION);
            currentWingsGeoPoint.fetchIfNeeded();
            currentLocation = currentWingsGeoPoint.getLocation();

            //Initialize destination of Buddy
            WingsGeoPoint destinationWingsGeoPoint = buddyInstance.getDestination();
            destinationWingsGeoPoint.fetchIfNeeded();

            destination = destinationWingsGeoPoint.getLocation();

            //3.) Start query:
            queryPotentialUsers();

            /*
                    Query steps:
                    1.) Get all users that is a Buddy --> save it in a field
                    2.) Take out all currentLocation ids
                    3.) For every WingsGeoPoint that has one of those ids --> only get those who's currentLocation  is +- yours
                    4.)


                    1.) Get all Buddies that need a Buddy still
                    2.) Take out all WingsRoute and save it
                    3.) For every WingsRoute that has one of those ids --> get those who's currentLocation
             */
        } catch (ParseException e) {
            Log.d(TAG, "onViewCreated(): buddyInstance fetch error");
            e.printStackTrace();
        }
    }

    //Purpose:      Finds all ParseUsers that are Buddies, and whose currentLocation is within the +- radius. Assume currentLocation and destination fields are initialized
    private void queryPotentialUsers() {
        Log.d(TAG, "in queryPotentialUsers(): our currentLocation = " + currentLocation.getLatitude() + " , " + currentLocation.getLongitude());

            //1.) Get all User's who are buddies + are around this current location:
            //Plus and minus to this currentLocation to get a radius around it:
       //    ParseGeoPoint plusCurrLocation = calculatePlusLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
      //     ParseGeoPoint minusCurrLocation = calculateMinusLocation(currentLocation.getLatitude(), currentLocation.getLongitude());


            //2.) Make the query:
            ParseQuery<ParseUser> query = ParseUser.getQuery();


           // query.whereWithinGeoBox(User.KEY_CURRENTLOCATION, plusCurrLocation, minusCurrLocation);
        //query.whereWithinKilometers(User.KEY_CURRENTLOCATION, currentLocation, 20/*(radiusDistance/1000)*/);
        query.whereEqualTo(User.KEY_ISBUDDY, true);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> allUsers, ParseException e) {
                    //if no error --> success!
                    if(e == null){
                        Log.d(TAG, "in queryPotentialUsers(): allUsers="+allUsers.toString());
                        //append data!
                        //postsAdapter.addAll(currPosts);
                        //Log.i(TAG, "queryPotentialBuddies():  success! posts appended= " + posts.toString());
                        if(allUsers.size() == 0){
                            tvLoadStatus.setVisibility(View.VISIBLE);
                            tvLoadStatus.setText("No results");
                        }
                        //2a.) Take out all of the Buddies in each user so we can query using the Buddy key: intendedDestination
                        List<String> potentialBuddyIds = new ArrayList<>();
                        for(int i = 0; i < allUsers.size(); i++){
                            ParseUser currUser = allUsers.get(i);
                            Buddy currBuddy = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
                            try {
                                currBuddy.fetchIfNeeded();
                                String id = currBuddy.getObjectId();

                                //Ensure to skip over own one:
                                if(!id.equals(buddyInstance.getObjectId())) {
                                    potentialBuddyIds.add(currBuddy.getObjectId());
                                }

                                //For testing:

                                //end testing
                            } catch (ParseException parseException) {
                                Log.d(TAG, "in queryPotentialUsers(): could not fetch current Buddy while parsing through returns List<ParseUser>, error="+parseException.getMessage());
                                parseException.printStackTrace();
                            }
                        }

                        //2b.) Make another query using these Buddies to get only those that are looking for Buddies + intendedDestination is +- radius
                        Log.d(TAG, "in queryPotentialUsers(): buddyIds="+potentialBuddyIds.toString());
                        queryPotentialBuddies(potentialBuddyIds);
                    }
                    else{   //on failure!
                        //Log.e(DEBUG_TAG, "loadMoreData(): error="+ e.getMessage());
                        Log.d(TAG, "queryPotentialUsers(): failed to query for ParseUsers, error=" + e.getMessage());
                        Toast toast = Toast.makeText(getContext(), "There was an error", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                    }
                }
            });
    }

    //Assume currentLocation and destination fields are initialized, given a List of Buddies, narrows down further by querying for relative close destinations to our destination field
    //Purpose:          Called by queryPotentialUsers() --> Make another query using these Buddies given by queryPotentialUsers() to get only those that are looking for Buddies + intendedDestination is +- radius
    private void queryPotentialBuddies(List<String> potentialBuddyIds){
        Log.d(TAG, "in queryPotentialBuddies(): potentialBuddyIds=" + potentialBuddyIds.toString());
       // ParseGeoPoint plusDestination = calculatePlusLocation(destination.getLatitude(), destination.getLongitude());
       // ParseGeoPoint minusDestination = calculateMinusLocation(destination.getLatitude(), destination.getLongitude());

        ParseQuery<Buddy> query = ParseQuery.getQuery(Buddy.class);

        query.whereContainedIn(Buddy.KEY_OBJECTID, potentialBuddyIds);                                  //get all those Buddy's whose objectId field is one of these
        //query.whereWithinKilometers(Buddy.KEY_DESTINATION, destination, (radiusDistance/1000));         //get all those Buddy's who's intendedDestination is within...
        //query.whereWithinGeoBox(Buddy.KEY_DESTINATION, plusDestination, minusDestination);
        query.whereEqualTo(Buddy.KEY_HASBUDDY, false);                                                  //who's hasBuddy = false --> they need a Buddy

        query.findInBackground(new FindCallback<Buddy>() {
            @Override
            public void done(List<Buddy> response, ParseException e) {
                if(e == null){
                    Log.d(TAG, "queryPotentialBuddies(): success!:  response=" + response.toString());
                    if(response.size() == 0){
                        tvLoadStatus.setVisibility(View.VISIBLE);
                        tvLoadStatus.setText("No results");
                    }
                    //parseModel(response);       //to extract all "intendedDestination" fields and all User model objects from the List<Buddy>
                    parse(response);
                }
                else{
                    Log.d(TAG, "queryPotentialBuddies(): failed to query for buddies, error=" + e.getMessage());
                }
            }
        });
    }

    //These don't technically get the right points btw
    private ParseGeoPoint calculatePlusLocation(double latitude, double longitude){
        double coefficient = radiusDistance * 0.0000089;
        double newLat = latitude + coefficient;
        double newLong = longitude + coefficient / Math.cos(latitude * 0.018);

        return new ParseGeoPoint(newLat, newLong);
    }

    private ParseGeoPoint calculateMinusLocation(double latitude, double longitude){
        double coef = radiusDistance * 0.0000089;
        double newLat = latitude - coef;
        double newLong = longitude - coef / Math.cos(latitude * 0.018);

        return new ParseGeoPoint(newLat, newLong);
    }

/*
    //Purpose:          Calculates and populates the distances between our currentLocation and their's. Assumes the potentialBuddies field is initialized, initializes usersToDisplay
    private void parseModel(List<Buddy> buddiesToShow){
        Log.d(TAG, "in parseModel()");
        for(int i = 0; i < buddiesToShow.size(); i++){
            Buddy currBuddy = buddiesToShow.get(i);

            WingsGeoPoint destinationGeoPoint = (WingsGeoPoint) currBuddy.getParseObject(Buddy.KEY_DESTINATION);
            try {
                destinationGeoPoint.fetchIfNeeded();
                ParseGeoPoint otherDestination = (ParseGeoPoint) destinationGeoPoint.getLocation();

                //Get distance between destination field and otherDestination + add it to the list:
             //----This was wrong, was supposed to be distance from current distance
                double distance = destination.distanceInKilometersTo(otherDestination)*1000;    //* by 1000 to convert to m
                distancesList.add(distance);

            } catch (ParseException e) {
                Log.d(TAG, "in parseModel(): destinationGeoPoint couldn't be fetched");
                e.printStackTrace();
            }

            //Get user field from each:
            ParseUser otherUser = currBuddy.getUser();
            usersToDisplay.add(otherUser);
        }
        Log.d(TAG, "parseModel is done: usersToDisplay = " + usersToDisplay.toString());
        Log.d(TAG, "parseModel is done: distances = " + distancesList.toString());
        buddyAdapter.notifyDataSetChanged();
    }*/

    private void parse(List<Buddy> buddiesToShow){
        for(int i = 0; i < buddiesToShow.size(); i++){
            Buddy currBuddy = buddiesToShow.get(i);
            ParseUser otherUser = currBuddy.getUser();
            try {
                otherUser.fetchIfNeeded();
                usersToDisplay.add(otherUser);

                //Get the otherUser's currentLocation:
                WingsGeoPoint otherCurrentGeoPoint = (WingsGeoPoint) otherUser.getParseObject(User.KEY_CURRENTLOCATION);

                otherCurrentGeoPoint.fetchIfNeeded();

                ParseGeoPoint otherCurrentLocation = otherCurrentGeoPoint.getLocation();
                double distance = currentLocation.distanceInKilometersTo(otherCurrentLocation)*1000;      //* by 1000 to convert to m
                distancesList.add(distance);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "parse() is done: usersToDisplay = " + usersToDisplay.toString());
        Log.d(TAG, "parse() is done: distances = " + distancesList.toString());
        buddyAdapter.notifyDataSetChanged();
        tvLoadStatus.setVisibility(View.INVISIBLE);
    }

}