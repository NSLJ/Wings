package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.startactivity.SAFragmentsListener;
import com.example.wings.startactivity.StartActivity;

public class ProfileSetupFragment extends Fragment {

    private static final String DEBUG_TAG = "ProfileSetupFragment";
    private SAFragmentsListener listener;

    public ProfileSetupFragment() {}        // Required empty public constructor

    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement SAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SAFragmentsListener) {
            listener = (SAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement SAFragmentsListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);        //let know that there is an options menu to inflate
    }

    @Override
    /**
     * Purpose:     Inflates a specific top navigation bar for this fragment, allowing the user to logout without setting up their profile, to go to SettingsFragments, or go to HelpFragment
     */
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profilesetup_navigation, menu);
    }

    /**
     * Purpose:         Attaches events when menu items are pushed.
     * @param item, which item was selected
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_logout:
                /*1.) If there is an actual user logged in, log them out
                    if(ParseUser.getCurrentUser() != null){
                        ParseUser.logOut();
                    }
                */

                //2.) Switch to LoginFragment:
                listener.toLoginFragment();
                return true;
            case R.id.action_settings:
                listener.toSettingsFragment();
                break;
            case R.id.action_help:
                listener.toHelpFragment();
                break;
            default:
                return false;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }
}