package com.example.wings.startactivity.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wings.R;
import com.example.wings.startactivity.SAFragmentsListener;

/**
 * EditTrustedContactsFragment.java
 * Purpose:            This displays the screen for user to add trusted contact members when user is in a danger situation
 */
public class EditTrustedContactsFragment extends Fragment {
    private static final String TAG = "EditTrustedContactsFragment";
    private SAFragmentsListener listener;
    private int numTCList = 0;

    public EditTrustedContactsFragment() {
    }        // Required empty public constructor

    @SuppressLint("ResourceAsColor")
    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Set limit max 5 members in the Trusted Contact List
        if (numTCList > 5) {
            Toast toast = Toast.makeText(getContext(), "Can only have up to 5 members!", Toast.LENGTH_LONG);
        }

    }

    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_trusted_contacts, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);        //let know that there is an options menu to inflate
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

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
    /**
     * Purpose:     Inflates a specific top navigation bar for this fragment, allowing the user to logout without setting up their profile, to go to SettingsFragments, or go to HelpFragment
     */
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profilesetup_navigation, menu);
    }

    /**
     * Purpose:     Attaches events when menu items are pushed.
     *
     * @param item, which item was selected
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

}
