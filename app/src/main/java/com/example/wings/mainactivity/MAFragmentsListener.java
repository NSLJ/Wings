package com.example.wings.mainactivity;

import android.view.View;

import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.parse.ParseUser;

/**
 * MAFragmentsListener.java
 * Purpose:         Allows a medium for Fragments to communicate with MainActivity in order to display the correct Fragments at the right time. Essentially shows all Fragments NOT in the Bottom Navigation
 *                  Menu (as those can be navigatied to from any screen).
 */

//NOTE:     Because there is only one listener used by all fragments and the MainActivity, this means any Fragment can invoke any other Fragment easily, which is something we may want to change later. This
//          can be done with in-class listeners later so that only specific Fragments can invoke specific methods  i.e HomeFragment.HomeFragmentListener{}
public interface MAFragmentsListener {
    public void setRestrictScreen(boolean answer);
    public void toHomeFragment(String modeKey);
    public void toProfileSetupFragment();
    public void toUserProfileFragment();
    public void toSearchUserFragment();
    public void toOtherProfileFragment();
    public void toChooseBuddyFragment();
    public void toSettingsFragment();
    public void toEditTrustedContactsFragment();
    public void toHelpFragment();
    public void setBuddyRequestBttn(boolean answer);
    public void setBuddyRequestBttnOnClickListener(View.OnClickListener onClickListener);
    public void toUserBuddyRequestFragment();
    public void toPotentialBuddyFragment(String potentialBuddyId, String keyShowDialog, String buddyRequestId);
    public void toBuddyHomeFragment(String modeKey, String meetUpId, boolean closeEnough);
    public void toBuddyHomeFragment(String modeKey);
    public void toDefaultHomeFragment();
    public void toConfirmBuddyHomeFragment(String modeKey, String otherUserId);
    public void toConfirmBuddyHomeFragment(String modeKey, String otherUserId, String buddyRequestId);
    public void toCurrentHomeFragment();        //used when implementing back buttons from other fragments --> always goes nack to the most current HomeFrag + mode selected
    public void toBuddyTripStatusFragment(ParseUser otherUser, BuddyMeetUp meetUpInstance);
    public void startCheckingProximity(int meters, String meetUpId);
    public void stopCheckingProximity();
}
