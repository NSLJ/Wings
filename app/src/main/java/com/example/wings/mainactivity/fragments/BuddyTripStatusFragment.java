package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.parse.ParseUser;

import org.parceler.Parcels;

/**
 * BuddyTripStatusFragment.java
 * Purpose:                         Invoked when the floating Buddy button (maintained by MainActivity) is clicked AND user is either on a BuddyMeetUp or BuddyTrip. This fragment displays important BuddyMeetUp/BuddyTrip details
 *                                  such as the other Buddy's information, destination, etc
 *
 *
 * */
public class BuddyTripStatusFragment extends Fragment {
    private static final String TAG = "BuddyTripStatusFragment";
    public static final String KEY_DATA = "parcelableObjectGiven";

    private MAFragmentsListener listener;
    ParseUser currUser = ParseUser.getCurrentUser();
    ParseUser otherUser;
    BuddyMeetUp meetUpInstance;


    public BuddyTripStatusFragment() {}

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
        if (getArguments() != null) {
            ParcelableObject dataReceived = Parcels.unwrap(getArguments().getParcelable(KEY_DATA));
            otherUser = dataReceived.getParseUser();
            meetUpInstance = dataReceived.getBuddyMeetUp();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buddy_trip_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}