package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.example.wings.models.User;
import com.example.wings.startactivity.SAFragmentsListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;


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

    private Button backLoginBtn;
    private Button signUpBtn;
    private EditText etFName;
    private EditText etLName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etRetypedPass;

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
        backLoginBtn = view.findViewById(R.id.backLoginBtn);
        etFName = view.findViewById(R.id.etFName);
        etLName = view.findViewById(R.id.etLName);
        etEmail= view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etRetypedPass = view.findViewById(R.id.etRetypedPassword);

        //2.) Set on click listeners:
        //2a.) backBttn --> back to Login page
        backLoginBtn.setOnClickListener(new View.OnClickListener() {
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
                //1.) Obtain all info needed:
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String retypedPass = etRetypedPass.getText().toString();

                String errorString = isValid(email, password, retypedPass);          //Check if email, username, and password are valid
                //2.) If no error string --> user's info is valid!
                if(errorString.equals("")){
                    String fName = etFName.getText().toString();
                    String lName = etLName.getText().toString();
                    createAccount(fName, lName, email, password);
                }
                else{
                    showLongTopToast(errorString);
                }
            }
        });
    }

    private void createAccount(String fName, String lName, String email, String password) {
        Log.d(TAG, " in createAccount()");
        //1.) Obtain the username:
        String username = email.substring(0, email.length()-8);

        //2.) Create a User and set with all sign up info:
        User user = new User();
        user.setFirstName(fName);
        user.setLastName(lName);
        user.setEmail(email);
        user.setPassword(password);
        user.setUsername(username);
        user.setProfileSetUp(false);

        //3.) Attempt sign up in another thread, and set up callback handler:
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                //No error --> success!
                if(e == null){
                    Log.i(TAG, "createAccount():  User successful sign up!" );

                    //Notify user of success (set to display at top of screen):
                    Toast toast = Toast.makeText(getContext(), "You're now signed up!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();

                    //Go to ProfileSetupFragment automatically:
                    listener.toProfileSetupFragment();
                }

                //On failure:
                else{
                    Log.e(TAG, "createAccount():  User sign up failure:  error=", e);

                    Toast toast = Toast.makeText(getContext(), "Sorry, we couldn't sign you up!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
            }
        });
    }

    /**
     * Purpose:         Determines if the given email, username, and password are valid. If not, the method returns the corresponding error String to display. If valid, the String will be empty.
     * @param email
     * @param password
     * @return
     */
    private String isValid(String email, String password, String retypedPass) {
        Log.d(TAG, "in isValid()");
        String result = "";

        //1.) Check if the password meets all requirements:
        if(!password.equals(retypedPass)){
            //Log.d(TAG, "in isValid():   password and re-typed password don't match");
            result += "Your password and re-typed password don't match!";
        }
        if(password.length() < 7){

            result += "Your password must be longer than 7 characters!\n";
        }

        boolean hasCapital = false;
        boolean hasLowerCase = false;
        boolean hasNumber = false;
        //loop through string, change flags if requirement is met:
        for(int index = 0; index < password.length(); index++){
            char ch = password.charAt(index);
            if(Character.isUpperCase(ch)){
                hasCapital = true;
            }
            if(Character.isLowerCase(ch)){
                hasLowerCase = true;
            }
            if(Character.isDigit(ch)){
                hasNumber = true;
            }
        }
        if(!hasCapital){
            result += "Your password must have at least one capital letter! \n";
        }
        if(!hasLowerCase){
            result += "Your password must have at least one lowercase letter! \n";
        }
        if(!hasNumber){
            result += "Your password must have at least one number! \n";
        }


        //2.) Check if email ends with "@cpp.edu":
        if(email.length() > 8) {
            String lastChars = email.substring(email.length() - 8, email.length());
            if (!(lastChars.equals("@cpp.edu"))) {
                result += "Your email does not end with \"@cpp.edu\"";
            }
            //otherwise --> email is valid, we can check if the username is unique! i.e. this email has not already been registered
            else{
                String username = email.substring(0, email.length()-8);
                queryUsernames();                   // == obtain and fill currUsers List
                if(!validUsername(username)) {
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
     * Purpose;             Obtains a list of all current users and updates the "currUsers" List field
     */
    private void queryUsernames() {
        Log.d(TAG, "in queryUsernames() ");
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        //To get all users:
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                //If no error --> success!
                if (e == null) {
                    Log.d(TAG, "queryUsernames(): success!");

                    currUsers.addAll(users);
                    Log.i(TAG, "users: " + currUsers.toString());
                }
                //on failure:
                else {
                    Log.e(TAG, "queryUsernames(): failure:  error=", e);
                }
            }
        });
    }

    /**
     * Purpose:             Returns whether the given username is in the List of current users. (returns whether the given username is unique/valid)
     * @param username, the username to test uniqueness for
     * @return whether or not the username is unique/valid
     */
    private boolean validUsername(String username) {
        for(ParseUser user: currUsers) {
            if(user.getUsername().equalsIgnoreCase(username)){
                return false;
            }
        }
        return true;
    }

    private void showShortTopToast(String message){
        //Notify user of success (set to display at top of screen):
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }
    private void showLongTopToast(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

}