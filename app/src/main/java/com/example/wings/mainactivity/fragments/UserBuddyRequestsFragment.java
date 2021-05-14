package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.adapters.ChooseBuddyAdapter;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose:         The screen shown to the user to manage their buddy requests. Shows all the buddy requests they sent, and the ones they have yet to respond to
 */


public class UserBuddyRequestsFragment extends Fragment {
    private static final String TAG = "UserBuddyRequestsFragment";

    private MAFragmentsListener listener;
    private ParseUser currentUser = ParseUser.getCurrentUser();

    //Views:
    private RecyclerView rvSentRequests;
    private RecyclerView rvReceivedRequests;
    private Button btnBack;
    ChooseBuddyAdapter sentRequestsAdapter;
    ChooseBuddyAdapter receivedRequestAdapter;

    //Models:
    //Sent requests:
    List<ParseUser> peopleSentTo;
    List<Double> distanceFromSent;
    List<ParseUser> peopleReceivedFrom;
    List<Double> distanceFromReceivers;

    public UserBuddyRequestsFragment() {}


    public static UserBuddyRequestsFragment newInstance(String param1, String param2) {
        UserBuddyRequestsFragment fragment = new UserBuddyRequestsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

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

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_buddy_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvSentRequests = view.findViewById(R.id.rvSent);
        rvReceivedRequests = view.findViewById(R.id.rvReceived);
        btnBack = view.findViewById(R.id.btnBack);

        peopleSentTo = new ArrayList<>();
        distanceFromSent = new ArrayList<>();
        peopleReceivedFrom = new ArrayList<>();
        distanceFromReceivers = new ArrayList<>();


        //Set on click listener:
        //2.) Create an ChooseBuddyAdapter.OnClickListener to create a ChooseBuddyAdapter:
        ChooseBuddyAdapter.OnClickListener onClickListener = new ChooseBuddyAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                //I think position = corresponds to the indexes in the List<> models but may be backwards or something:
                //TODO: open a simple yes or no dialog box
                Toast.makeText(getContext(), "You clicked on a request, I should show a dialog rn", Toast.LENGTH_SHORT).show();
            }
        };

        sentRequestsAdapter = new ChooseBuddyAdapter(getContext(), peopleSentTo, distanceFromSent, onClickListener);

        //TODO: this is not done like at all, please do not use. I stopped in the middle of it due to time
    }
}