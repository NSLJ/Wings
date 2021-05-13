package com.example.wings.mainactivity.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wings.R;

/**
 * Purpose:         The screen shown to the user to manage their buddy requests. Shows all the buddy requests they sent, and the ones they have yet to respond to
 */


public class UserBuddyRequestsFragment extends Fragment {



    public UserBuddyRequestsFragment() {
        // Required empty public constructor
    }


    public static UserBuddyRequestsFragment newInstance(String param1, String param2) {
        UserBuddyRequestsFragment fragment = new UserBuddyRequestsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
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
}