package com.example.wings.mainactivity;

import android.view.View;

import com.example.wings.models.ParcelableObject;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.parse.ParseUser;

/**
 * MAFragmentsListener.java
 * Purpose:         Allows a medium for Fragments to communicate with MainActivity in order to display the correct Fragments at the right time. Essentially shows all Fragments NOT in the Bottom Navigation
 *                  Menu (as those can be navigatied to from any screen).
 */

//NOTE:     Because there is only one listener used by all fragments and the MainActivity, this means any Fragment can invoke any other Fragment easily, which is something we may want to change later. This
//          can be done with in-class listeners later so that only specific Fragments can invoke specific methods  i.e HomeFragment.HomeFragmentListener{}
public interface MAFragmentsListener {
    //Helper methods to toggle views/invoke functions that need to be continuous:
    void setRestrictScreen(boolean answer);
    void setBuddyRequestBttn(boolean answer);
    void setBuddyRequestBttnOnClickListener(View.OnClickListener onClickListener);
    void startCheckingProximity(int meters, String meetUpId);
    void stopCheckingProximity();

    //Navigating to fragments:
    //Special case frags:
    void toDefaultHomeFragment();
    void toCurrentHomeFragment();        //used when implementing back buttons from other fragments --> always goes nack to the most current HomeFrag + mode selected

   // void toBuddyHomeFragment(String mode);                                                //intended for mode = default
    void toBuddyHomeFragment(ParcelableObject data);


    void toBuddyHomeFragment(String mode, BuddyMeetUp meetUpInstance, Buddy otherBuddyInstance, ParseUser otherUser, Buddy currUserBuddyInstance, boolean closeEnough);          //intended for mode = meetUp
    void toBuddyHomeFragment(String mode, BuddyTrip buddyTripInstance, ParseUser otherUser, Buddy otherBuddyInstance);           //intended for mode = onTrip

    void toConfirmBuddyHomeFragment(String modeKey, String otherUserId);
    void toConfirmBuddyHomeFragment(String modeKey, String otherUserId, String buddyRequestId);

    void toBuddyTripStatusFragment(Buddy otherBuddy, BuddyMeetUp meetUpInstance, String mode);



    //Other frags:
    void toHomeFragment(String modeKey);
    void toProfileSetupFragment();
    void toUserProfileFragment();
    void toSearchUserFragment();
    void toOtherProfileFragment();
    void toChooseBuddyFragment();
    void toSettingsFragment();
    void toEditTrustedContactsFragment();
    void toHelpFragment();
    void toUserBuddyRequestFragment();
    void toPotentialBuddyFragment(String potentialBuddyId, String keyShowDialog, String buddyRequestId);



}
