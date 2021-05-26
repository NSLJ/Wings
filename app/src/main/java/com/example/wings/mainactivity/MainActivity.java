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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.wings.CheckProximityWorker;
import com.example.wings.HandleBuddyRequestsWorker;
import com.example.wings.R;
import com.example.wings.UpdateLocationWorker;
import com.example.wings.mainactivity.fragments.BuddyHomeFragment;
import com.example.wings.mainactivity.fragments.ChooseBuddyFragment;
import com.example.wings.commonFragments.EditTrustedContactsFragment;
import com.example.wings.commonFragments.HelpFragment;
import com.example.wings.mainactivity.fragments.ConfirmBuddyHomeFragment;
import com.example.wings.mainactivity.fragments.DefaultHomeFragment;
import com.example.wings.mainactivity.fragments.HomeFragment;
import com.example.wings.mainactivity.fragments.OtherProfileFragment;
import com.example.wings.mainactivity.fragments.PotentialBuddyFragment;
import com.example.wings.mainactivity.fragments.ProfileSetupFragment;
import com.example.wings.mainactivity.fragments.SearchUserFragment;
import com.example.wings.commonFragments.SettingsFragment;
import com.example.wings.mainactivity.fragments.UserBuddyRequestsFragment;
import com.example.wings.mainactivity.fragments.UserProfileFragment;
import com.example.wings.models.Buddy;
import com.example.wings.models.User;
import com.example.wings.startactivity.StartActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * MainActivity.java
 * Purpose:         Displays the appropriate fragments that executes the main functions of the app. Essentially is a container to swap between each fragment.
 *
 */
public class MainActivity extends AppCompatActivity implements MAFragmentsListener{
    private static final String TAG = "MainActivity";
    private static final String CHECK_PROXIMITY_WORKER_TAG = "checkProximityWorkers";

    //MainActivity specific constants --> to label which HomeFragment currUser needs to go to/previously have gone to:
    public static final String DEFAULT_HOME = "DefaultHomeFragment";
    public static final String BUDDY_HOME_FIND_MODE = "BuddyHomeFragment-mode= find a buddy";
    public static final String BUDDY_HOME_MEETUP_MODE = "BuddyHomeFragment-mode = meeting a buddy";
    public static final String BUDDY_HOME_MEETUP_MODE_CLOSE = "BuddyHomeFragment-mode = meeting a buddy - close enough!";
    public static final String BUDDY_HOME_ONTRIP_MODE = "BuddyHomeFragment-mode= on a trip";

    public static final String CONFIRM_BUDDY_HOME = "ConfirmBuddyHomeFragment";

    //Other constants used to pass data to other frags:
    public static final String KEY_PROFILESETUPFRAG = "ProfileSetupFrag?";          //to get whether or not the current user's profile is set up from the StartActivity
    public static final String KEY_USERID = "potentialBuddyId";
    private static final String KEY_DIALOG = "dialogTypeToShow";
    private static final String KEY_BUDDYREQUESTID = "buddyRequestId";
    private static final String KEY_MODE = "whatMode?";

    private static final String KEY_APPROVED_REQUEST = "whichSentRequest?"; //these two are for responses with sentrequests
    private static final String KEY_ISAPPROVED = "anyRequestsApproved?";
    private static final String KEY_INITIAL_COUNT = "initialSizeReceivedRequests";  //this is to
    private static final String KEY_NUM_RECEIVED = "responseTotalReceived";
    private static final String KEY_CHANGED = "receivedRequestsChanged?";

    //UpdateLocationWorker keys:
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;              //request code for permissions result
    public static final String KEY_SENDCOUNTER = "activity_counter";
    public static final String KEY_RESULTSTRING = "result_string";
    public static final String KEY_GETCOUNTER = "worker_counter";

    //CheckProximityWorker keys:
    private static final String KEY_PROXIMITY_RESULT = "closeEnough?";

    //----------Class fields begin here-----------------------------------------------
    private String previousHomeFrag = "";           //to track which HomeFrag + modes need to go back to when navigating frags      //TODO: I actusally don't know if we'll need to track the previous
    private String currentHomeFrag = "";

    ParseUser currUser = ParseUser.getCurrentUser();
    Buddy userBuddyInstance;

    final FragmentManager fragmentManager = getSupportFragmentManager();
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

    @Override
    /**
     * Purpose:         called automatically when activity is launched. Initializes the BottomNavigationView with listeners when each menu item is clicked. on click --> raise correct fragment class.
     */
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "in onCreate():");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        fabBuddyRequests = (FloatingActionButton) findViewById(R.id.fabBuddyRequests);
        fabBuddyRequests.setVisibility(View.INVISIBLE);             //do by default so other fragments may choose to toggle if they choose, also has no handler by default!

        //1.) Find out whether or not need to force ProfileSetupFrag:
        if(getIntent().getBooleanExtra(KEY_PROFILESETUPFRAG, false)){
            Log.d(TAG, "onCreate(): going to ProfileSetUpFragment");
            restrictUserScreen = true;
            setRestrictScreen(restrictUserScreen);          //hides bottom nav bar and safety toolkit button
            toProfileSetupFragment();
        }

        //else create bottom nav menu, go to HomeFrag, and begin tracking location:
        else {
            //1.) Figure out which HomeFrag to start on:
            currentHomeFrag = findUserBuddyStatus();
            Log.d(TAG, "onCreate() - currentHomeFrag = " + currentHomeFrag);

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
                   // setWatchRequests(true);
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
    //TODO: this is for testing purposes, delete later:
    private void setCounter(int newCounter){
        counter = newCounter;
    }

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
    }

    /**
     * Purpose:         Clears all old UpdateLocation requests or stops all new requests by setting keepTracking field = false.
     */
    private void stopAllRequests(CountDownLatch latch){
        keepTracking= false;
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
                .setInitialDelay(2, TimeUnit.MINUTES)      //wait 5 seconds before doing it         //TODO: don't hardcode, make this time frame a constant
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
                                    //get output:
                                    Data output = workInfo.getOutputData();
                                    String result = output.getString(KEY_RESULTSTRING);
                                    int newCounter = output.getInt(KEY_GETCOUNTER, 0);
                                    setCounter(newCounter);

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
        Toast.makeText(this, "You pushed the Safety Toolkit button! Sorry, it's not implemented yet!", Toast.LENGTH_SHORT).show();
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
                                    break;

                                case R.id.action_home:
                                    fragment = getCorrespondingFrag(currentHomeFrag);
                                    break;

                                case R.id.action_profile:
                                    fragment = new UserProfileFragment();
                                    break;
                                default:
                                    break;
                            }

                            //Raise the fragment:
                            fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).commit();
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
    @Override
    public void toHomeFragment(String modeKey) {
        Bundle bundle = new Bundle();;
        bundle.putString(KEY_MODE, modeKey);
        Fragment frag = new HomeFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }
    @Override
    public void toBuddyHomeFragment(String modeKey) {
        //update HomeFrag history:
        previousHomeFrag = currentHomeFrag;
        currentHomeFrag = BUDDY_HOME_FIND_MODE;     //I believe this method would only ever be called with find mode, if not --> will need to error check

        Bundle bundle = new Bundle();;
        bundle.putString(KEY_MODE, modeKey);
        Fragment frag = new BuddyHomeFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
    }
    @Override
    public void toBuddyHomeFragment(String modeKey, String meetUpId) {
        //update HomeFrag history:
        previousHomeFrag = currentHomeFrag;
        if(meetUpId.equals(BuddyHomeFragment.KEY_MEET_BUDDY_MODE)){
            currentHomeFrag = BUDDY_HOME_MEETUP_MODE;
        }
        else{
            currentHomeFrag = BUDDY_HOME_ONTRIP_MODE;                   //would need to error check if ever implement more than these three methods for BuddyHomeFrag
        }

        Bundle bundle = new Bundle();;
        bundle.putString(KEY_MODE, modeKey);
        bundle.putString(BuddyHomeFragment.KEY_BUDDYMEETUPID, meetUpId);
        bundle.putBoolean(BuddyHomeFragment.KEY_CLOSE_ENOUGH, false);           //automatic as this will ONLY ever be true when invoked by CheckProximityWorker!
        Fragment frag = new BuddyHomeFragment();
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
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).commit();
    }
    public void toProfileSetupFragment(){
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new ProfileSetupFragment()).commit();
    }
    @Override
    public void toUserProfileFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new UserProfileFragment()).commit();
    }

    @Override
    public void toSearchUserFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new SearchUserFragment()).commit();
    }

    //Required implementation of methods from MAFragmentsListener
    //Purpose:      Displays the corresponding Fragment classes
    @Override
    public void toOtherProfileFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new OtherProfileFragment()).commit();
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
    public void toEditTrustedContactsFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new EditTrustedContactsFragment()).commit();
    }

    @Override
    public void toHelpFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new HelpFragment()).commit();
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
    public void toUserBuddyRequestFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new UserBuddyRequestsFragment()).commit();
    }

    @Override
    public void toPotentialBuddyFragment(String userId, String dialogKey, String buddyRequestId) {
        Bundle bundle = new Bundle();;
        bundle.putString(KEY_USERID, userId);
        bundle.putString(KEY_DIALOG, dialogKey);
        bundle.putString(KEY_BUDDYREQUESTID, buddyRequestId);
        Fragment frag = new PotentialBuddyFragment();
        frag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();

    }

    //Purpose:      called by other Fragments when current User is a Buddy looking to keep track of their requests
    public void setWatchRequests(boolean answer){
        if(answer){
            receivedRequestCount = 10;
            setRunRequestHandling(true);
            watchRequests();
        }
    }

    private void setReceivedRequestCount(int newCount){
        receivedRequestCount = newCount;
    }
    private void setRunRequestHandling(boolean answer){
        runRequestHandling = answer;
    }

    private void watchRequests(){
        Log.d(TAG, "in watchRequests() receivedRequestCount = " + receivedRequestCount);
        //Package data to send the counter
        Data data = new Data.Builder()
                .putInt(KEY_INITIAL_COUNT, receivedRequestCount)       //send the counter
                .build();

        //Create the request
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(HandleBuddyRequestsWorker.class)
                .setInputData(data)         //send data
                .setInitialDelay(8, TimeUnit.SECONDS)      //wait 5 seconds before doing it         //TODO: don't hardcode, make this time frame a constant
                .build();

        //Queue up the request
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniqueWork(
                        "HandleBuddyRequests request",
                        ExistingWorkPolicy.REPLACE,         //says, if it does repeat, replace the new request with the old one
                        (OneTimeWorkRequest) request
                );

        //Listen to information from the request
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())      //returns a live data
                .observe(owner, new Observer<WorkInfo>() {

                    //called every time WorkInfo is changed
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        Log.d(TAG, "watchRequests(): onChanged()");

                        if (workInfo != null) {

                            //Check if finished:
                            if(workInfo.getState().isFinished()){
                                if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                                    Log.d(TAG, "watchRequest(): Request succeeded ");

                                    Data output = workInfo.getOutputData();

                                    //Check if any of our sent requests were approved
                                    boolean anyApproved = output.getBoolean(KEY_ISAPPROVED, false);
                                    Log.d(TAG, "watchRequests(): anyApproved=" + anyApproved);

                                    if(anyApproved){
                                        int sentRequestIndex = output.getInt(KEY_APPROVED_REQUEST, -1);
                                        Log.d(TAG, "watchRequests(): sentRequestindex=" + sentRequestIndex);

                                        //break out of the loop bc there's an approved request now
                                        setRunRequestHandling(false);
                                        Toast.makeText(getApplicationContext(), "One of your sent requests was approved!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        int numOfReceived = output.getInt(KEY_NUM_RECEIVED, 0);
                                        Log.d(TAG, "watchRequests(): numOfreceived=" + numOfReceived);
                                        setReceivedRequestCount(numOfReceived);

                                        boolean receivedRequestsChanged = output.getBoolean(KEY_CHANGED, false);
                                        Log.d(TAG, "watchRequests(): receivedRequestsChanged=" + receivedRequestsChanged);
                                        if (receivedRequestsChanged) {        //hopefully should mean the # of received requests got bigger --> show it
                                            Toast.makeText(getApplicationContext(), "You got a new BuddyRequest!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    Log.d(TAG, "watchRequests(): runRequestHanding=" + runRequestHandling);
                                    if(runRequestHandling) {
                                        Log.d(TAG, "watchRequests():  recursive call it!");
                                        watchRequests();         //to infinitely do it!
                                    }
                                }
                                else {
                                    Log.d(TAG, "Request didn't succeed, status=" + workInfo.getState().name());
                                    watchRequests();
                                }
                            }
                        }
                    }
                });
    }

    //Purpose:      Finds out at which stage the of a BuddyTrip (if any) the current user is at in order to begin on the correct HomeFrag:
    private String findUserBuddyStatus(){
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
    //TODO: remove this boilerplate code lol
    private Fragment getCorrespondingFrag(String fragLabel){
        if(fragLabel.equals(DEFAULT_HOME)){
            return new DefaultHomeFragment();
        }
        else if(fragLabel.equals(BUDDY_HOME_FIND_MODE)){
            Bundle bundle = new Bundle();;
            bundle.putString(KEY_MODE, BuddyHomeFragment.KEY_FIND_BUDDY_MODE);
            Fragment frag = new BuddyHomeFragment();
            frag.setArguments(bundle);
            return frag;
        }
        else if(fragLabel.equals(BUDDY_HOME_MEETUP_MODE)){
            //1.) Must find the meetUpId:
            String meetUpId = userBuddyInstance.getBuddyMeetUpInstance().getObjectId();

            Bundle bundle = new Bundle();;
            bundle.putString(KEY_MODE, BuddyHomeFragment.KEY_MEET_BUDDY_MODE);
            bundle.putString(BuddyHomeFragment.KEY_BUDDYMEETUPID, meetUpId);
            bundle.putBoolean(BuddyHomeFragment.KEY_CLOSE_ENOUGH, false);
            Fragment frag = new BuddyHomeFragment();
            frag.setArguments(bundle);
            return frag;
        }
        else if(fragLabel.equals(BUDDY_HOME_MEETUP_MODE_CLOSE)){
            String meetUpId = userBuddyInstance.getBuddyMeetUpInstance().getObjectId();

            Bundle bundle = new Bundle();
            bundle.putString(KEY_MODE, BuddyHomeFragment.KEY_MEET_BUDDY_MODE);
            bundle.putString(BuddyHomeFragment.KEY_BUDDYMEETUPID, meetUpId);
            bundle.putBoolean(BuddyHomeFragment.KEY_CLOSE_ENOUGH, true);           //automatic as this will ONLY ever be true when invoked by CheckProximityWorker!
            Fragment frag = new BuddyHomeFragment();
            frag.setArguments(bundle);
            return frag;
        }
        else if(fragLabel.equals(BUDDY_HOME_ONTRIP_MODE)){
            //TODO: bc this is not yet implemented, will later need to input extra info
            Bundle bundle = new Bundle();;
            bundle.putString(KEY_MODE, BuddyHomeFragment.KEY_ON_TRIP_MODE);
            Fragment frag = new BuddyHomeFragment();
            frag.setArguments(bundle);
            return frag;
        }
        else{
            Log.d(TAG, "getCurrentHomeFrag(): error - currentHomeFrag string doesn't match any of the MainActivity keys?");
            return null;
        }
    }

    @Override
    //Purpose:      To start checking for proximity this distance away for this meetUpId
    public void startCheckingProximity(int meters, String meetUpId) {
        keepCheckingProximity = true;
        startCheckingProximityWorker(meters, meetUpId);
    }

    @Override
    public void stopCheckingProximity() {
        keepCheckingProximity = false;
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(CHECK_PROXIMITY_WORKER_TAG);       //probably will need to refine by Tags so specific requests get canceled
    }

    /**
     * Purpose:     Makes a OneTimeWorkRequest on the UpdateLocationWorker class infinitely as long as keepTracking = true. keepTracking may be turned off through the stopAllRequests()
     */
    private void startCheckingProximityWorker(int meters, String meetUpId){
        Log.d(TAG, "startCheckingProximityWorker()");
        //Package data to send the counter
        /*Data data = new Data.Builder()
                .putInt(KEY_SENDCOUNTER, counter)       //send the counter
                .build();*/

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

                        //If workInfo is there and it is succeeded --> update the text
                        if (workInfo != null) {
                            //Check if finished:
                            if(workInfo.getState().isFinished()){
                                if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                                    Log.d(TAG, "Request succeeded ");
                                    //get output:
                                    Data output = workInfo.getOutputData();
                                    Log.d(TAG, "startCheckingProximityWorker(): output = " + output.toString());

                                    boolean result = output.getBoolean(KEY_PROXIMITY_RESULT, false);
                                    Log.d(TAG, "startCheckingProximityWorker(): result=" + result);
                                    //If user is close enough --> invoke BuddyHomeFrag with it as true:
                                    if(result){

                                        //Invoke overlay to display if not already doing so:
                                        if(!currentHomeFrag.equals(BUDDY_HOME_MEETUP_MODE_CLOSE)) {
                                            currentHomeFrag = BUDDY_HOME_MEETUP_MODE_CLOSE;
                                            Bundle bundle = new Bundle();
                                            bundle.putString(KEY_MODE, BuddyHomeFragment.KEY_MEET_BUDDY_MODE);
                                            bundle.putString(BuddyHomeFragment.KEY_BUDDYMEETUPID, meetUpId);
                                            bundle.putBoolean(BuddyHomeFragment.KEY_CLOSE_ENOUGH, true);           //automatic as this will ONLY ever be true when invoked by CheckProximityWorker!
                                            Fragment frag = new BuddyHomeFragment();
                                            frag.setArguments(bundle);
                                            fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, frag).commit();
                                            //NOTE: We DO NOT stop this worker here as we only want to stop checking for proximity when the user interacts with the correct overlays!
                                        }
                                    }

                                    if(keepCheckingProximity) {
                                        startCheckingProximityWorker(meters, meetUpId);              //to infinitely do it!
                                    }
                                }
                                else {
                                    Log.d(TAG, "Request didn't succeed, status=" + workInfo.getState().name());
                                    startCheckingProximityWorker(meters, meetUpId);
                                }
                            }
                        }
                    }
                });
    }

}