package com.example.wings.mainactivity.fragments;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.helpers.WingsMap;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.BuddyTrip;
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

import org.parceler.Parcels;

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
    public static final String KEY_DATA = "dataGivenAsParcelableObject";

    //Three possible modes, one of these choices must be passed in by the Context calling it in order for this fragment to function
    public static final String KEY_FIND_BUDDY_MODE = "modeFindBuddy";
    public static final String KEY_MEET_BUDDY_MODE = "modeMeetBuddy";
    //public static final String KEY_MEET_BUDDY_MODE_NEAR = "modeMeetBuddy - near enough";            //TODO: fix so don't need this
    public static final String KEY_ON_TRIP_MODE = "modeOnTrip";
    public static final String CONTEXT_MAIN_ACTIVITY = "fromMainActivity";          //We NEED to know who called this fragment --> different parameters are packaged into the Parcelable Object received

    private static MAFragmentsListener listener;
    //Fields to hold the data passed in:
    private static String mode;
    private static String otherUserId;
    private static String meetUpId;
    private boolean closeEnough = false;

    //General fields:
    static ParseUser currUser = ParseUser.getCurrentUser();
    static ParseUser otherUser;
    static Buddy otherBuddyInstance;
    static Buddy curBuddyInstance;
    static Buddy senderBuddy;
    static Buddy receiverBuddy;                    //used to create a BuddyTrip instance when needed

    public static Resources resources;
    public static Context context;

    //For mapping:
    public static WingsMap wingsMap;
    private static LatLng destination;

    //Views:
    private SupportMapFragment mapFragment;

    private RelativeLayout needBuddyButtonOverlay;
    //views in this overlay:
    private ExtendedFloatingActionButton fabGoChooseBuddyFrag;
    private ExtendedFloatingActionButton fabCancelBuddy;

    //Fields used for meetUp mode:
    private static BuddyMeetUp meetUpInstance;

    //Views:
    public static RelativeLayout confirmMeetingOverlay;
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvUserBuddyId;
    private TextView tvOtherBuddyId;
    private static EditText etPin;
    private static ImageButton btnExitMeetUp;
    private static Button btnConfirmBuddyMeetup;
    static boolean meetUpOverlayEnabled;



    //Fields used for onTrip mode:
    private BuddyTrip buddyTripInstance;

    private RelativeLayout confirmArrivalOverlay;
    //views in this overlay:
    private TextView tvConfirmArrivalTitle;
    private TextView tvConfirmArrivalStatus;
    private TextView tvConfirmArrivalName;
    private TextView tvConfirmArrivalOtherId;
    private TextView tvConfirmArrivalTripDestination;
    private ImageView ivConfirmArrivalProfile;
    private EditText etConfirmArrivalPin;
    private Button btnConfirmArrival;


    private RelativeLayout tripInfoOverlay;
    //views in this overlay:
    private TextView tvTripInfoTitle;
    private TextView tvTripInfoStatus;
    private TextView tvTripInfoName;
    private TextView tvTripInfoOtherId;
    private TextView tvTripInfoDestination;
    private ImageView ivTripInfoProfile;
    private ImageButton btnTripInfoExit;

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
        if (getArguments() != null) {           //TODO: this isn't runnable at all yet but I need a break
            ParcelableObject dataReceived = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_DATA));
            mode = dataReceived.getMode();
            Log.d(TAG, "onCreate():  mode =" + mode);

            if(!mode.equals("")) {

                if(mode.equals(KEY_FIND_BUDDY_MODE)){}      //nothing needs to be done as no data is passed in this mode

                else if(mode.equals(KEY_MEET_BUDDY_MODE)) {
                    Log.d(TAG, "onCreate(): mode= meetUp not near");
                    meetUpInstance = dataReceived.getBuddyMeetUp();
                    otherBuddyInstance = dataReceived.getOtherBuddy();
                    otherUser = dataReceived.getOtherParseUser();
                    curBuddyInstance = dataReceived.getCurrBuddy();
                    closeEnough = dataReceived.getBoolean();
                    Log.d(TAG, "onCreate(): mode= meetUp IS near, closeEnough=" + closeEnough);
                }
                //Must do same for buddyTrip
                else if(mode.equals(KEY_ON_TRIP_MODE)) {
                    Log.d(TAG, "onCreate(): mode= onTrip");
                    buddyTripInstance = dataReceived.getBuddyTrip();
                    otherBuddyInstance = dataReceived.getOtherBuddy();
                    otherUser = dataReceived.getOtherParseUser();
                } else {
                    Toast.makeText(getContext(), "onCreate(): error - mode did not equal any of the modes..", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCreate(): error - mode did not equal any of the modes..");
                }
            }
            else{
                Log.d(TAG, "onCreate(): mode was null");
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
                    wingsMap = new WingsMap(googleMap, getContext(), getViewLifecycleOwner(), false);        //automatically constantly shows current location

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
            });
        }
        else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listener.setBuddyRequestBttn(true);     //to display constant floating buddy btn from MainActivity--> displays the current buddy info: current requests, current Buddy partner, etc
        resources = getResources();
        context = getContext();

        //1.) connect views:
        needBuddyButtonOverlay = view.findViewById(R.id.needBuddyButtonOverlay);
        //views in this overlay:
        fabGoChooseBuddyFrag = view.findViewById(R.id.fabChooseBuddy);
        fabCancelBuddy = view.findViewById(R.id.fabCancelBuddy);

        //MeetUp overlay + views:
        confirmMeetingOverlay = view.findViewById(R.id.confirmMeetupOverlay);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvName = view.findViewById(R.id.tvTitle);
        tvUserBuddyId = view.findViewById(R.id.tvUserBuddyId);
        tvOtherBuddyId = view.findViewById(R.id.tvTripDestination);
        etPin = view.findViewById(R.id.etPin);
        btnConfirmBuddyMeetup = view.findViewById(R.id.btnConfirmBuddy);
        btnExitMeetUp = view.findViewById(R.id.btnExit);

        //ConfirmArrival overlay + views:           shown at end of BuddyTrip to end it
        confirmArrivalOverlay = view.findViewById(R.id.confirmArrivalOverlay);
        tvConfirmArrivalTitle = view.findViewById(R.id.tvConfirmArrivalTitle);
        tvConfirmArrivalStatus = view.findViewById(R.id.tvConfirmArrivalTripStatus);
        tvConfirmArrivalName = view.findViewById(R.id.tvConfirmArrivalName);
        tvConfirmArrivalOtherId= view.findViewById(R.id.tvConfirmArrivalOtherUserId);
        tvConfirmArrivalTripDestination = view.findViewById(R.id.tvConfirmArrivalTripDestination);
        ivConfirmArrivalProfile = view.findViewById(R.id.ivConfirmArrivalProfilePic);
        etConfirmArrivalPin = view.findViewById(R.id.etConfirmArrivalPin);
        btnConfirmArrival = view.findViewById(R.id.btnConfirmArrival);

        //Trip info overlay + views:            shown at beginning of BuddyTrip
        tripInfoOverlay = view.findViewById(R.id.buddyTripInfoOverlay);
        tvTripInfoTitle= view.findViewById(R.id.tvTripInfoTitle);
        tvTripInfoStatus= view.findViewById(R.id.tvTripInfoTripStatus);
        tvTripInfoName = view.findViewById(R.id.tvTripInfoName);
        tvTripInfoOtherId = view.findViewById(R.id.tvTripInfoOtherUserId);
        tvTripInfoDestination = view.findViewById(R.id.tvTripInfoTripDestination);
        ivTripInfoProfile = view.findViewById(R.id.ivTripInfoProfilePic);
        btnTripInfoExit = view.findViewById(R.id.btnTripInfoExit);

        //2.) by default, do not show meetUp and safeArrival overlays:              NOTE: needBuddyButtonOverlay may be visible as all modes need the fabCancelBuddy
        confirmMeetingOverlay.setVisibility(View.INVISIBLE);
        confirmArrivalOverlay.setVisibility(View.INVISIBLE);
        tripInfoOverlay.setVisibility(View.INVISIBLE);
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
        disableMeetUpOverlay();

        //2.) Setup the confirmMeetUpOverlay --> Query for and instantiate BuddyMeetUp instance and populate views
        //queryAndPopulateMeetBuddyMode();
        populateMeetUpViews();              //new edit: assume all fields are instantiated as they should now be passed in

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
            enableMeetUpOverlay();
        }
        else{
            listener.startCheckingProximity(15, meetUpInstance);
        }

    }

    //Purpose:  In charge of setting up necessary handlers in on trip mode:
    private void setOnTripMode(){
        Log.d(TAG, "setOnTripMode()");

        //1.) (1) Out of the needBuddyButtonOverlay --> ONLY make the cancel button visible and (2) Show the meetUp overlay but disable the confirm button:
        fabGoChooseBuddyFrag.setVisibility(View.INVISIBLE);
        tripInfoOverlay.setVisibility(View.VISIBLE);

        //2.) Set onClick listener for the exit button:
        btnExitMeetUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmMeetingOverlay.setVisibility(View.INVISIBLE);
            }
        });

        //3.) Set onClick listener for when confirm safe arrival:
        btnConfirmArrival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(etConfirmArrivalPin.getText().toString().equals("") || etConfirmArrivalPin.getText().toString().length() != 4){
                        Toast.makeText(getContext(), "You did not enter your PIN # for confirmation!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        String attemptedPin = etConfirmArrivalPin.getText().toString();
                        Log.d(TAG, "setMeetBuddyMode(): attemptedPin = " + attemptedPin);

                        //Get the current user's pin field to check match:
                        String correctPin = currUser.getString(User.KEY_PIN).toString();
                        Log.d(TAG, "setMeetBuddyMode(): correct pin = " + correctPin);

                        if(attemptedPin.equals(correctPin)){
                            //TODO: start buddytrip here
                            Toast.makeText(getContext(), "You confirmed your safe arrival!", Toast.LENGTH_LONG).show();
                            //listener.stopCheckingProximity();
                        }
                        else{   //pin entered was not correct
                            Toast.makeText(getContext(), "The pin entered is incorrect!", Toast.LENGTH_LONG).show();
                        }
                    }
            }
        });

        //listener.startCheckingProximity(15, meetUpId);
    }









    //------------------------Meet up mode helper methods------------------------------------------------------------------------------

    //Purpose:      Finds the BuddyMeetUp that corresponds to the field, "meetUpId", then updates the meetUpInstance field.
    private void queryAndPopulateMeetBuddyMode(){
        Log.d(TAG, "in queryBuddyMeetUp(): meetUpId=" + meetUpId);

        //1.) Find the BuddyMeetUp instance based on the given meetUpId:
        ParseQuery<BuddyMeetUp> query = ParseQuery.getQuery(BuddyMeetUp.class);
        query.whereEqualTo(BuddyMeetUp.KEY_OBJECT_ID, meetUpId);
        query.findInBackground(new FindCallback<BuddyMeetUp>() {
            @Override
            public void done(List<BuddyMeetUp> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "in queryBuddyMeetUp(): success!: response=" + objects.toString());
                    setBuddyMeetUp(objects.get(0));     //initalizes the meetUpInstance field:

                    //2.) Find the Buddy instance based on the given otherUserId:
                    queryOtherBuddy(otherUserId);                                   //NOTE:  in charge of populating the views after its done querying
                }
            }
        });
    }

    private void setBuddyMeetUp(BuddyMeetUp meetUp){
        meetUpInstance = meetUp;
    }
    private void queryOtherBuddy(String id) {
        Log.d(TAG, "in queryOtherUser(): otherUserId=" + id);

        //1.) Query to find the Buddy instance corresponding to the otherBuddy, given the otherUserId
        ParseQuery<Buddy> query = ParseQuery.getQuery(Buddy.class);
        query.whereEqualTo(Buddy.KEY_OBJECT_ID, id);
        query.findInBackground(new FindCallback<Buddy>() {
            @Override
            public void done(List<Buddy> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "in queryPotentialBuddy(): success!: response=" + objects.toString());

                    if(objects.get(0) != null) {
                        //2.) Set the fields:
                        setOtherBuddy((Buddy) objects.get(0));              //sets otherBuddyInstance and otherUser
                        setOtherUser(otherBuddyInstance.getUser());
                        if (otherUser != null) {

                            //3.) Now we have all the info needed --> populate the views!
                            populateMeetUpViews();
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

    //Purpose:      populates corresponding data into Views of confirmMeetUpOverlay.
    private void populateMeetUpViews() {
        Log.d(TAG, "populateMeetUpViews()");
        if (meetUpInstance != null && otherBuddyInstance != null && otherUser != null) {
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

                    //Initalize sender/receiver Buddy instances --> used to create a BuddyTrip instance later:
                    senderBuddy = currBuddyInstance;
                    receiverBuddy = otherBuddyInstance;
                }
                else{
                    //Then otherUser = the senderBuddy
                    otherUserId = meetUpInstance.getSenderBuddyId();
                    tvOtherBuddyId.setText("Their ID:  " + otherUserId);

                    senderBuddy = otherBuddyInstance;
                    receiverBuddy = currBuddyInstance;
                }

                //2.) Populate other views relying on otherUser:
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
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    private void setOtherBuddy(Buddy buddy){
        otherBuddyInstance = buddy;
    }
    private void setOtherUser(ParseUser other){
        otherUser = other;
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

    //---------------------------------------------------------end all meetUp mode helper methods-----------------------------------------------------------
    private static void onConfirmMeetUp(){
        Toast.makeText(context, "You confirmed Meet up with your buddy!", Toast.LENGTH_LONG).show();
        listener.stopCheckingProximity();

        WingsGeoPoint currLocation = (WingsGeoPoint) currUser.getParseObject(User.KEY_CURRENTLOCATION);
        try {
            currLocation.fetchIfNeeded();
            BuddyTrip buddyTrip = new BuddyTrip(senderBuddy, receiverBuddy, currLocation, meetUpInstance.getDestination());
            buddyTrip.save();

            ParcelableObject sendData = new ParcelableObject();
            sendData.setMode(KEY_ON_TRIP_MODE);
            sendData.setBuddyTrip(buddyTrip);
            sendData.setOtherParseUser(otherUser);
            sendData.setOtherBuddy(otherBuddyInstance);

            //listener.toBuddyHomeFragment(KEY_ON_TRIP_MODE, buddyTrip, otherUser, otherBuddyInstance);
            listener.toBuddyHomeFragment(sendData);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void enableMeetUpOverlay(){
        meetUpOverlayEnabled = true;
        btnExitMeetUp.setVisibility(View.INVISIBLE);                //make it required for the user to confirm the meetUp by not allowing to exit out

        btnConfirmBuddyMeetup.setBackgroundColor(resources.getColor(R.color.logo_teal, null));
        btnConfirmBuddyMeetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etPin.getText().toString().length() != 4){
                    Toast.makeText(context, "You did not enter your PIN # for confirmation!", Toast.LENGTH_LONG).show();
                }
                else{
                    String attemptedPin = etPin.getText().toString();
                    Log.d(TAG, "setMeetBuddyMode(): attemptedPin = " + attemptedPin);

                    //Get the current user's pin field to check match:
                    String correctPin = currUser.getString(User.KEY_PIN);

                    Log.d(TAG, "setMeetBuddyMode(): correct pin = " + correctPin);
                    Log.d(TAG, "enableMeetUpOverlay():  compare pins: " + attemptedPin.compareTo(correctPin));
                    if(attemptedPin.equals(correctPin)){            //this won't work for some reason --> correctPin = "####" and attemptedPin = #### ?
                        //Create a BuddyTrip:
                        onConfirmMeetUp();
                    }
                    else{   //pin entered was not correct
                        Toast.makeText(context, "The pin entered is incorrect!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    public static void disableMeetUpOverlay(){
        meetUpOverlayEnabled = false;
        btnExitMeetUp.setVisibility(View.VISIBLE);
        btnConfirmBuddyMeetup.setBackgroundColor(resources.getColor(R.color.gray, null));
        btnConfirmBuddyMeetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Cannot confirm yet! You are not close enough to your Buddy!", Toast.LENGTH_LONG).show();
            }
        });
    }
    public static boolean checkNearEnough(){
        Log.d(TAG, "checkNearEnough(), distanceFromCurrLocation = " + wingsMap.getDistanceFromCurLocation());
        boolean result = wingsMap.isNearEnough(100);           //unsure if wingsMap will continue to update is all
        Log.d(TAG, "checkNearEnough(), result = " + result );//+ "      meetUpOverlayEnabled =" + meetUpOverlayEnabled);

        /*if(result && !meetUpOverlayEnabled){        //if we are close enough and overlay is not yet enabled --> enable it
            enableMeetUpOverlay();
        }
        else if(!result && meetUpOverlayEnabled){
            disableMeetUpOverlay();
        }*/
        return result;
    }
}