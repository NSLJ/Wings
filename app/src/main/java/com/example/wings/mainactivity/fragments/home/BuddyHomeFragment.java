package com.example.wings.mainactivity.fragments.home;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
import com.example.wings.mainactivity.fragments.BuddyTripStatusFragment;
import com.example.wings.mainactivity.fragments.dialogs.MakeRatingDialog;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

/**
 * BuddyHomeFragment.java
 * Purpose:             This HomeFragment is used once the user has confirmed being a Buddy (i.e. wants to start a BuddyTrip). There are three different modes: (1) Finding a Buddy, (2) Meeting a Buddy, (3) on a BuddyTrip.
 *                      Certain Views and functionalities will be hidden depending on the mode. Due to these different modes, the Context calling this fragment MUST specify which mode is desired to display.
 *
 * Layout file:     fragment_buddy_home.xml             NOTE:  There are 4 different labeled overlays in this file, one is toggled with visibility at a time depending on the received mode!
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
    public static FragmentManager fragManager;
    public static BuddyHomeFragment thisFragInstance;

    //For mapping:
    public static WingsMap wingsMap;
    private static LatLng destination;

    //Views:
    private SupportMapFragment mapFragment;

    private RelativeLayout needBuddyButtonOverlay;
    //views in this overlay:
    private ExtendedFloatingActionButton fabGoChooseBuddyFrag;
    private static ExtendedFloatingActionButton fabCancelBuddy;

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
        if (getArguments() != null) {
            ParcelableObject dataReceived = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_DATA));
            mode = dataReceived.getMode();
            Log.d(TAG, "onCreate():  mode =" + mode);

            if(!mode.equals("")) {

                if(mode.equals(KEY_FIND_BUDDY_MODE)){}      //nothing needs to be done as no data is passed in this mode

                else if(mode.equals(KEY_MEET_BUDDY_MODE) || (mode.equals(KEY_ON_TRIP_MODE))) {
                    otherBuddyInstance = dataReceived.getOtherBuddy();
                    otherUser = dataReceived.getOtherParseUser();
                    curBuddyInstance = dataReceived.getCurrBuddy();
                    closeEnough = dataReceived.getBoolean();
                    Log.d(TAG, "onCreate(): closeEnough=" + closeEnough);
                    Log.d(TAG, "onCreate(): otherBuddy=" + (otherBuddyInstance!=null));
                    Log.d(TAG, "onCreate(): otherUser=" + (otherUser !=null));
                    Log.d(TAG, "onCreate(): curBuddy=" + (curBuddyInstance!= null));

                    if(mode.equals(KEY_MEET_BUDDY_MODE)){
                        Log.d(TAG, "onCreate(): mode= onMeetUp");
                        meetUpInstance = dataReceived.getBuddyMeetUp();
                    }
                    else{
                        Log.d(TAG, "onCreate(): mode= onTrip");
                        buddyTripInstance = dataReceived.getBuddyTrip();
                    }
                }
                else {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_buddy_home, container, false);
        return mainView;
    }

    private void setMapFragment(){
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(getView() == null){
            Log.d(TAG, "setMapFragment(): view = null");
        }
        if (mapFragment != null) {
            Log.d(TAG, "setMapFragment(): view != null and mapFragment != null");
            mapFragment.getMapAsync(new OnMapReadyCallback() {      //initializes maps system and view:
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if(mode.equals(KEY_ON_TRIP_MODE)) {
                        wingsMap = new WingsMap(googleMap, getContext(), getViewLifecycleOwner(), false, true);        //automatically constantly shows current location
                    }
                    else {
                        wingsMap = new WingsMap(googleMap, getContext(), getViewLifecycleOwner(), false, false);        //automatically constantly shows current location
                    }
                    //Ensures to set mode once map is done loading:
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
        setMapFragment();
        resources = getResources();                         //Used for statically calling from CheckProximityWorker
        context = getContext();
        fragManager = getFragmentManager();
        thisFragInstance = this;

        //1.) connect views:
        needBuddyButtonOverlay = view.findViewById(R.id.needBuddyButtonOverlay);
        //views in this overlay:
        fabGoChooseBuddyFrag = view.findViewById(R.id.fabChooseBuddy);
        fabCancelBuddy = view.findViewById(R.id.fabCancelBuddy);

        //MeetUp overlay + views:
        confirmMeetingOverlay = view.findViewById(R.id.confirmMeetupOverlay);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvName = view.findViewById(R.id.tvName);
        tvUserBuddyId = view.findViewById(R.id.tvUserBuddyId);
        tvOtherBuddyId = view.findViewById(R.id.tvOtherBuddyId);
        etPin = view.findViewById(R.id.etPin);
        btnConfirmBuddyMeetup = view.findViewById(R.id.btnConfirmBuddy);
        btnExitMeetUp = view.findViewById(R.id.btnOptionsExit);

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
        fabCancelBuddy.setOnClickListener(new View.OnClickListener() {              //TODO: figure out how to reset the otherUser from here + deleting all relevant ParseObjects (e.g. if they were on BuddyMeetUp vs. BuddyTrip)
            @Override
            public void onClick(View v) {
                Log.d(TAG, "cancelBuddyBtn onClick invoked!");
                resetUser(currUser);
                if(curBuddyInstance != null){
                    curBuddyInstance.reset();
                }
                listener.setBuddyRequestBttn(false);
                listener.toDefaultHomeFragment();
            }
        });

        listener.setBuddyRequestBttn(true);     //to display constant floating buddy btn from MainActivity--> displays the current buddy info: current requests, current Buddy partner, etc
    }

    //----------------------------------------------General Helper methods for everyone----------------------------------------------------------------------
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


    //Purpose:  In charge of setting up necessary handlers in Find buddy mode:
    private void setFindBuddyMode(){
        currUser = ParseUser.getCurrentUser();
        Log.d(TAG, "setFindBuddyMode(): currUser = " + currUser.getObjectId());
        needBuddyButtonOverlay.setVisibility(View.VISIBLE);

        //1.) Map the user's intended destination --> obtain + save it + draw route:
        Buddy currBuddy = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
        try {
            currBuddy.fetchIfNeeded();
            Log.d(TAG, "setFindBuddyMode(): currBuddy = " + currBuddy.getObjectId());
            WingsGeoPoint intendedDestination = currBuddy.getDestination();

            //2.) Save "destination" field to intendedDestination + route to it
            destination = new LatLng(intendedDestination.getLatitude(), intendedDestination.getLongitude());
            wingsMap.routeFromCurrentLocation(destination, true, "Your destination");

        } catch (ParseException e) {
            Log.d(TAG, "loadMap(): error fetching currBuddy, error = " + e.getLocalizedMessage());
        }

        //2.) Set onClickListeners:
        //2a.) Choose Buddy bttn = onClick --> go to ChooseBuddyFragment to see list of potential buddies"
        fabGoChooseBuddyFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
            }
        });

        //2b.) Floating Buddy Request button = onClick --> Go to Buddy Request screen to display all received and sent requests:
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
        currUser = ParseUser.getCurrentUser();
        //1.) (1) Out of the needBuddyButtonOverlay --> ONLY make the cancel button visible and (2) Show the meetUp overlay but disable the confirm button:
        fabGoChooseBuddyFrag.setVisibility(View.INVISIBLE);
        fabCancelBuddy.setText("Cancel the Meet Up");
        confirmMeetingOverlay.setVisibility(View.VISIBLE);
        disableMeetUpOverlay();

        //2.) Populate views assuming all fields are instantiated from data passed in
        populateMeetUpViews();

        //3.) Set onClickListeners:
            //3a.) Exit bttn:
        btnExitMeetUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmMeetingOverlay.setVisibility(View.INVISIBLE);
            }
        });
            //3b.) Floating buddy button --> show info about the trip
        listener.setBuddyRequestBttnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toBuddyTripStatusFragment(BuddyTripStatusFragment.MEET_BUDDY_MODE, otherBuddyInstance, meetUpInstance.getDestination());
            }
        });

        //4.) Watch current location
        //We were told that the user is close enough --> show the overlay
        if(closeEnough){
            enableMeetUpOverlay();
        }
        else{
            listener.startCheckingProximity(100, meetUpInstance);               //tells MainActivity to start CheckProximityWorker --> responds whether currUser is close enough to meeting up with their Buddy
        }
    }


    //Purpose:  In charge of setting up necessary handlers in on trip mode:
    private void setOnTripMode(){
        Log.d(TAG, "setOnTripMode()");
        currUser = ParseUser.getCurrentUser();
        //1.) (1) Out of the needBuddyButtonOverlay --> ONLY make the cancel buddy button visible
        fabGoChooseBuddyFrag.setVisibility(View.INVISIBLE);
        fabCancelBuddy.setText("Cancel the Trip");

        //2.) Populate views assuming all fields are instantiated from data passed in
        populateAllTripViews();

        //3.) Watch current location
        //We were told that the user is close enough --> show the confirmArrivaloverlay
        if(closeEnough){
            confirmArrivalOverlay.setVisibility(View.VISIBLE);
        }
        else{           //Otherwise --> show the tripInfoOverlay (just displays overall significant info at beginning of BuddyTrip)
            tripInfoOverlay.setVisibility(View.VISIBLE);
            listener.startCheckingProximity(100, meetUpInstance);               //tells MainActivity to start CheckProximityWorker --> responds whether currUser is close enough to meeting up with their Buddy
        }


        //4.) Set all onClick listener's need for both trip overlays:
        // 4a.) for tripInfoOverlay:  exit bttn
        btnTripInfoExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripInfoOverlay.setVisibility(View.INVISIBLE);
            }
        });

        //4b.) for confirmArrivalOverlay:  confirm arrival bttn:
        btnConfirmArrival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String attemptedPin = etConfirmArrivalPin.getText().toString();
                Log.d(TAG, "setMeetBuddyMode(): attemptedPin = " + attemptedPin);
                if(attemptedPin.length() != 4){
                        Toast.makeText(getContext(), "You did not enter your PIN # for confirmation!", Toast.LENGTH_LONG).show();
                }
                else{
                    //Get the current user's pin field to check match:
                    String correctPin = currUser.getString(User.KEY_PIN).toString();
                    Log.d(TAG, "setMeetBuddyMode(): correct pin = " + correctPin);

                    if(attemptedPin.equals(correctPin)){
                        Toast.makeText(getContext(), "You confirmed your safe arrival!", Toast.LENGTH_LONG).show();
                        onConfirmArrival();
                    }
                    else{   //pin entered was not correct
                        Toast.makeText(getContext(), "The pin entered is incorrect!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //4c.) for Floating Buddy button --> show info about the buddyTrip
        listener.setBuddyRequestBttnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toBuddyTripStatusFragment(BuddyTripStatusFragment.ON_TRIP_MODE, otherBuddyInstance, buddyTripInstance.getDestination());
            }
        });
    }



    //-------------------------------------Meet up mode helper methods------------------------------------------------------------------------------

    //Purpose:      populates corresponding data into Views of confirmMeetUpOverlay. Also draws route from currUser's currentLocation --> otherUser's currentLocation
    private void populateMeetUpViews() {
        Log.d(TAG, "populateMeetUpViews()");

        if (meetUpInstance != null && otherBuddyInstance != null && otherUser != null && curBuddyInstance != null) {
            //1.) Populate Buddy id's --> Get user's buddyInstance for buddyId --> get otherBuddy's id
            String currBuddyId = curBuddyInstance.getObjectId();
            tvUserBuddyId.setText("Your ID:  " + currBuddyId);

            //1b.) Populate otherBuddyId:  See if other user is classified as the "senderBuddy" or "receiverBuddy":
            String senderBuddyId = meetUpInstance.getSenderBuddyId();
            if(senderBuddyId.equals(currBuddyId)){                              //Then otherUser = the receiverBuddy
                otherUserId = meetUpInstance.getReceiverBuddyId();

                //Initialize sender/receiver Buddy instances --> used to create a BuddyTrip instance later:
                senderBuddy = curBuddyInstance;
                receiverBuddy = otherBuddyInstance;
            }
            else{                                                               //Then otherUser = the senderBuddy
                otherUserId = meetUpInstance.getSenderBuddyId();
                senderBuddy = otherBuddyInstance;
                receiverBuddy = curBuddyInstance;
            }
            tvOtherBuddyId.setText("Their ID:  " + otherUserId);

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


            //3.) Draw map from current user's current location --> other User's current location:
            WingsGeoPoint otherCurrLocationGeoPoint = (WingsGeoPoint) otherUser.getParseObject(User.KEY_CURRENTLOCATION);
            try {
                otherCurrLocationGeoPoint.fetchIfNeeded();
                wingsMap.routeFromCurrentLocation(new LatLng(otherCurrLocationGeoPoint.getLatitude(), otherCurrLocationGeoPoint.getLongitude()), true, "Their current location");
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        }
    }

    private static void onConfirmMeetUp(){
        Toast.makeText(context, "You confirmed Meet up with your buddy!", Toast.LENGTH_LONG).show();
        listener.stopCheckingProximity();

        //Create a BuddyTrip instance!
        WingsGeoPoint currLocation = (WingsGeoPoint) currUser.getParseObject(User.KEY_CURRENTLOCATION);
        try {
            currLocation.fetchIfNeeded();
            BuddyTrip buddyTrip = new BuddyTrip(senderBuddy, receiverBuddy, currLocation, meetUpInstance.getDestination());
            buddyTrip.save();

            updateBuddyForTrip(curBuddyInstance, buddyTrip);
            updateBuddyForTrip(otherBuddyInstance, buddyTrip);

            ParcelableObject sendData = new ParcelableObject();
            sendData.setMode(KEY_ON_TRIP_MODE);
            sendData.setBuddyTrip(buddyTrip);
            sendData.setOtherParseUser(otherUser);
            sendData.setOtherBuddy(otherBuddyInstance);
            sendData.setCurrBuddy(curBuddyInstance);

            listener.toBuddyHomeFragment(sendData);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public static void updateBuddyForTrip(Buddy buddyToUpdate, BuddyTrip buddyTrip){
        try {
            buddyToUpdate.setBuddyTrip(buddyTrip);
            buddyToUpdate.setOnBuddyTrip(true);
            buddyToUpdate.setOnMeetup(false);
            buddyToUpdate.save();
        }catch(ParseException e){
            Log.d(TAG, "updatedBuddyFroTrip(): error="+e.getLocalizedMessage());
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
                    if(attemptedPin.equals(correctPin)){            //TODO: Commenting out for now --> checking strings won't work for some reason --> debugging --> correctPin = "####" and attemptedPin = #### ?
                        //Create a BuddyTrip:*/
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
    //---------------------------------------------------------end all meetUp mode helper methods-----------------------------------------------------------


    //---------------------------------------------------------Start onTrip mode helper methods-----------------------------------------------------------
    //Purpose:      populates corresponding data into Views of tripInfoOverlay AND confirmArrivalOverlay (as they are exactly mirrored views). Also draws on map routing from currentLocation --> tripDestination.
    private void populateAllTripViews(){
        Log.d(TAG, "populateAllTripViews()");

        if (buddyTripInstance != null && otherBuddyInstance != null && otherUser != null && curBuddyInstance != null) {
            Log.d(TAG, "populateAllTripViews():  populating...");
            //1.) Find and Populate otherBuddyId:  See if other user is classified as the "senderBuddy" or "receiverBuddy":
            String currBuddyId = curBuddyInstance.getObjectId();
            String senderBuddyId = buddyTripInstance.getSenderBuddyId();
            if(senderBuddyId.equals(currBuddyId)){                              //Then otherUser = the receiverBuddy
                otherUserId = buddyTripInstance.getReceiverBuddyId();

                //Initialize sender/receiver Buddy instances --> used to create a BuddyTrip instance later:
                senderBuddy = curBuddyInstance;
                receiverBuddy = otherBuddyInstance;
            }
            else{                                                               //Then otherUser = the senderBuddy
                otherUserId = buddyTripInstance.getSenderBuddyId();
                senderBuddy = otherBuddyInstance;
                receiverBuddy = curBuddyInstance;
            }
            tvTripInfoOtherId.setText("Their ID:  " + otherUserId);
            tvConfirmArrivalOtherId.setText("Their ID:  " + otherUserId);

            //2.) Populate other views relying on otherUser:
            ParseFile imageFile = otherUser.getParseFile(User.KEY_PROFILEPICTURE);
            if (imageFile != null) {
                try {
                    Glide.with(getContext()).load(imageFile.getFile()).into(ivTripInfoProfile);
                    Glide.with(getContext()).load(imageFile.getFile()).into(ivConfirmArrivalProfile);
                } catch (ParseException error) {
                    error.printStackTrace();
                }
            }
            String fName = otherUser.getString(User.KEY_FIRSTNAME);
            tvTripInfoName.setText("Your Buddy:  " + fName);
            tvConfirmArrivalName.setText("Your Buddy:  " + fName);

            //3.) Draw map from current user's current location --> tripDestination:
            WingsGeoPoint tripDestination = buddyTripInstance.getDestination();
            tvTripInfoDestination.setText("Trip Destination:  ("+ Math.round(tripDestination.getLatitude()*1000.0)/1000.0 +", " + Math.round(tripDestination.getLongitude()*1000.0)/1000.0+")");
            tvConfirmArrivalTripDestination.setText("Trip Destination:  ("+ Math.round(tripDestination.getLatitude()*1000.0)/1000.0 +", " + Math.round(tripDestination.getLongitude()*1000.0)/1000.0+")");
            Log.d(TAG, "populateAllTripViews():  calling wingsMap.route()");
            wingsMap.routeFromCurrentLocation(new LatLng(tripDestination.getLatitude(), tripDestination.getLongitude()), true, "Trip destination");

            setOtherUserLocationMarker();
        }
    }

    //Purpose:          Called by wingsMap instance automatically every time map is drawn
    public static void setOtherUserLocationMarker(){
        WingsGeoPoint otherUserCurrLocation = (WingsGeoPoint) otherUser.getParseObject(User.KEY_CURRENTLOCATION);
        try {
            otherUserCurrLocation.fetchIfNeeded();
            wingsMap.setMarker(new LatLng(otherUserCurrLocation.getLatitude(), otherUserCurrLocation.getLongitude()), BitmapDescriptorFactory.HUE_ORANGE, true, "Your Buddy's current location");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private static void onConfirmArrival(){
        Toast.makeText(context, "You confirmed reaching your destination!", Toast.LENGTH_LONG).show();
        listener.stopCheckingProximity();

        //Buddy Trip is finished --> reset both users + show dialog asking if they'd like to rate each other:
        fabCancelBuddy.performClick();                                      //needs to reset both users completely (including deleting BuddyTrip) and go back to DefaultHomeFrag
        MakeRatingDialog dialog = MakeRatingDialog.newInstance();
        dialog.setTargetFragment(thisFragInstance, 1);
        dialog.show(fragManager, "MakeRatingDialogTag");
    }

    public static boolean checkNearEnough(){
        Log.d(TAG, "checkNearEnough(), distanceFromCurrLocation = " + wingsMap.getDistanceFromCurLocation());
        boolean result = wingsMap.isNearEnough(100);                //unsure if wingsMap will continue to update is all
        Log.d(TAG, "checkNearEnough(), result = " + result );//+ "      meetUpOverlayEnabled =" + meetUpOverlayEnabled);
        return result;
    }
}