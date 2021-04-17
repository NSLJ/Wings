package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wings.R;
import com.example.wings.startactivity.SAFragmentsListener;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


/**
 * LoginFragment.java
 * Purpose:         This displays the first screen of the app, the Login page!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */
public class LoginFragment extends Fragment {

    private SAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private Button login;
    private Button register;
    private String usernametxt;
    private String passwordtxt;
    private EditText password;
    private EditText username;

    public LoginFragment() {}    // Required empty public constructor

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
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_login.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    /**
     * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        register = view.findViewById(R.id.register);
        login = view.findViewById(R.id.login);

        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);

        //Changes the Fragment to the RegisterOneFragment via the StartActivity!
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toRegisterOneFragment();
            }
        });

        //Calls onLogin() when want to go to Home page (MainActivity)!
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernametxt = username.getText().toString();
                passwordtxt = password.getText().toString();

                ParseUser.logInInBackground(usernametxt, passwordtxt, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(user != null){
                            listener.onLogin();
                        } else {
                            Toast.makeText(getContext(), "This user doesn't exist. Please Sign-up!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}