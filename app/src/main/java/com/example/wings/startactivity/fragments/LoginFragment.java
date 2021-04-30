package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wings.R;
import com.example.wings.models.User;
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
    private static final String DEBUG_TAG = "LoginFragment";
    public static final String KEY_SEND_PROFILESETUPFRAG = "ProfileSetupFrag?";

    private SAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private Button btnLogin;
    private TextView tvRegister;
    private String usernameTxt;
    private String passwordTxt;
    private EditText etPassword;
    private EditText etUsername;

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
        //1.) Initialize Views:
        tvRegister = view.findViewById(R.id.tvRegister);
        btnLogin = view.findViewById(R.id.btnLogin);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);

        //2.) Set listeners::
        // 2a.) btnLogin --> obtain user input and attempts to login through Parse Server
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "btnLogin : onClick()");
                usernameTxt = etUsername.getText().toString();
                passwordTxt = etPassword.getText().toString();

                ParseUser.logInInBackground(usernameTxt, passwordTxt, new LogInCallback() {

                    @Override
                    public void done(ParseUser user, ParseException e) {
                        //if there is a user --> tell StartActivity of successful login
                        if(user != null){
                            //Notify user of successful login:
                            Toast toast = Toast.makeText(getContext(), "You're logged in!", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP, 0,0);
                            toast.show();

                            //Send user to HomeFrag or ProfileSetupFrag depending if they setup their profile yet:
                            Log.d(DEBUG_TAG, "profileSetUp = " + user.getBoolean(User.KEY_PROFILESETUP));
                            if(user.getBoolean(User.KEY_PROFILESETUP)){
                                listener.onLogin("go to HomeFrag");     //just something not the ProfileSetupFrag key
                            }
                            else {
                                listener.onLogin(KEY_SEND_PROFILESETUPFRAG);
                            }

                        } else {
                            Toast.makeText(getContext(), "This user doesn't exist. Please Sign-up!", Toast.LENGTH_SHORT).show();
                            Log.d(DEBUG_TAG, "logInInBackground(): failed" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        //Changes the Fragment to the RegisterOneFragment via the StartActivity!
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toRegisterOneFragment();
            }
        });
    }
}