package com.example.wings.mainactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.example.wings.SheetsAndJava;
import com.example.wings.mainactivity.fragments.BuddyTripStatusFragment;
import com.example.wings.mainactivity.fragments.EditTrustedContactsFragment;
import com.example.wings.mainactivity.fragments.ReviewFragment;
import com.example.wings.mainactivity.fragments.dialogs.RequestTimeDialog;
import com.example.wings.mainactivity.fragments.dialogs.SafetyOptionsDialog;
import com.example.wings.mainactivity.fragments.dialogs.WarningDialog;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.example.wings.models.inParseServer.TrustedContact;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.example.wings.workers.CheckProximityWorker;
import com.example.wings.R;
import com.example.wings.workers.TimerWorker;
import com.example.wings.workers.UpdateLocationWorker;
import com.example.wings.mainactivity.fragments.home.BuddyHomeFragment;
import com.example.wings.mainactivity.fragments.ChooseBuddyFragment;
import com.example.wings.commonFragments.HelpFragment;
import com.example.wings.mainactivity.fragments.home.ConfirmBuddyHomeFragment;
import com.example.wings.mainactivity.fragments.home.DefaultHomeFragment;
import com.example.wings.mainactivity.fragments.home.HomeFragment;
import com.example.wings.mainactivity.fragments.OtherProfileFragment;
import com.example.wings.mainactivity.fragments.ProfileSetupFragment;
import com.example.wings.mainactivity.fragments.SearchUserFragment;
import com.example.wings.commonFragments.SettingsFragment;
import com.example.wings.mainactivity.fragments.UserBuddyRequestsFragment;
import com.example.wings.mainactivity.fragments.UserProfileFragment;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.User;
import com.example.wings.startactivity.StartActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.parse.ParseException;
import com.parse.ParseUser;


import org.parceler.Parcels;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.internal.http2.Http2Reader;

/**
 * MainActivity.java
 * Purpose:         Displays the appropriate fragments that executes the main functions of the app. Essentially is a container to swap between each fragment. Will have to do misc. tasks that fragments cannot do itself
 *                  e.g. constant floating buddy button + onClick listener. Because this class is the main container for the whole app --> talks to all Worker classes, Fragments, and some models. Many constants are necessary
 *                  to sort between each.
 *
 * Classes associated w/:
 *      Workers:        CheckProximityWorker (checks if user is close to a certain location), UpdateLocationWorker(updates current location to Parse database)
 *      All fragments:
 *          HomeFragments:          fragments that are responsible for s specific version of the home page (e.g. onMeetUp, onBuddyTrip, onFindingBuddy,etc)
 *          Misc.       :           ...all others
 *
 * Layout file: activity_main.xml
 */
public class MainActivity extends AppCompatActivity implements MAFragmentsListener, SafetyOptionsDialog.SafetyToolkitListener, RequestTimeDialog.RequestDialogListener {
    private static final String TAG = "MainActivity";
    private static final String CHECK_PROXIMITY_WORKER_TAG = "checkProximityWorkers";       //these two are needed to stop workers when necessary
    private static final String TIMER_WORKER_TAG = "timerWorkers";

    //MainActivity specific constants --> to label which HomeFragment currUser needs to go to/previously have gone to:
    public static final String DEFAULT_HOME = "DefaultHomeFragment";
    public static final String BUDDY_HOME_FIND_MODE = "BuddyHomeFragment-mode= find a buddy";
    public static final String BUDDY_HOME_MEETUP_MODE = "BuddyHomeFragment-mode = meeting a buddy";
    public static final String BUDDY_HOME_MEETUP_MODE_NEAR = "BuddyHomeFragment-mode = meeting a buddy - close enough (enable overlays!)";
    public static final String BUDDY_HOME_ONTRIP_MODE = "BuddyHomeFragment-mode= on a trip";
    public static final String BUDDY_HOME_ONTRIP_MODE_NEAR = "BuddyHomeFragment-mode= on a trip - close enough (enable overlays!)";

    //Other constants used to pass data to other frags:
    public static final String KEY_PROFILESETUPFRAG = "ProfileSetupFrag?";          //to get whether or not the current user's profile is set up from the StartActivity
    private static final String KEY_MODE = "whatMode?";

    //UpdateLocationWorker keys:
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;              //request code for permissions result
    private static final int REQUEST_CODE_CALL_PHONE_AND_TEXT = 1234;
    public static final String KEY_SENDCOUNTER = "activity_counter";

    //CheckProximityWorker keys:
    private static final String KEY_PROXIMITY_RESULT = "closeEnough?";

    //Keys for which notifications of messages to TrustedContacts:
    public static final String USER_INVOKED = "user asked for notification --> they feel unsafe!";
    public static final String WINGS_INVOKED = "Wings asked for emergency notifications via text!";



    //----------Class fields begin here-----------------------------------------------
    private String previousHomeFrag = "";           //to track which HomeFrag + modes need to go back to when navigating frags      //TODO: I actusally don't know if we'll need to track the previous
    private String currentHomeFrag = "";

    ParseUser currUser = ParseUser.getCurrentUser();
    Buddy userBuddyInstance;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    FrameLayout fragmentContainer;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabBuddyRequests;            //used to display the BuddyRequest control button above all fragments when applicable!
    private boolean restrictUserScreen = false;

    //fields for getting current location:
    private boolean keepTracking;                    //whether or not we should continue tracking user's current location
    private boolean keepCheckingProximity;
    private boolean runRequestHandling = false;
    private int receivedRequestCount;
    private int counter = 0;
    LifecycleOwner owner = this;                    //used in the startTracking() to listen to WorkInfo responses from the UpdateLocationWorker

    private boolean sToolkitWaitingForOkay = false;             //used to tell SafetyOptionsDialog which overlay to show (options or safety confirmation?)

    //For TimerWorker:
    private boolean timerOn = false;                 //used so BuddyHomeFrag can tell MainActivity to start timer for tracking EST
    private boolean onInitialWait = false;          //are we just starting a trip/meetup? e.g. = false when we've given a warning already/on agreed request/not waiting

    @Override
    /**
     * Purpose:         called automatically when activity is launched. Initializes the BottomNavigationView with listeners when each menu item is clicked. on click --> raise correct fragment class.
     */
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "in onCreate():");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentContainer = findViewById(R.id.flFragmentContainer);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        fabBuddyRequests = findViewById(R.id.fabBuddyRequests);
        fabBuddyRequests.setVisibility(View.INVISIBLE);             //by default so other fragments may choose to toggle if they choose, also has no handler by default!

        //Toast.makeText(this, "You may need to refresh the page!", Toast.LENGTH_LONG).show();
        //1.) Figure out which HomeFrag to start on:
        currentHomeFrag = findUserBuddyStatus();
        Log.i(TAG, "onCreate() - currentHomeFrag = " + currentHomeFrag);

        //2.) Find out whether or not need to force ProfileSetupFrag (Intent would be given by StartActivity):
        if(getIntent().getBooleanExtra(KEY_PROFILESETUPFRAG, false)){
            restrictUserScreen = true;
            setRestrictScreen(restrictUserScreen);              //hides bottom nav bar and safety toolkit button until user's account is fully set up
            toProfileSetupFragment();
        }

        //else create bottom nav menu, go to correct HomeFrag, and begin tracking location:
        else {
            Log.d(TAG, "");
            //2.) Unrestrict the screen --> shows the safety toolkit and bottom nav bar:
            restrictUserScreen = false;
            setRestrictScreen(restrictUserScreen);      //also automatically pulls the HomeFrag corresponding to the currentHomeFrag field onto screen
        }
            //2.) Start tracking current user's location automatically (will ask permission if needed):
                //2a.) Clear any old location requests:
                CountDownLatch clearRequests = new CountDownLatch(1);
                stopAllRequests(clearRequests);

                try {   //tells main thread to wait until all requests are cleared
                    clearRequests.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //2b.) Check if we have user's permission to track location, if not, request it
                if((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                    //request permissions (ACCESS_FINE_LOCATION AND ACCESS_COARSE_LOCATION)
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                }
                //Otherwise --> start tracking location
                else{
                    keepTracking= true;
                    startTracking();           //infinitely runs as long as keepTracking = true;
                }
    }

    /**
     * Purpose:         called automatically when activity launched --> inflates and displays menu items across the activity using "menu_main_navigation.xml".
     * @param menu
     * @return whether or not to show the menu
     */
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_top_navigation, menu);
        //MenuItem safetyToolkit = (MenuItem) findViewById(R.id.action_safety_toolkit);
        menu.findItem(R.id.action_safety_toolkit).setVisible(!restrictUserScreen);
        return true;
    }

    /**
     * Purpose:         Attaches events when menu items are pushed.
     * @param item, which item was selected
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent;
        switch(item.getItemId()){
            case R.id.action_logout:
                //1.) Log off the user using Parse:
                 ParseUser.logOut();

                //2.) Intent to go to StartActivity, finish() this activity, stop tracking current user:
                Log.d(TAG, ": logging out");
                    //2a.) Clear all requests:
                CountDownLatch clearRequests = new CountDownLatch(1);
                stopAllRequests(clearRequests);
                try {   //tells main thread to wait until all requests are cleared
                    clearRequests.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                    //2b.) Start intent to StartActivity
                intent = new Intent(this, StartActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:
                //Intent to SettingsActivity
                toSettingsFragment();
                break;
            case R.id.action_safety_toolkit:
                onClickSafetyToolkit();
                break;
            case R.id.action_help:
                toHelpFragment();
                break;
            default:
                return false;
        }
        return false;
    }

    //----------------------------------------------------------All Location stuff------------------------------------------------

    @Override
    /**
     * Purpose:         To ask the user permission for the given String[] of permissions, then handles the result (if permissions are granted, call startTrack())
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 1 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                keepTracking = true;
                startTracking();
            }
            else{
                Toast.makeText(this, "Permission was denied!", Toast.LENGTH_SHORT).show();
                //TODO: handle what to do when location permision is denied , i.e ask again or Toast, etc
            }
        }

        if(requestCode == REQUEST_CODE_CALL_PHONE_AND_TEXT && grantResults.length>2/*3*/){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED /*&& grantResults[3] == PackageManager.PERMISSION_GRANTED*/) {
                //makePhoneCall()
                Toast.makeText(this, "Please try again for now! This will be fixed soon!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission was denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Purpose:         Clears all old UpdateLocation requests or stops all new requests by setting keepTracking field = false.
     */
    private void stopAllRequests(CountDownLatch latch){
        keepTracking= false;
        timerOn = false;
        WorkManager.getInstance(getApplicationContext()).cancelAllWork();       //probably will need to refine by Tags so specific requests get canceled
        latch.countDown();
        //textView.setText("All request stopping..");
    }

    /**
     * Purpose:     Makes a OneTimeWorkRequest on the UpdateLocationWorker class infinitely as long as keepTracking = true. keepTracking may be turned off through the stopAllRequests()
     */
    private void startTracking(){

        //Package data to send the counter
        Data data = new Data.Builder()
                .putInt(KEY_SENDCOUNTER, counter)       //send the counter
                .build();

        //Create the request
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UpdateLocationWorker.class)
                .setInputData(data)         //send data
                .setInitialDelay(60, TimeUnit.SECONDS)      //wait 5 seconds before doing it         //TODO: don't hardcode, make this time frame a constant
                .build();

        //Queue up the request
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(
                        "sendDataWorker request",
                        ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                        (OneTimeWorkRequest) request
                );
        //Listen to information from the request
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                .observe(owner, new Observer<WorkInfo>() {

                    //called every time WorkInfo is changed
                    public void onChanged(@Nullable WorkInfo workInfo) {

                        //If workInfo is there and it is succeeded --> update the text
                        if (workInfo != null) {
                            //Check if finished:
                            if(workInfo.getState().isFinished()){
                                if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                                    Log.d(TAG, "Request succeeded ");

                                    if(keepTracking) {
                                        startTracking();               //to infinitely do it!
                                    }
                                }
                                else {
                                    Log.d(TAG, "Request didn't succeed, status=" + workInfo.getState().name());
                                    startTracking();
                                }
                            }
                        }
                    }
                });
    }

    //------------------------------------------------------------------------All Location Stuff---------------------------------------------


    //----------------------------------------------------------Required methods by MAFragmentsListener-----------------------------------------------------
    /**
     * Purpose:     Needs to be implemented in this activity so Safety Toolkit will execute on any screen
     */
    public void onClickSafetyToolkit(){
        /*Open Dialog box here:
            Option 1: User feels unsafe and suspicious --> notify all Trusted Contacts
            Option 2: Immediate danger --> notify all Trusted Contacts + dial the police
         */
        //Toast.makeText(this, "You pushed the Safety Toolkit button! Sorry, it's not implemented yet!", Toast.LENGTH_SHORT).show();
        SafetyOptionsDialog dialog = SafetyOptionsDialog.newInstance(sToolkitWaitingForOkay);
        dialog.show(fragmentManager, "SafetyOptionsDialog");
        //Handles the response through overriden interface methods onNotify and onEmergency()
    }

    /**
     * Purpose:         Restricts or unrestricts the screen of MainActivity given boolean answer. Restricting the screen deletes the Safety Toolkit button from the Top Nav bar, and hides the Biottom nav bar.
     *                  Unrestricting does this vice versa.
     * @param answer
     */
    public void setRestrictScreen(boolean answer){
        Log.d(TAG, "setRestrictScreen():   profile setup?: " + ParseUser.getCurrentUser().getBoolean(User.KEY_PROFILESETUP));
        //If we need to restrict screen --> hide Bottom Nav Bar
        if(answer){
            bottomNavigationView.setVisibility(View.GONE);
        }

        //Otherwise, show and initialize Bottom Nav Bar, and start on HomeFragment:
        else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        /**
                         * Purpose:         called when a specific item is clicked/selected --> raise correct fragment
                         */
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            Fragment fragment = null;
                            switch (item.getItemId()) {
                                case R.id.action_search_friends:
                                    fragment = new SearchUserFragment();
                                    fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).commit();
                                    break;

                                case R.id.action_home:
                                    fragment = getCorrespondingFrag(currentHomeFrag);
                                    fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).addToBackStack(null).commit();
                                    break;

                                case R.id.action_profile:
                                    fragment = new UserProfileFragment();
                                    fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).commit();
                                    break;
                                default:
                                    break;
                            }
                            return true;
                        }
                    });
            //By default: select/press the action_home action --> start user on home timeline page once logged in!
            bottomNavigationView.setSelectedItemId(R.id.action_home);
        }

        //2.) Shows or unshows the Safety toolkit button through re-initializing onCreateOptionsMenu():
        if(restrictUserScreen != answer) {      //just in case restrictUserScreen not yet consistent with "answer"
            restrictUserScreen = answer;
        }
        this.invalidateOptionsMenu();
    }


    //--------------------------------Required implementation of methods from MAFragmentsListener--------------------------------------------------------------------------
    @Override
    public void toHomeFragment(String modeKey) {
        Bundle bundle = new Bundle();;
        bundle.putString(KEY_MODE, modeKey);
        Fragment frag = new HomeFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }

    @Override
    public void toDefaultHomeFragment() {
        //update HomeFrag history:
        previousHomeFrag = currentHomeFrag;
        currentHomeFrag = DEFAULT_HOME;

        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new DefaultHomeFragment()).commit();
    }
    @Override
    public void toBuddyHomeFragment(ParcelableObject data) {
        //Always update this before sending it off! --> ensures BuddyHomeFrag will always know whether or not the timer is on --> will not accidentally start the timer again
        data.setTimerOn(timerOn);

        //1.) update HomeFrag history:
        String mode = data.getMode();
        boolean closeEnough = data.getBoolean();            //returns false by default if "data" did not specifically initalize it

        if(!mode.equals("")){
            previousHomeFrag = currentHomeFrag;
            if (mode.equals(BuddyHomeFragment.KEY_FIND_BUDDY_MODE)) {
                currentHomeFrag = BUDDY_HOME_FIND_MODE;
            } else if (mode.equals(BuddyHomeFragment.KEY_MEET_BUDDY_MODE) && !closeEnough) {          //if meetup mode and user is not close enough to destination
                currentHomeFrag = BUDDY_HOME_MEETUP_MODE;
            } else if (mode.equals(BuddyHomeFragment.KEY_MEET_BUDDY_MODE) && closeEnough) {        //if meetup mode and user IS close enough to destination
                currentHomeFrag = BUDDY_HOME_MEETUP_MODE_NEAR;
            } else if (mode.equals(BuddyHomeFragment.KEY_ON_TRIP_MODE)) {
                currentHomeFrag = BUDDY_HOME_ONTRIP_MODE;
            } else {//TODO: come back to revise this bc it needs to be done better
                Log.d(TAG, "toBuddyHomeFragment(): error, mode did not match any of the modes: mode=" + mode);
            }

            //2.) Bundle it and send:
            Bundle bundle = new Bundle();
            bundle.putParcelable(BuddyHomeFragment.KEY_DATA, Parcels.wrap(data));
            Fragment frag = new BuddyHomeFragment();
            frag.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
        }
        else{
            Log.d(TAG, "toBuddyHomeFrag(): mode or contextFrom was null");
        }
    }

    @Override
    //Because ConfirmBuddyHomeFragment are explicitly one time use --> do NOT save it in history (e.g. change currentHomeFrag)
    public void toConfirmBuddyHomeFragment(String modeKey, String otherUserId) {
        Bundle bundle = new Bundle();;
        bundle.putString(KEY_MODE, modeKey);
        bundle.putString(ConfirmBuddyHomeFragment.KEY_OTHER_USER_ID, otherUserId);
        Fragment frag = new ConfirmBuddyHomeFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }

    @Override
    public void toConfirmBuddyHomeFragment(String modeKey, String otherUserId, String buddyRequestId) {
        Bundle bundle = new Bundle();;
        bundle.putString(KEY_MODE, modeKey);
        bundle.putString(ConfirmBuddyHomeFragment.KEY_OTHER_USER_ID, otherUserId);
        bundle.putString(ConfirmBuddyHomeFragment.KEY_BUDDYREQUESTID, buddyRequestId);
        Fragment frag = new ConfirmBuddyHomeFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }

    @Override
    //Purpose:          navigates to whatever was the last home fragment (= whatever HomeFrag that corresponds to previousHomeFragment class field)
    public void toCurrentHomeFragment() {
        Fragment fragment = getCorrespondingFrag(currentHomeFrag);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).addToBackStack(null).commit();
    }

    @Override
    public void toBuddyTripStatusFragment(String mode, Buddy otherBuddy, WingsGeoPoint tripDestination) {
        //1.) Package ParseObjects into a ParcelableObject to send as a Parcelable:
        ParcelableObject sendData = new ParcelableObject();
        sendData.setMode(mode);
        sendData.setOtherBuddy(otherBuddy);
        sendData.setWingsGeoPoint(tripDestination);

        //2.) Bundle it and send:
        Bundle bundle = new Bundle();;
        bundle.putParcelable(BuddyTripStatusFragment.KEY_DATA, Parcels.wrap(sendData));
        Fragment frag = new BuddyTripStatusFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }

    public void toProfileSetupFragment(){
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new ProfileSetupFragment()).commit();
    }

    @Override
    public void toProfileSetupFragment(List<TrustedContact> trustedContacts) {
        ParcelableObject sendData = new ParcelableObject();
        sendData.setTrustedContactList(trustedContacts);

        Bundle bundle = new Bundle();
        bundle.putParcelable(ProfileSetupFragment.KEY_TRUSTED_CONTACTS, Parcels.wrap(sendData));
        Fragment frag = new ProfileSetupFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }
    @Override
    public void toEditTrustedContactsFragment(List<TrustedContact> trustedContacts) {
        Log.d(TAG, "toEditTrustedContactsFragment - trustedContacts = " + trustedContacts.toString());
        ParcelableObject sendData = new ParcelableObject();
        sendData.setTrustedContactList(trustedContacts);

        Bundle bundle = new Bundle();
        bundle.putParcelable(EditTrustedContactsFragment.KEY_TRUSTED_CONTACTS, Parcels.wrap(sendData));        //ProfileSetupFrag and EditTrustedContacts keys are the same but we get error for statically calling EditTrustedCibtacts's key
        Fragment frag = new EditTrustedContactsFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }

    @Override
    public void toUserProfileFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new UserProfileFragment()).commit();
    }
    @Override
    public void toSearchUserFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new SearchUserFragment()).commit();
    }
    //Purpose:      Displays the corresponding Fragment classes
    @Override
    public void toOtherProfileFragment(ParseUser userToShow) {
        ParcelableObject sendData = new ParcelableObject();
        sendData.setOtherParseUser(userToShow);
        Bundle bundle = new Bundle();
        bundle.putParcelable(OtherProfileFragment.KEY_OTHER_USER, Parcels.wrap(sendData));
        Fragment frag = new OtherProfileFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }
    @Override
    public void toChooseBuddyFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new ChooseBuddyFragment()).commit();
    }
    @Override
    public void toSettingsFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new SettingsFragment()).commit();
    }

    @Override
    public void toHelpFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new HelpFragment()).commit();
    }
    @Override
    public void toUserBuddyRequestFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new UserBuddyRequestsFragment()).commit();
    }

    @Override
    public void toReviewFragment(ParseUser userReviewFor) {
        ParcelableObject sendData = new ParcelableObject();
        sendData.setOtherParseUser(userReviewFor);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ReviewFragment.KEY_FOR_USER, Parcels.wrap(sendData));
        Fragment fragment = new ReviewFragment();
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).commit();
    }

    @Override
    public void setBuddyRequestBttn(boolean answer) {
        if(answer){
            fabBuddyRequests.setVisibility(View.VISIBLE);
        }
        else{
            fabBuddyRequests.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void setBuddyRequestBttnOnClickListener(View.OnClickListener onClickListener) {
        fabBuddyRequests.setOnClickListener(onClickListener);
    }
    @Override
    //Purpose:      To start checking for proximity this distance away for this meetUpId
    public void startCheckingProximity(int meters, BuddyMeetUp meetUpInstance) {
        keepCheckingProximity = true;
        startCheckingProximityWorker(meters, meetUpInstance);
    }

    @Override
    public void stopCheckingProximity() {
        keepCheckingProximity = false;
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(CHECK_PROXIMITY_WORKER_TAG);       //probably will need to refine by Tags so specific requests get canceled
    }

    @Override
    //Purpose:          Called by BuddyHomeFrag --> which user has confirmed safe arrival home --> sends messages to all Trusted Contacts that user has supposedly arrived home IF user had previously used the safety toolkit at all this trip
    public void sendArrivedMessage(){
        if(sToolkitWaitingForOkay){
            Log.d(TAG, "sendArrivedMessage():  sending all TC's that this user has arrived home/destination");
            User localParseUser = new User(currUser);
            messageAllTC(localParseUser.getArrivedMessage());
        }
    }

    @Override
    //Purpose:          changes the startTimer field --> starts TimerWorker to wait for this amount of time
    public void startTimer(boolean isInitalWait, long est) {
        timerOn = true;
        onInitialWait = isInitalWait;
        //If this is the first time we are waiting for a trip/meetup --> wait for the est + 10 extra minutes
        if(isInitalWait){
            startTimerWorker(est/*+600*/);      //10 min * 60sec = 600 sec added
            //TODO: ensure to uncomment the added 10 min! Commented for testing reasons..
        }

        //Otherwise we are starting the timer due to a reqest --> do NOT give them extra time.
        else{
            startTimerWorker(est);
        }
    }

    @Override
    public void stopTimer(){
        timerOn = false;
        onInitialWait = false;
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(TIMER_WORKER_TAG);
        Log.d(TAG, "timer stopped.");
    }

    @Override
    //Purpose:          Called when the users have request and confirmed additional time to arrive. Stop the timmer, and start again from this new time:
    public void setRequestTime(long time) {
        stopTimer();
        startTimer(false, time);
    }
    @Override
    public void onClose(){
        showSnackBar();
    }
    @Override
    public void showSnackBar(){
        View.OnClickListener listenerToRequestTimeDialog = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestTimeDialog dialog = RequestTimeDialog.newInstance();
                dialog.show(fragmentManager, "RequestTimeDialogTag");
            }
        };
        Snackbar snack = Snackbar.make(fragmentContainer, "URGENT: Confirm arrival or Request more time within 10 MIN!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Request more time", listenerToRequestTimeDialog)
                .setActionTextColor(getResources().getColor(R.color.red, null));
        //Make it display at the top:
        View snackView = snack.getView();
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams params =(androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackView.setLayoutParams(params);
        snack.show(); // Don’t forget to show!
    }

    //------------Misc. helper methods------------------------------------------------------------------------
    private void setCurrentHomeFragment(String key){
        currentHomeFrag = key;          //assuming the key is one of the main activity's constants --> needs error checking
    }

    //Purpose:      Finds out at which stage the of a BuddyTrip (if any) the current user is at in order to begin on the correct HomeFrag:
    private String findUserBuddyStatus(){
        Log.d(TAG, "findUserBuddyStatus, isBuddy=" + currUser.getBoolean(User.KEY_ISBUDDY));
        //1.) Is the user even a buddy?
        if(!currUser.getBoolean(User.KEY_ISBUDDY)) {        //if not --> they belong to DefaultHomeFrag
            return DEFAULT_HOME;
        }
        else{
            //Then get the currUser's buddy instance:
            userBuddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
            try {
                userBuddyInstance.fetchIfNeeded();

                //2.) Do they need a buddy?
                if (!userBuddyInstance.getHasBuddy()) {
                    return BUDDY_HOME_FIND_MODE;                //--> they need to find a buddy
                } else {
                    //3.) Are they current meeting their buddy or already on a BuddyTrip?
                    if(userBuddyInstance.getOnMeetup() && (!userBuddyInstance.getOnBuddyTrip()))
                    {
                        return BUDDY_HOME_MEETUP_MODE;          //--> they need be tracked to meet their buddy
                    }
                    else{
                        return BUDDY_HOME_ONTRIP_MODE;
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "error - No HomeFrag applicable.";
    }

    //Purpose:          Returns which HomeFrag instance corresponds to the currentHomeFrag field. Used by bottomNavigationBar to always go back to the correct HomeFrag + mode when the home icon is pushed
    private Fragment getCorrespondingFrag(String fragLabel){
        ParcelableObject sendData = new ParcelableObject();
        Bundle bundle = new Bundle();
        Fragment frag = new BuddyHomeFragment();

        if(fragLabel.equals(DEFAULT_HOME)){
            return new DefaultHomeFragment();
        }
        else if(fragLabel.equals(BUDDY_HOME_FIND_MODE)){
            Log.d(TAG, "getCorrespondingFrag(): currUser = " + currUser.getObjectId());
            sendData.setMode(BuddyHomeFragment.KEY_FIND_BUDDY_MODE);
            bundle.putParcelable(BuddyHomeFragment.KEY_DATA, Parcels.wrap(sendData));
            frag.setArguments(bundle);
            return frag;
        }
        else if(fragLabel.equals(BUDDY_HOME_MEETUP_MODE)){
            BuddyMeetUp meetUpInstance = userBuddyInstance.getBuddyMeetUpInstance();
            return makeBuddyHomeFragmentMeetUp(meetUpInstance, false);
        }
        else if(fragLabel.equals(BUDDY_HOME_MEETUP_MODE_NEAR)){
            BuddyMeetUp meetUpInstance = userBuddyInstance.getBuddyMeetUpInstance();
            return makeBuddyHomeFragmentMeetUp(meetUpInstance, true);
        }
        else if(fragLabel.equals(BUDDY_HOME_ONTRIP_MODE)){
            BuddyTrip buddyTripInstance = userBuddyInstance.getBuddyTripInstance();
            return makeBuddyHomeFragmentBuddyTrip(buddyTripInstance, false);
        }
        else if(fragLabel.equals(BUDDY_HOME_ONTRIP_MODE_NEAR)){
            BuddyTrip buddyTripInstance = userBuddyInstance.getBuddyTripInstance();
            return makeBuddyHomeFragmentBuddyTrip(buddyTripInstance, true);
        }
        else{
            Log.d(TAG, "getCurrentHomeFrag(): error - currentHomeFrag string doesn't match any of the MainActivity keys?");
            return null;
        }
    }

    /**
     * Purpose:     Makes a OneTimeWorkRequest on the UpdateLocationWorker class infinitely as long as keepTracking = true. keepTracking may be turned off through the stopAllRequests()
     */
    private void startCheckingProximityWorker(int meters, BuddyMeetUp meetUpInstance){
        Log.d(TAG, "startCheckingProximityWorker()");

        //Create the request
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CheckProximityWorker.class)
                //.setInputData(data)         //send data
                .addTag(CHECK_PROXIMITY_WORKER_TAG)
                .setInitialDelay(10, TimeUnit.SECONDS)      //wait 5 seconds before doing it         //TODO: don't hardcode, make this time frame a constant
                .build();

        //Queue up the request
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(
                        "sendDataWorker request",
                        ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                        (OneTimeWorkRequest) request
                );
        //Listen to information from the request
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                .observe(owner, new Observer<WorkInfo>() {

                    //called every time WorkInfo is changed
                    public void onChanged(@Nullable WorkInfo workInfo) {

                        //If workInfo is there and it is succeeded
                        if (workInfo != null) {
                            if(workInfo.getState().isFinished()){
                                if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                                    Log.d(TAG, "Request succeeded ");

                                    //1.) get output:
                                    Data output = workInfo.getOutputData();
                                    boolean result = output.getBoolean(KEY_PROXIMITY_RESULT, false);
                                    Log.d(TAG, "startCheckingProximityWorker(): result=" + result);

                                    //2.) Handle it:
                                    handleCheckProximityResult(result);

                                    //3.) Keep running the CheckProximityWorker? --> stops when keepCheckingProximity = toggled to false by an onClick button from BuddyHomeFrag
                                    if(keepCheckingProximity) {
                                        startCheckingProximityWorker(meters, meetUpInstance);              //to infinitely do it!
                                    }
                                }
                                else {
                                    Log.d(TAG, "Request didn't succeed, status=" + workInfo.getState().name());
                                    startCheckingProximityWorker(meters, meetUpInstance);
                                }
                            }
                        }
                    }
                });
    }

    //Purpose:          given how much time to wait in total in sec --> starts the TimerWorker to time it. This method will chop the calculatedEst accordingly in order to give warnings and chances for users to finish trip okay.
    public void startTimerWorker(long est) {
        //Warning 1 --> After 10 min passed the original est
        //Warning 2 --> After

        //For now just + 10min to est --> can request for time
        //Test TimerWorker:
        Data data = new Data.Builder().putLong(TimerWorker.KEY_TIME_WAIT_FOR, est).build();

        //Create the request
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(TimerWorker.class)
                .setInputData(data)         //send data
                .addTag(TIMER_WORKER_TAG)
                .build();

        //Queue up the request
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(
                        "timerWorker request",
                        ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                        (OneTimeWorkRequest) request
                );
        //Listen to information from the request
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                .observe(owner, new Observer<WorkInfo>() {

                    //called every time WorkInfo is changed
                    public void onChanged(@Nullable WorkInfo workInfo) {

                        //If workInfo is there and it is succeeded
                        if (workInfo != null) {
                            if (workInfo.getState().isFinished()) {
                                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                    Log.d(TAG, "Request succeeded ");
                                   // Toast.makeText(getApplicationContext(), "Timer is done!!", Toast.LENGTH_SHORT).show();

                                    //1.) If onInitialWait --> user still deserves a warning:  Show Dialog warning and constant SnackBar:
                                    if(onInitialWait) {
                                        //1a.) Automatically show warning:
                                        WarningDialog dialog = WarningDialog.newInstance();
                                        dialog.show(fragmentManager, "WarningDialogTag");

                                        //1b.) Show infinite SnackBar --> Click listener --> re-show the Dialog needed to make request for more time!
                                        showSnackBar();

                                        //1c.) Restart timer for 10 more min & ensure to toggle onInitialWait to off
                                        stopTimer();
                                        startTimer(false, 10/**60*/);       //TODO: ensure to remove commenting here (commented for testing!)
                                    }

                                    //2.) else --> tell them we're notifying all Trusted Contacts, and then do it, then stopTimer(). (But CheProximityWorker is still on btw)
                                    else{
                                        //2a.) If the RequestTimeDialog is open --> automatically close it:
                                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("RequestTimeDialogTag");
                                        if(fragment != null)
                                            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

                                        Snackbar snack = Snackbar.make(fragmentContainer, "URGENT: You have not responded in time, notifying all Trusted Contacts now!", Snackbar.LENGTH_INDEFINITE);
                                              //  .setAction("Request more time", listenerToRequestTimeDialog)
                                              //  .setActionTextColor(getResources().getColor(R.color.red, null));

                                        //Make it display at the top:
                                        View snackView = snack.getView();
                                        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams params =(androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
                                        params.gravity = Gravity.TOP;
                                        snackView.setLayoutParams(params);
                                        snack.show(); // Don’t forget to show!

                                        onNotifyContacts(WINGS_INVOKED);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    //Purpose:          Creates/packages a BuddyHomeFragment for mode=onMeetUp. Because this mode will be display different depending on closeEnough, closeEnough is REQUIRED to be specified. Used by getCorrespondingFragment(), and
    //                  handling WorkerRequests when on success! (WorkerRequests keep checking certain fields for when closeEnough can = true!)
    private Fragment makeBuddyHomeFragmentMeetUp(BuddyMeetUp meetUpInstance, boolean closeEnough){
        ParcelableObject sendData = new ParcelableObject();
        sendData.setTimerOn(timerOn);
        Bundle bundle = new Bundle();
        Fragment frag = new BuddyHomeFragment();

        //Get the necessary info:
        Buddy otherBuddy;
        if(meetUpInstance.getSenderBuddyId().equals(userBuddyInstance.getObjectId())){
            otherBuddy = meetUpInstance.getReceiverBuddy();
        }
        else{
            otherBuddy = meetUpInstance.getSenderBuddy();               //not actually good --> will also go here if error
        }
        ParseUser otherUser = otherBuddy.getUser();

        //Fill in data into the Parcelable Object:
        sendData.setMode(BuddyHomeFragment.KEY_MEET_BUDDY_MODE);
        sendData.setBuddyMeetUp(meetUpInstance);
        sendData.setOtherBuddy(otherBuddy);
        sendData.setOtherParseUser(otherUser);
        sendData.setCurrBuddy(userBuddyInstance);
        sendData.setBoolean(closeEnough);

        //Package the Parcelable Object into the Bundle
        bundle.putParcelable(BuddyHomeFragment.KEY_DATA, Parcels.wrap(sendData));
        frag.setArguments(bundle);
        return frag;
    }

    //Purpose:          Creates/packages a BuddyHomeFragment for mode=onTrip. Because this mode will be display different depending on closeEnough, closeEnough is REQUIRED to be specified. Used by getCorrespondingFragment(), and
    //                  handling WorkerRequests when on success! (WorkerRequests keep checking certain fields for when closeEnough can = true!)
    private Fragment makeBuddyHomeFragmentBuddyTrip(BuddyTrip buddyTripInstance, boolean closeEnough){
        ParcelableObject sendData = new ParcelableObject();
        sendData.setTimerOn(timerOn);
        Bundle bundle = new Bundle();
        Fragment frag = new BuddyHomeFragment();

        //Get the necessary info:
        Buddy otherBuddy;
        //Figure out if the currUser = senderBuddy or receiverBuddy
        if(buddyTripInstance.getSenderBuddyId().equals(userBuddyInstance.getObjectId())){
            otherBuddy = buddyTripInstance.getReceiverBuddy();
        }
        else{
            otherBuddy = buddyTripInstance.getSenderBuddy();               //not actually good --> will also go here if error
        }
        ParseUser otherUser = otherBuddy.getUser();

        //Fill in data into the Parcelable Object:
        sendData.setMode(BuddyHomeFragment.KEY_ON_TRIP_MODE);
        sendData.setBuddyTrip(buddyTripInstance);
        sendData.setOtherBuddy(otherBuddy);
        sendData.setOtherParseUser(otherUser);
        sendData.setCurrBuddy(userBuddyInstance);
        sendData.setBoolean(closeEnough);

        //Package the Parcelable Object into the Bundle
        bundle.putParcelable(BuddyHomeFragment.KEY_DATA, Parcels.wrap(sendData));
        frag.setArguments(bundle);
        return frag;
    }

    //Purpose:          Called by result of CheckProximityWorker class. Figures out if result needs to invoke a specific BuddyHomeFrag + mode
    private void handleCheckProximityResult(boolean closeEnough){
        //If user is close enough --> enable correct overlay:
        if(closeEnough){
            if(currentHomeFrag.equals(BUDDY_HOME_MEETUP_MODE)) {            //if was on meetUp mode --> make it same thing but near
                setCurrentHomeFragment(BUDDY_HOME_MEETUP_MODE_NEAR);
            }
            else if(currentHomeFrag.equals(BUDDY_HOME_ONTRIP_MODE)){
                setCurrentHomeFragment(BUDDY_HOME_ONTRIP_MODE_NEAR);
            }
            else{
                return;             //no need to invoke a fragment
            }
        }
        //otherwise:   user not close enough --> check if they were previously though --> disable correct overlay
        else{
            if(currentHomeFrag.equals(BUDDY_HOME_MEETUP_MODE_NEAR)){
                setCurrentHomeFragment(BUDDY_HOME_MEETUP_MODE);
            }
            else if(currentHomeFrag.equals(BUDDY_HOME_ONTRIP_MODE_NEAR)){
                setCurrentHomeFragment(BUDDY_HOME_ONTRIP_MODE_NEAR);
            }
            else{
                return;
            }
        }
        toCurrentHomeFragment();
    }

    public void makeSnackBar(String title, String actionText, View.OnClickListener listener, int colorId){
        Snackbar snack = Snackbar.make(fragmentContainer, title, Snackbar.LENGTH_INDEFINITE)
                .setAction(actionText, listener)
                .setActionTextColor(getResources().getColor(colorId, null));
        //Make it display at the top:
        View snackView = snack.getView();
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams params =(androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackView.setLayoutParams(params);
        snack.show(); // Don’t forget to show!
    }


    //-----------------Overriding SafetyToolkitListener methods ---------------------------------------
    @Override
    //Purpose:          Toggle flag "sToolkitWaitingForOkay" so we wait for user to confirm their safety. Text all trusted contacts of current info.
    public void onNotifyContacts(String messageType) {
        //WAS testing google sheets API:
        /*try {
            SheetsAndJava sheetsAndJavaObject = new SheetsAndJava();
            Sheets sheetsService = sheetsAndJavaObject.getSheetsService(this);
            String range = "Sheet1!A2:E2";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SheetsAndJava.SPREADSHEET_ID, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                Log.d("Jo", "no data found");
            } else {
                for (List row : values) {
                    Log.d("Jo", (String) row.get(0) + " " + (String) row.get(1) + " " + (String) row.get(2) + " " + (String) row.get(3) + " " + (String) row.get(4));   //columns 1-5 --> indexes 0 - 4
                }
            }
        } catch(IOException ioException){

        }*/

        //testing setStartTimer() + startTimerWorker():

        sToolkitWaitingForOkay = true;

        //Check permissions for it:   don't needthe call permision but still. TODO: should probably ask for permissions in onCreate() btw
        if((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        ){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, REQUEST_CODE_CALL_PHONE_AND_TEXT);
        }
        else {
            User currLocalUser = new User(currUser);
            if(messageType.equals(USER_INVOKED)){
                makeSnackBar("All Trusted Contacts notified!", "ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {}          //automatically dimisses SnackBar
                }, R.color.white);
                String message = currLocalUser.getNotifyMessage();
                messageAllTC(message);
            }
            else if(messageType.equals(WINGS_INVOKED)){
                String message = currLocalUser.getEmergencyNoResponseMessage();
                messageAllTC(message);
            }
            else{
                Log.e(TAG, "messageType was not any of the two acceptable options");
            }

        }
    }

    @Override
    //Purpose:          Toggle flag "sToolkitWaitingForOkay" so we wait for user to confirm their safety. Make dial call immediately to police + text all trusted contacts of current info.
    public void onEmergency() {
        sToolkitWaitingForOkay = true;
        //Check permissions for it:
        if((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
                /*|| (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PRIVILEGED) != PackageManager.PERMISSION_GRANTED)*/
                ){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS/*, Manifest.permission.CALL_PRIVILEGED*/}, REQUEST_CODE_CALL_PHONE_AND_TEXT);
        }
        else{
            makeSnackBar("All Trusted Contacts notified!", "Ok", new View.OnClickListener() {
                @Override
                public void onClick(View v) {}
            }, R.color.white);

            //1.) Call Emergency services: 911 or campus phone, etc
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:9098693070"));
            startActivity(intent);
            /*Uri callUri = Uri.parse("tel://911");
            Intent callIntent = new Intent(Intent.ACTION_CALL,callUri);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            startActivity(callIntent);*/

            //2.) Text message every one of the user's trusted contacts:
            User currLocalUser = new User(currUser);
            String emergencyMessage = currLocalUser.getEmergencyMessage();
            messageAllTC(emergencyMessage);
        }
    }
    @Override
    //Purpose:      Toggle flag so we no longer check for user to be in danger.
    public void onOkayNow(){
        sToolkitWaitingForOkay = false;
        User currLocalUser = new User(currUser);
        messageAllTC(currLocalUser.getOkayMessage());
    }

    //Purpose:      Helper for overridden SafetyToolkitListener methods --> sends given message to all of user's trusted contacts
    public void messageAllTC(String message){
        //Log.d(TAG, "messageAllTC(): message =" + message);
        List<TrustedContact> trustedContacts = currUser.getList(User.KEY_TRUSTEDCONTACTS);
        Log.d(TAG, "messageAllTC(): trustedContacts = " + trustedContacts.toString());

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messageArray = smsManager.divideMessage(message);         //bc the message is to long
        for(int i = 0; i < trustedContacts.size(); i++) {
            TrustedContact currTC = trustedContacts.get(i);
            try {
                currTC.fetchIfNeeded();

                //smsManager.sendTextMessage(currTC.parsePhoneNumber(), null, "Just testing again", null, null);            //only for short messages
                smsManager.sendMultipartTextMessage(currTC.parsePhoneNumber(), null, messageArray, null, null);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}