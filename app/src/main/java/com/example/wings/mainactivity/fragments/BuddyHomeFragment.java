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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.models.helpers.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * BuddyHomeFragment.java
 * Purpose:             (Needs to be edited) This HomeFragment is used once the user has confirmed being a Buddy (i.e. wants to start a BuddyTrip). There are three different modes: (1) Finding a Buddy, (2) Meeting a Buddy, (3) on a BuddyTro[.
 *                      Certain Views and functionalities will be hidden depending on the mode. Due to these different modes, the Context calling this fragment MUST specify which mode is desired to display.
 */
public class BuddyHomeFragment extends Fragment {
    private static final String TAG = "BuddyHomeFragment";
    public static final String KEY_MODE = "whatMode?";     //mode passed in
    public static final String KEY_BUDDYMEETUPID = "buddyMeetUpId";
    public static final String KEY_CLOSE_ENOUGH = "userCloseEnough?";           //used when MainActivity needs to tell this frag that the user is close enough to the meetUp/destination

    //Three possible modes, one of these choices must be passed in by the Context calling it in order for this fragment to function
    public static final String KEY_FIND_BUDDY_MODE = "modeFindBuddy";
    public static final String KEY_MEET_BUDDY_MODE = "modeMeetBuddy";
    public static final String KEY_ON_TRIP_MODE = "modeOnTrip";

    private MAFragmentsListener listener;
    private String mode;
    private String otherUserId;
    private String meetUpId;
    private boolean closeEnough;

    ParseUser currUser = ParseUser.getCurrentUser();

    //Fields for mapping:
    public static WingsMap wingsMap;
    private LatLng destination;

    //Views:
    private SupportMapFragment mapFragment;

    private RelativeLayout needBuddyButtonOverlay;
    //views in this overlay:
    private ExtendedFloatingActionButton fabGoChooseBuddyFrag;
    private ExtendedFloatingActionButton fabCancelBuddy;

    public static RelativeLayout confirmMeetingOverlay;
    //views in this overlay:
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvUserBuddyId;
    private TextView tvOtherBuddyId;
    private EditText etPin;
    private ImageButton btnExitMeetUp;
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
                closeEnough = getArguments().getBoolean(KEY_CLOSE_ENOUGH);

                Log.d(TAG, "onCreate(): meetUpId="+meetUpId);
            }

            //Must do same for buddyTrip
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
        wingsMap = new WingsMap(map, getContext(), getViewLifecycleOwner(), false);        //automatically constantly shows current location

        //Ensures to set everyone once map is done loading:
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listener.setBuddyRequestBttn(true);     //to display constant floating buddy btn from MainActivity--> displays the current buddy info: current requests, current Buddy partner, etc

        //1.) connect views:
        needBuddyButtonOverlay = view.findViewById(R.id.needBuddyButtonOverlay);
        //views in this overlay:
        fabGoChooseBuddyFrag = view.findViewById(R.id.fabChooseBuddy);
        fabCancelBuddy = view.findViewById(R.id.fabCancelBuddy);

        confirmMeetingOverlay = view.findViewById(R.id.confirmMeetupOverlay);
        //views in this overlay:
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvName = view.findViewById(R.id.tvName);
        tvUserBuddyId = view.findViewById(R.id.tvUserBuddyId);
        tvOtherBuddyId = view.findViewById(R.id.tvOtherBuddyId);
        etPin = view.findViewById(R.id.etPin);
        btnConfirmBuddyMeetup = view.findViewById(R.id.btnConfirmBuddy);
        btnExitMeetUp = view.findViewById(R.id.btnExit);

        //2.) by default, do not show meetUp and safeArrival overlays:              NOTE: needBuddyButtonOverlay may be visible as all modes need the fabCancelBuddy
        confirmMeetingOverlay.setVisibility(View.INVISIBLE);
        //confirmArrivalOverlay.setVisibility(View.INVISIBLE);
        //do same for confirmArrivalOverlay


        //3.) This cancel button can be shown on all modes + has the same functionality:
        // onClick --> reset user and go back to DefaultHomeFrag
        fabCancelBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetUser(currUser);
                listener.setBuddyRequestBttn(false);
                listener.toDefaultHomeFragment();
            }
        });
    }

    //Purpose:  In charge of setting up necessary handlers in Find buddy mode:
    private void setFindBuddyMode(){
        Log.d(TAG, "setFindBuddyMode()");
        needBuddyButtonOverlay.setVisibility(View.VISIBLE);

        //1.) Map the user's intended destination --> obtain + save it + draw route:
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

        //onClick --> go to ChooseBuddyFragment to see list of potential buddies"
        fabGoChooseBuddyFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
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
        Log.d(TAG, "setMeetBuddyMode()");

        //1.) (1) Out of the needBuddyButtonOverlay --> ONLY make the cancel button visible and (2) Show the meetUp overlay but disable the confirm button:
        fabGoChooseBuddyFrag.setVisibility(View.INVISIBLE);
        confirmMeetingOverlay.setVisibility(View.VISIBLE);
        btnConfirmBuddyMeetup.setBackgroundColor(getResources().getColor(R.color.gray, null));
        btnConfirmBuddyMeetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Cannot confirm yet! You are not close enough to your Buddy!", Toast.LENGTH_LONG).show();
            }
        });

        //2.) Setup the confirmMeetUpOverlay --> Query for and instantiate BuddyMeetUp instance and populate views
        queryBuddyMeetUp();

        //3.) Set onClick listener for the exit button:
        btnExitMeetUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmMeetingOverlay.setVisibility(View.INVISIBLE);
            }
        });

        //3.) Watch current location
        //If MainActivity told us that the user is close enough --> show the overlay
        if(closeEnough){
            //3a.) Enable the btnConfirmBuddy + disable the btnExit:
            //confirmMeetingOverlay.setVisibility(View.VISIBLE);
            btnExitMeetUp.setVisibility(View.INVISIBLE);                //make it required for the user to confirm the meetUp by not allowing to exit out

            btnConfirmBuddyMeetup.setBackgroundColor(getResources().getColor(R.color.logo_teal, null));
            btnConfirmBuddyMeetup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(etPin.getText().equals("")){
                        Toast.makeText(getContext(), "You did not enter your PIN # for confirmation!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        String attemptedPin = etPin.getText().toString();
                        Log.d(TAG, "setMeetBuddyMode(): attemptedPin = " + attemptedPin);

                        //Get the current user's pin field to check match:
                        String correctPin = currUser.getString(User.KEY_PIN).toString();
                        Log.d(TAG, "setMeetBuddyMode(): correct pin = " + correctPin);

                        if(attemptedPin.equals(correctPin)){
                            //TODO: start buddytrip here
                            Toast.makeText(getContext(), "You confirmed Meet up with your buddy!", Toast.LENGTH_LONG).show();
                            listener.stopCheckingProximity();
                        }
                        else{   //pin entered was not correct
                            Toast.makeText(getContext(), "The pin entered is incorrect!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

        else{
            listener.startCheckingProximity(15, meetUpId);
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
                    populateMeetUpViews();

                }
            }
        });
    }

    private void setBuddyMeetUp(BuddyMeetUp meetUp){
        meetUpInstance = meetUp;
    }
    private void queryOtherBuddy(String id) {
        Log.d(TAG, "in queryOtherUser(): otherUserId=" + id);
        ParseQuery<Buddy> query = ParseQuery.getQuery(Buddy.class);
        query.whereEqualTo(Buddy.KEY_OBJECT_ID, id);
        query.findInBackground(new FindCallback<Buddy>() {
            @Override
            public void done(List<Buddy> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "in queryPotentialBuddy(): success!: response=" + objects.toString());
                    if(objects.get(0) != null) {
                        Buddy otherBuddyInstance = (Buddy) objects.get(0);

                        setOtherUser(otherBuddyInstance.getUser());
                        if (otherUser != null) {
                            ParseFile imageFile = otherUser.getParseFile(User.KEY_PROFILEPICTURE);
                            if (imageFile != null) {
                                try {
                                    Glide.with(getContext()).load(imageFile.getFile()).into(ivProfilePic);
                                } catch (ParseException error) {
                                    error.printStackTrace();
                                }
                            }
                            tvName.setText(otherUser.getString(User.KEY_FIRSTNAME));

                            //Draw map from current user's current location --> other User's current location:
                            WingsGeoPoint otherCurrLocationGeoPoint = (WingsGeoPoint) otherUser.getParseObject(User.KEY_CURRENTLOCATION);
                            try {
                                otherCurrLocationGeoPoint.fetchIfNeeded();
                                wingsMap.routeFromCurrentLocation(new LatLng(otherCurrLocationGeoPoint.getLatitude(), otherCurrLocationGeoPoint.getLongitude()), true);
                            } catch (ParseException parseException) {
                                parseException.printStackTrace();
                            }


                            //NOTE: This is done here and NOT in setMeetUpMode() to ensure otherUser and BuddyMeetUp instance are initialized
                            //3.) Set an onclick listener for the floating buddy request button --> show a fragment that displays the BuddyTrip/Meeting status
                            listener.setBuddyRequestBttnOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    listener.toBuddyTripStatusFragment(otherBuddyInstance, meetUpInstance, KEY_MEET_BUDDY_MODE);
                                }
                            });
                        }
                    }
                    else{
                        Log.d(TAG, "queryOtherBuddy:  queried Buddy List was null");
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

    //Purpose:      populates corresponding data into Views of confirmMeetUpOverlay.
    private void populateMeetUpViews() {
        Log.d(TAG, "populateMeetUpViews()");
        if (meetUpInstance != null) {
            //1.) Populate Buddy id's --> Get user's buddyInstance for buddyId --> get otherBuddy's id
            Buddy currBuddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
            try {
                currBuddyInstance.fetchIfNeeded();
                String currBuddyId = currBuddyInstance.getObjectId();
                tvUserBuddyId.setText("Your ID:  " + currBuddyId);

                //See if other user is classified as the "senderBuddy" or "receiverBuddy":
                String senderBuddyId = meetUpInstance.getSenderBuddyId();
                if(senderBuddyId.equals(currBuddyId)){
                    //Then otherUser = the receiverBuddy
                    otherUserId = meetUpInstance.getReceiverBuddyId();
                    tvOtherBuddyId.setText("Their ID:  " + otherUserId);
                }
                else{
                    //Then otherUser = the senderBuddy
                    otherUserId = meetUpInstance.getSenderBuddyId();
                    tvOtherBuddyId.setText("Their ID:  " + otherUserId);
                }

                //2.) Populate other views relying on otherUser:
                queryOtherBuddy(otherUserId);                            //instantiates otherUser
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkNearEnough(){
        Log.d(TAG, "checkNearEnough(), distanceFromCurrLocation = " + wingsMap.getDistanceFromCurLocation());
        boolean result = wingsMap.isNearEnough(100);           //unsure if wingsMap will continue to update is all
        Log.d(TAG, "checkNearEnough(), result = " + result);
        return result;
    }
}