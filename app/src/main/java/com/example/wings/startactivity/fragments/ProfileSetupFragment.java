package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.startactivity.SAFragmentsListener;
import com.example.wings.startactivity.StartActivity;

/**
 * ProfileSetupFragment.java
 * Purpose:            This displays the profile setup screen for the user to complete all required user information before using the app. This includes
 *                      PIN number, profile picture, and Trusted Contacts.
 */
public class ProfileSetupFragment extends Fragment {

    private static final String DEBUG_TAG = "ProfileSetupFragment";
    private SAFragmentsListener listener;
    private Button completeBtn;

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
     * Purpose:     Attaches events when menu items are pushed.
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
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        completeBtn = view.findViewById(R.id.completeBtn);

        //Changes the Fragment to the Home Fragment via the StartActivity!
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { listener.onLogin();}
        });
    }
}