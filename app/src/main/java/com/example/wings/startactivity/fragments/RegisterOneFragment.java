package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.models.User;
import com.example.wings.startactivity.SAFragmentsListener;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * RegisterOneFragment.java
 * Purpose:         This displays the first screen of registering a new user/creating an account!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */
public class RegisterOneFragment extends Fragment {

    public static final String TAG = "RegisterOneFragment";
    private SAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private Button backLoginBtn;
    private Button signUpBtn;
    private String usernametxt;
    private String passwordtxt1;
    private String passwordtxt2;
    private EditText password1;
    private EditText password2;
    private EditText username;

    public RegisterOneFragment() {}    // Required empty public constructor

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
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_register_one.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_one, container, false);
    }

    @Override
    /**
     * Purpose:         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {

        signUpBtn = view.findViewById(R.id.signUpBtn);
        backLoginBtn = view.findViewById(R.id.backLoginBtn);

        username = view.findViewById(R.id.username);
        password1 = view.findViewById(R.id.password1);
        password2 = view.findViewById(R.id.password2);

        User user = new User();

        //Changes the Fragment to the LoginFragment via the StartActivity!
        backLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toLoginFragment();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernametxt = username.getText().toString();
                passwordtxt1 = password1.getText().toString();
                passwordtxt2 = password2.getText().toString();

                if (usernametxt.equals("") || passwordtxt1.equals("") || passwordtxt1.equals("")) {
                    Toast.makeText(getContext(), "Please complete the form.", Toast.LENGTH_SHORT).show();
                } else if (!(passwordtxt1.equals(passwordtxt2))) {
                    Toast.makeText(getContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    ParseUser user = new ParseUser();
                    user.setUsername(usernametxt);
                    user.setPassword(passwordtxt1);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null) {
                                Toast.makeText(getContext(), "Successfully signed up!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Sign up error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        /*Changes the Fragment to RegisterTwoFragment via the StartActivity!
        registerTwoBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toRegisterTwoFragment();
            }
        });*/

    }
}