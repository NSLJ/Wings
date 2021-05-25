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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.Buddy;
import com.example.wings.models.BuddyMeetUp;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * BuddyHomeFragment.java
 * Purpose:             This HomeFragment is used once the user has confirmed being a Buddy (i.e. wants to start a BuddyTrip). There are three different modes: (1) Finding a Buddy, (2) Meeting a Buddy, (3) on a BuddyTro[.
 *                      Certain Views and functionalities will be hidden depending on the mode. Due to these different modes, the Context calling this fragment MUST specify which mode is desired to display.
 */
public class BuddyHomeFragment extends Fragment {
    private static final String TAG = "BuddyHomeFragment";
    public static final String KEY_MODE = "whatMode?";     //mode passed in
    public static final String KEY_BUDDYMEETUPID = "buddyMeetUpId";

    //Three possible modes, one of these choices must be passed in by the Context calling it in order for this fragment to function
    public static final String KEY_FIND_BUDDY_MODE = "modeFindBuddy";
    public static final String KEY_MEET_BUDDY_MODE = "modeMeetBuddy";
    public static final String KEY_ON_TRIP_MODE = "modeOnTrip";

    private MAFragmentsListener listener;
    private String mode;
    private String otherUserId;
    private String meetUpId;

    ParseUser currUser = ParseUser.getCurrentUser();

    //Fields for mapping:
    private WingsMap wingsMap;
    private LatLng destination;

    //Views:
    private SupportMapFragment mapFragment;

    private RelativeLayout needBuddyButtonOverlay;
    //views in this overlay:
    private ExtendedFloatingActionButton fabGoChooseBuddyFrag;
    private ExtendedFloatingActionButton fabCancelBuddy;

    private RelativeLayout confirmMeetingOverlay;
    //views in this overlay:
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvUserBuddyId;
    private TextView tvOtherBuddyId;
    private EditText etPin;
    private Button btnConfirmBuddyMeetup;
    private BuddyMeetUp meetUpInstance;
    private ParseUser otherUser;


    private RelativeLayout confirmArrivalOverlay;       //not yet implemented in layout

    public BuddyHomeFragment() {}

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
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            mode = getArguments().getString(KEY_MODE);
            Log.d(TAG, "onCreate():  mode =" + mode);

            //If on meet buddy mode --> expect to receive extra data (buddyMeetUpId)
            if(mode.equals(KEY_MEET_BUDDY_MODE)){
                meetUpId = getArguments().getString(KEY_BUDDYMEETUPID);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View mainView = inflater.inflate(R.layout.fragment_buddy_home, container, false);
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
                    loadMap(googleMap);     //draws route to destination
                }
            });
        }
        else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }
    //Purpose:          Initializes our "map" field, starts continuously checking for location updates
    private void loadMap(GoogleMap map) {
        Log.d(TAG, "in loadMap():");
        wingsMap = new WingsMap(map, getContext(), getViewLifecycleOwner());        //automatically constantly shows current location

        //1.) obtain the user's intended destination + save it + draw route:
        Buddy currBuddy = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
        try {
            currBuddy.fetchIfNeeded();
            WingsGeoPoint intendedDestination = currBuddy.getDestination();

            //2.) Save "destination" field to intendedDestination + route to it
            destination = new LatLng(intendedDestination.getLatitude(), intendedDestination.getLongitude());
            wingsMap.routeFromCurrentLocation(destination, true);


        } catch (ParseException e) {
            Log.d(TAG, "loadMap(): error fetching currBuddy, error = " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listener.setBuddyRequestBttn(true);     //to display constant floating buddy btn from MainActivity--> displays the current buddy info: current requests, current Buddy partner, etc

        //1.) connect views:
        needBuddyButtonOverlay = view.findViewById(R.id.needBuddyButtonOverlay);
        //views in this overlay:
        fabGoChooseBuddyFrag = view.findViewById(R.id.fabChooseBuddy);
        fabCancelBuddy = view.findViewById(R.id.fabCancelBuddy);

        confirmMeetingOverlay = view.findViewById(R.id.confirmMeetingOverlay);
        //views in this overlay:
        ivProfilePic = view.findViewById(R.id.ivConfirmMeetingPic);
        tvName = view.findViewById(R.id.tvConfirmMeetingName);
        tvUserBuddyId = view.findViewById(R.id.tvUserBuddyId);
        tvOtherBuddyId = view.findViewById(R.id.tvOtherBuddyId);
        etPin = view.findViewById(R.id.etPin);
        btnConfirmBuddyMeetup = view.findViewById(R.id.btnConfirmBuddy);

        //2.) by default, show none of the overlays:
        needBuddyButtonOverlay.setVisibility(View.INVISIBLE);
        confirmMeetingOverlay.setVisibility(View.INVISIBLE);
        //do same for confirmArrivalOverlay

        if(mode.equals(KEY_FIND_BUDDY_MODE)){
            setFindBuddyMode();
        }
        else if(mode.equals(KEY_MEET_BUDDY_MODE)){
            setMeetBuddyMode();
        }
        else if(mode.equals(KEY_ON_TRIP_MODE)){
            setOnTripMode();
        }
        else{
            Toast.makeText(getContext(), "Error! BuddyHomeFragment received a mode that wasn't valid!", Toast.LENGTH_SHORT).show();
        }
    }

    //Purpose:  In charge of setting up necessary handlers in Find buddy mode:
    private void setFindBuddyMode(){
        needBuddyButtonOverlay.setVisibility(View.VISIBLE);

        //onClick --> go to ChooseBuddyFragment to see list of potential buddies"
        fabGoChooseBuddyFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
            }
        });

        //onClick --> reset user and go back to DefaultHomeFrag
        fabCancelBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetUser(currUser);
                listener.setBuddyRequestBttn(false);
                listener.toDefaultHomeFragment();
            }
        });

        //onClick --> Go to Buddy Request screen to display all received and sent requests:
        listener.setBuddyRequestBttnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toUserBuddyRequestFragment();
            }
        });
    }


    //Purpose:  In charge of setting up necessary handlers in meeting buddy mode. Watches current location until it become near enough to invoke the confirmMeetUpOverlay. Then, populates data into views. Assumes the user has a Buddy and BuddyMeetUp instantiated.
    private void setMeetBuddyMode(){


        //1.) Setup the confirmMeetUpOverlay --> Query for and instantiate BuddyMeetUp instance and populate views
        queryBuddyMeetUp();
        populateMeetUpViews();
        btnConfirmBuddyMeetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etPin.getText().equals("")){
                    Toast.makeText(getContext(), "You did not enter your PIN # for confirmation!", Toast.LENGTH_LONG).show();
                }
                else{
                    String attemptedPin = etPin.getText().toString();

                    //Get the current user's pin field to check match:
                    String correctPin = currUser.getString(User.KEY_PIN);

                    if(attemptedPin.equals(correctPin)){
                        //TODO: start buddytrip here
                        confirmArrivalOverlay.setVisibility(View.INVISIBLE);
                    }
                    else{   //pin entered was not correct
                        Toast.makeText(getContext(), "The pin entered is incorrect!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //2.) Watch current location
        //TODO: This needs to be done in some background loop
        if (wingsMap.isNearEnough(15)) {
            confirmMeetingOverlay.setVisibility(View.VISIBLE);
        }

    }

    //Purpose:  In charge of setting up necessary handlers in on trip mode:
    private void setOnTripMode(){

    }

    private void resetUser(ParseUser userToErase) {
        Log.d(TAG, "in resetUser()");

        //1.) Reset Buddy field, queriedDestination, isBuddy=false, and defaulted destinationStr:
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
        userToErase.put(User.KEY_ISBUDDY, false);
        userToErase.put(User.KEY_DESTINATIONSTR, "default");

        //Save it
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
    }


    //------------------------Meet up mode helper methods------------------------------------------------------------------------------

    //Purpose:      Finds the BuddyMeetUp that corresponds to the field, "meetUpId", then updates the meetUpInstance field.
    private void queryBuddyMeetUp(){
        Log.d(TAG, "in queryBuddyMeetUp(): meetUpId=" + meetUpId);

        ParseQuery<BuddyMeetUp> query = ParseQuery.getQuery(BuddyMeetUp.class);
        query.whereEqualTo(BuddyMeetUp.KEY_OBJECT_ID, meetUpId);
        query.findInBackground(new FindCallback<BuddyMeetUp>() {
            @Override
            public void done(List<BuddyMeetUp> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "in queryBuddyMeetUp(): success!: response=" + objects.toString());
                    setBuddyMeetUp(objects.get(0));
                }
            }
        });
    }
    private void setBuddyMeetUp(BuddyMeetUp meetUp){
        meetUpInstance = meetUp;
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

    //Purpose:      populates corresponding data into Views of confirmMeetUpOverlay.
    private void populateMeetUpViews() {
        Log.d(TAG, "populateMeetUpViews()");
        if (meetUpInstance != null) {
            //1.) Populate Buddy id's --> Get user's buddyInstance for buddyId --> get otherBuddy's id
            Buddy currBuddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
            try {
                currBuddyInstance.fetchIfNeeded();
                String currBuddyId = currBuddyInstance.getObjectId();
                tvUserBuddyId.setText(currBuddyId);

                //See if other user is classified as the "senderBuddy" or "receiverBuddy":
                String senderBuddyId = meetUpInstance.getSenderBuddyId();
                if(senderBuddyId.equals(currBuddyId)){
                    //Then otherUser = the receiverBuddy
                    otherUserId = meetUpInstance.getReceiverBuddyId();
                    tvOtherBuddyId.setText(otherUserId);
                }
                else{
                    //Then otherUser = the senderBuddy
                    otherUserId = meetUpInstance.getSenderBuddyId();
                    tvOtherBuddyId.setText(otherUserId);
                }

                //2.) Populate other views relying on otherUser:
                queryOtherUser(otherUserId);                            //instantiates otherUser
                if(otherUser != null){
                    ParseFile imageFile = otherUser.getParseFile(User.KEY_PROFILEPICTURE);
                    if (imageFile != null) {
                        try {
                            Glide.with(getContext()).load(imageFile.getFile()).into(ivProfilePic);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    tvName.setText(otherUser.getString(User.KEY_FIRSTNAME));
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}