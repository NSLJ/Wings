package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.models.ParcelUser;
import com.example.wings.models.User;
import com.example.wings.startactivity.SAFragmentsListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


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
    private SAFragmentsListener listener;
    private List<ParseUser> currUsers;          //To error check if new user is already a user

    private Button signUpBtn;
    private TextView tvToLogin;
    private EditText etFName;
    private EditText etLName;
    private EditText etEmail;

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
        currUsers = new ArrayList<ParseUser>();

        //1.) Get references to Views:
        signUpBtn = view.findViewById(R.id.signUpBtn);
        tvToLogin = view.findViewById(R.id.tvToLogin);
        etFName = view.findViewById(R.id.etFName);
        etLName = view.findViewById(R.id.etLName);
        etEmail= view.findViewById(R.id.etEmail);

        //2.) Set on click listeners:
        //2a.) clickable login link --> back to Login page
        tvToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toLoginFragment();
            }
        });

        //2b.) signUpBtn --> check user inputs and attempt to register User:
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ": sign up button clicked!");
                if(etEmail != null && etFName != null && etLName != null) {
                    String email = etEmail.getText().toString();

                    //Check if email and username is valid:
                    String errorString = null;
                    try {
                        errorString = isValid(email);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // If no error string --> user's info is valid!
                    //      Create a user and send to RegisterTwoFragment:
                    if (errorString.equals("")) {
                        User user = new User();
                        user.setFirstName(etFName.getText().toString());
                        user.setLastName(etLName.getText().toString());
                        user.setEmail(email);
                        user.setUsername(email.substring(0, email.length() - 8));
                        user.setProfileSetUp(false);            //ensure flag on --> show this user has not set up profile

                        listener.toRegisterTwoFragment(user);
                    } else {
                        showLongTopToast(errorString);
                    }
                }
                else{
                    showLongTopToast("You did not fill in all of the fields!");
                }
            }
        });
    }


    /**
     * Purpose:         Determines if the given email and username. If not, the method returns the corresponding error String to display. If valid, the String will be empty.
     * @param email
     * @return
     */
    private String isValid(String email) throws InterruptedException {
        Log.d(TAG, "in isValid()");
        String result = "";

        //2.) Check if email ends with "@cpp.edu":
        if(email.length() > 8) {
            String lastChars = email.substring(email.length() - 8, email.length());
            if (!(lastChars.equals("@cpp.edu"))) {
                result += "Your email does not end with \"@cpp.edu\"";
            }
            //otherwise --> email is valid, we can check if the username is unique! i.e. this email has not already been registered
            else{
                String username = email.substring(0, email.length()-8);
                Log.d(TAG, "Checking if username=" + username + " is valid! Querying for all existing users");
                result += queryUsernames();

                boolean validUsername = validUsername(username);
                Log.d(TAG, "validUsername = "+validUsername);
                if(!validUsername) {
                    result += "This username/email is already registered!";
                }
            }
        }
        else{       //if email wasn't even long enough:
            result += "Your email does not end with \"@cpp.edu\"";
        }

        return result;
    }
    /**
     * Purpose;             Obtains a list of all current users and updates the "currUsers" List field. Returns an error string if error occurs, and "" if not
     */
    private String queryUsernames(){
        Log.d(TAG, "in queryUsernames() ");
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        //To get all users:
        try {
            currUsers.addAll(query.find());
            Log.i(TAG, "queryUsernames():  -users: " + currUsers.toString());
            return "";
        } catch (ParseException e) {
            Log.e(TAG, "queryUsernames(): failure:  error=", e);
            return "Your session is expired! Please try again.";
        }
    }

    /**
     * Purpose:             Returns whether the given username is in the List of current users. (returns whether the given username is unique/valid)
     * @param username, the username to test uniqueness for
     * @return whether or not the username is unique/valid
     */
    private boolean validUsername(String username) {
        Log.d(TAG, "in validUsername(" + username + ")");
        Log.d(TAG, "validUsername(): currUsers = " + currUsers.toString());
        for(ParseUser user: currUsers) {
            if(user.getUsername().equalsIgnoreCase(username)){
                return false;
            }
        }
        return true;
    }

    private void showLongTopToast(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

}