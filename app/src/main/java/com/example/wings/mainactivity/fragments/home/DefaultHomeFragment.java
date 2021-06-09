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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.helpers.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

/**
 * DefaultHomeFragment.java
 * Purpose:         This is the HomeFragment that is called when the user is NOT a Buddy yet and may enter a destination in the search bar to become one! Map functionalities: mapping current location, drawing route from current location to queried destination.
 *                  In charge of confirming a destination to start being a Buddy! --> navigates to BuddyHomeFragment to display the buddy requesting buttons needed.
 *
 *                  The simplest out of the three HomeFrags --> only 1 overlay
 *
 * Layout file:     fragment_default_home.xml
 */
public class DefaultHomeFragment extends Fragment {
    private static final String TAG = "DefaultHomeFragment";

    private MAFragmentsListener listener;
    ParseUser currUser = ParseUser.getCurrentUser();

    //Fields for mapping:
    private WingsMap wingsMap;
    private LatLng queriedDestination;

    //Views:
    private SupportMapFragment mapFragment;
    private EditText etSearchBar;
    private ImageButton btnSearch;
    private RelativeLayout confirmDestinationOverlay;

    private TextView displayDestination;
    private ImageView ivAcceptBtn;
    private ImageView ivRejectBtn;

    View mainView;
    public DefaultHomeFragment() {}

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_default_home, container, false);
        Log.d(TAG, "onCreateView(): mainView == null: " + (mainView==null));
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
                    Log.d(TAG, "onMapReady(): mainView == null: " + (mainView==null));
                    wingsMap = new WingsMap(googleMap, getContext(), getViewLifecycleOwner(), false, false);   //automatically constantly shows current location
                }
            });
        }
        else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated(): mainView == null: " + (mainView==null));
        setMapFragment();

        //1.) Connect Views:
        btnSearch = view.findViewById(R.id.btnSearch);
        etSearchBar = view.findViewById(R.id.etSearchBar);
        confirmDestinationOverlay = view.findViewById(R.id.chooseDestinationOverlay);
        displayDestination = view.findViewById(R.id.tvNotify);
        ivAcceptBtn = view.findViewById(R.id.ivAccept);
        ivRejectBtn = view.findViewById(R.id.ivReject);

        //By default: do no display the overlay:
        confirmDestinationOverlay.setVisibility(View.INVISIBLE);

        //2.) onClick() --> Search + draw a route to a destination + display confirmDestinationOverlay:
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnSearch clicked!");
                //1.) Maps and draws route to the destination in the etSearchBar:

                //1a.) Get the destination text:
                String destinationTxt = etSearchBar.getText().toString();
                if(destinationTxt.equals("")){
                    Toast.makeText(getContext(), "You didn't enter anything!", Toast.LENGTH_SHORT).show();
                }
                else {
                    //1b.) Check if there are possible addresses to map to:
                    List<Address> possibleAddresses = wingsMap.getPossibleAddresses(etSearchBar.getText().toString());
                    if(possibleAddresses != null || possibleAddresses.size() == 0) {
                        Log.d(TAG, "btnSearch clicked: possibleAddresses is NOT null, possibleAddresses="+possibleAddresses.toString());
                        //1c.) Get + draw the route:
                        queriedDestination = wingsMap.routeFromCurrentLocation(destinationTxt, true, "Your destination");
                        Log.d(TAG, "queriedDestination successfully initialized");

                        //2.) Display the confirmDestinationOverlay:
                        displayDestination.setText("Destination: " + destinationTxt);
                        confirmDestinationOverlay.setVisibility(View.VISIBLE);
                    }
                    else{
                        Toast.makeText(getContext(), "There isn't an address that can be mapped to that!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //3.) Set the handlers inside the confirmDestinationOverlay:
        //on accept --> (1) create Buddy instance for the current user, (2) go to BuddyHomeFragment, passing in the mode = KEY_FIND_BUDDY, so it knows what to make visible and not visible
        ivAcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ivAcceptBtn onClick()");

                try {
                    //1.) Create a new Buddy instance and link it to this user:
                    Buddy buddy = new Buddy(currUser, new WingsGeoPoint(currUser, queriedDestination.latitude, queriedDestination.longitude));
                    buddy.save();/*InBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Log.d(TAG, "ivAcceptBtn onClick(): no Error saving Buddy!");
                        }
                        else{
                            Log.d(TAG, "ivAcceptBtn onClick(): Error saving Buddy!");
                        }
                    }
                });*/

                    //link new buddy instance to user:
                    currUser.put(User.KEY_ISBUDDY, true);
                    currUser.put(User.KEY_BUDDY, buddy);
                    currUser.save();/*InBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Log.d(TAG, "ivAcceptBtn onClick(): successfully saved user's isBuddy and Buddy!");
                        }
                        else{
                            Log.d(TAG, "ivAcceptBtn onClick(): successfully saved user's isBuddy and Buddy!");
                        }
                    }
                });*/

                    //2.) Go to BuddyHomeFrag:
                    ParcelableObject sendData = new ParcelableObject(BuddyHomeFragment.KEY_FIND_BUDDY_MODE);
                    listener.toBuddyHomeFragment(sendData);
                }catch(ParseException e){
                    Log.d(TAG, "acceptDestination button cllicked: error="+ e.getLocalizedMessage());
                }
            }
        });

        //on reject --> close out overlay + stop drawing route:
        ivRejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ivRejectBtn: onClick()");

                try {
                    //1.) Stop displaying overlay:
                    confirmDestinationOverlay.setVisibility(View.INVISIBLE);

                    //2.) Reset the queriedDestination & destinationStr in database
                    WingsGeoPoint parseQueriedDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_QUERIEDDESTINATION);
                    parseQueriedDestination.reset();
                    wingsMap.setDestination(null);              //so wingsMap knows to stop mapping
                    currUser.put(User.KEY_DESTINATIONSTR, "default");
                    currUser.save();/*InBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Log.d(TAG, "ivRejectBtn: onClick() - user reset successfully");
                        }
                        else{
                            Log.d(TAG, "ivRejectBtn: onClick() - user reset failed, error-" + e.getLocalizedMessage());
                        }
                    }
                });*/

                    //3.) Set the map back to normal
                    wingsMap.removeRoute();
                }catch(ParseException e){
                    Log.e(TAG, "reject destination bttn clicked: error = " +e.getLocalizedMessage());
                }
            }
        });
    }
}