package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.home.BuddyHomeFragment;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

/**
 * BuddyTripStatusFragment.java
 * Purpose:                         Invoked when the floating Buddy button (maintained by MainActivity) is clicked AND user is either on a BuddyMeetUp or BuddyTrip. This fragment displays important BuddyMeetUp/BuddyTrip details
 *                                  such as the other Buddy's information, destination, etc
 *
 *
 * */
//TODO: make the layout scrollable, and add a cancel button to cancel the trip
public class BuddyTripStatusFragment extends Fragment {
    private static final String TAG = "BuddyTripStatusFragment";
    public static final String KEY_DATA = "parcelableObjectGiven";
    public static final String MEET_BUDDY_MODE = "we are given a BuddyMeetUp";
    public static final String ON_TRIP_MODE = "we are given a BuddyTrip";

    private MAFragmentsListener listener;
    ParseUser currUser = ParseUser.getCurrentUser();
    ParseUser otherUser;
    Buddy otherBuddy;
   // BuddyMeetUp meetUpInstance;
   // BuddyTrip buddyTripInstance;
    WingsGeoPoint tripDestination;
    String mode;

    //Views:
    ImageView ivProfilePic;
    TextView tvName;
    TextView tvTripStatus;
    TextView tvUserBuddyId;
    TextView tvOtherBuddyId;
    TextView tvUserDestination;
    TextView tvCommonDestination;
    Button btnBack;

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
            mode = dataReceived.getMode();
            tripDestination = dataReceived.getWingsGeoPoint();
            otherBuddy = dataReceived.getOtherBuddy();
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

        btnBack = view.findViewById(R.id.btnBack);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvName = view.findViewById(R.id.tvTitle);
        tvTripStatus = view.findViewById(R.id.tvTripStatus);
        tvUserBuddyId = view.findViewById(R.id.tvUserBuddyId);
        tvOtherBuddyId = view.findViewById(R.id.tvTripDestination);
        tvUserDestination = view.findViewById(R.id.tvUserDestination);
        tvCommonDestination = view.findViewById(R.id.tvCommonDestination);

        //Set up btnBack:
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toCurrentHomeFragment();
            }
        });

        listener.setBuddyRequestBttnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "You are already on this screen!", Toast.LENGTH_SHORT).show();
            }
        });
        otherBuddy.getObjectId();
        Buddy userBuddyInstance = (Buddy) currUser.getParseObject(User.KEY_BUDDY);
        try {
            userBuddyInstance.fetchIfNeeded();
            userBuddyInstance.getObjectId();

            //1.) Populate ID's and destinations:
            tvUserBuddyId.setText("Your ID:  " + userBuddyInstance.getObjectId());
            tvOtherBuddyId.setText("Their ID:  " + otherBuddy.getObjectId());
            tvUserDestination.setText("Your intended destination:  " + currUser.getString(User.KEY_DESTINATIONSTR));         //TODO: input destinationStr field into Buddy class and destination field into BuddyMeetUp class
            tvCommonDestination.setText("Trip destination:  "+ Math.round(tripDestination.getLatitude()*1000.0)/1000.0 +", " + Math.round(tripDestination.getLongitude()*1000.0)/1000.0+")");

            //2.) Populate otherUser information:
            otherUser = otherBuddy.getUser();
            if(otherUser != null) {
                ParseFile imageFile = otherUser.getParseFile(User.KEY_PROFILEPICTURE);
                if (imageFile != null) {
                    try {
                        Glide.with(getContext()).load(imageFile.getFile()).into(ivProfilePic);
                    } catch (ParseException error) {
                        error.printStackTrace();
                    }
                }
                tvName.setText(otherUser.getString(User.KEY_FIRSTNAME));
            }

            //3.) Update trip status to whatever context this was called from (either meetup mode or ontrip mode):
            if(mode.equals(MEET_BUDDY_MODE)){
                tvTripStatus.setText("Trip Status:  On meeting up");
            }
            else if(mode.equals(ON_TRIP_MODE)){
                tvTripStatus.setText("Trip Status:  On trip walking to destination");
            }
            else{
                tvTripStatus.setText("Error - mode not matching either key");
                Log.d(TAG, "Error - mode was not meetup mode or ontrip mode");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}