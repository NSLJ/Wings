package com.example.wings.mainactivity;

import android.view.View;

import com.example.wings.models.ParcelableObject;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.example.wings.models.inParseServer.TrustedContact;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.parse.ParseUser;

import java.util.List;

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
    void startCheckingProximity(int meters, BuddyMeetUp meetUpInstance);
    void stopCheckingProximity();

    //Navigating to fragments:
    //Special case frags:
    void toDefaultHomeFragment();
    void toCurrentHomeFragment();        //used when implementing back buttons from other fragments --> always goes nack to the most current HomeFrag + mode selected

   // void toBuddyHomeFragment(String mode);                                                //intended for mode = default
    void toBuddyHomeFragment(ParcelableObject data);
    void toConfirmBuddyHomeFragment(String modeKey, String otherUserId);
    void toConfirmBuddyHomeFragment(String modeKey, String otherUserId, String buddyRequestId);

    void toBuddyTripStatusFragment(String mode, Buddy otherBuddy, WingsGeoPoint tripDestination);


    //Other frags:
    void toHomeFragment(String modeKey);
    void toProfileSetupFragment();
    void toProfileSetupFragment(List<TrustedContact> trustedContacts);          //so EditTrustedContactsFrag may pass the finalized list of Trusted Contacts
    void toUserProfileFragment();
    void toSearchUserFragment();
    void toOtherProfileFragment(ParseUser userToShow);
    void toChooseBuddyFragment();
    void toSettingsFragment();
    void toEditTrustedContactsFragment(List<TrustedContact> trustedContacts);           //So can persist the same list of TrustedContacts during profile set up
    void toHelpFragment();
    void toUserBuddyRequestFragment();
    void toReviewFragment(ParseUser userReviewFor);
    void sendArrivedMessage();

    void startTimer(boolean isInitialWait, long est);
    void stopTimer();
}
