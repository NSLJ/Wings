package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wings.R;
import com.example.wings.adapters.PotentialBuddyAdapter;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.Buddy;

import java.util.ArrayList;
import java.util.List;

/**
 * ChooseBuddyFragment.java
 * Purpose:         This displays all possible buddies for the user to choose one they'd like!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */
public class ChooseBuddyFragment extends Fragment {

    private MAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private RecyclerView recyclerView;
    private List<Buddy> potentialBuddies;
    private PotentialBuddyAdapter buddyAdapter;

    public ChooseBuddyFragment() {}    // Required empty public constructor

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
    /**
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_choose_buddy.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_buddy, container, false);
    }

    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        
        //1.) Connect views:
        potentialBuddies = new ArrayList<>();
        recyclerView = view.findViewById(R.id.rvBuddies);
        buddyAdapter = new PotentialBuddyAdapter(getContext(), potentialBuddies);

        //2.) Set up recycler view:
        recyclerView.setAdapter(buddyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        //3.) Populate with buddies:
        queryPotentialBuddies();
    }

    private void queryPotentialBuddies() {
    }
}