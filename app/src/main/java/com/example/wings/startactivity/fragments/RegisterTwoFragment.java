package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wings.R;
import com.example.wings.startactivity.SAFragmentsListener;

/**
 * RegisterTwoFragment.java
 * Purpose:         This displays the second screen of registering a new user/creating an account!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */
public class RegisterTwoFragment extends Fragment {

    private SAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private Button registerOneBttn;
    private Button loginBttn;
    private Button profileSetupBttn;

    public RegisterTwoFragment() {}    // Required empty public constructor

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
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_register_two.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_two, container, false);
    }

    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {

     /*   loginBttn = view.findViewById(R.id.loginBttn);
        registerOneBttn = view.findViewById(R.id.registerOneBttn);
        profileSetupBttn = view.findViewById(R.id.profileSetupBttn);

        //Changes the Fragment to the LoginFragment via the StartActivity!
        loginBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toLoginFragment();
            }
        });

        //Changes the Fragment to RegisterTwoFragment via the StartActivity!
        registerOneBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toRegisterOneFragment();
            }
        });
        //Changes the Fragment to RegisterTwoFragment via the StartActivity!
        profileSetupBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toProfileSetupFragment();
            }
        });*/

    }
}