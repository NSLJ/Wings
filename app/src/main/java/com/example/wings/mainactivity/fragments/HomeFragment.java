package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;

/**
 * HomeFragment.java
 * Purpose:         This displays the default screen of the app!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */
public class HomeFragment extends Fragment {

    private MAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private Button chooseBuddyBttn;

    public HomeFragment() {}    // Required empty public constructor

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
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_home.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //Changes the Fragment to the ChooseBuddyFragment via the MainActivity!
        chooseBuddyBttn = view.findViewById(R.id.registerBttn);
        chooseBuddyBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
            }
        });
    }
}