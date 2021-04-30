package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.graphics.Color;
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
import com.example.wings.models.ParcelUser;
import com.example.wings.models.User;
import com.example.wings.startactivity.SAFragmentsListener;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.parceler.Parcels;

/**
 * RegisterTwoFragment.java
 * Purpose:         This displays the second screen of registering a new user/creating an account!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */
public class RegisterTwoFragment extends Fragment {
    public static final String KEY_GET_USER = "user";
    public static final String KEY_SEND_PROFILESETUPFRAG = "ProfileSetupFrag?";
    private static final String TAG = "RegisterTwoFragment";

    private SAFragmentsListener listener;
    private User userToRegister;

    private EditText etPassword;
    private EditText etRetypedPassword;
    private Button signUpBtn;
    private TextView tvToLogin;

    private TextView tvCapLetter;           //to be able to show which requirements are met
    private TextView tvLowerLetter;
    private TextView lengthReq;
    private TextView numReq;

    private String password;            //bc for sommeee reason it won't getPassword() even after its been set

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

    /**
     * Purpose:     obtains the User object that was half-initialized by RegisterOne, needed to register
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ParcelUser pUser = Parcels.unwrap(getArguments().getParcelable(KEY_GET_USER));
            userToRegister = pUser.getUser();
            Log.d(TAG, "onCreate(): userToRegister = " + userToRegister.getUsername());
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
        //1.) Tie references to Views:
        etPassword = view.findViewById(R.id.etPassword);
        etRetypedPassword = view.findViewById(R.id.etRetypedPassword);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        tvToLogin = view.findViewById(R.id.tvToLogin);

        tvCapLetter = view.findViewById(R.id.tvCapLetterReq);           //to be able to show which requirements are met
        tvLowerLetter= view.findViewById(R.id.tvLowLetterReq);
        lengthReq= view.findViewById(R.id.lengthReq);
        numReq= view.findViewById(R.id.numReq);

        //2.) Add click listeners:
        tvToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toLoginFragment();
            }
        });

        //User is attempting to sign up --> check if password meets requirements
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etPassword != null && etRetypedPassword != null) {
                    String password = etPassword.getText().toString();
                    String retypedPass = etRetypedPassword.getText().toString();

                    try {
                        if (passwordValid(password, retypedPass)) {           //if valid password --> set it and create account, then go directly to ProfileSetUpFragment
                            userToRegister.setPassword(password);
                            createAccount();
                        }
                        else{
                            showShortTopToast("Your password is invalid!");
                        }
                    } catch (InterruptedException | ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    showShortTopToast("You didn't input anything!");
                }

            }
        });
    }

    /**
     * Purpose:         Determines if the given password are valid. If not, the method returns the corresponding error String to display. If valid, the String will be empty.
     * @param password
     * @return
     */
    private boolean passwordValid(String password, String retypedPass) throws InterruptedException {
        Log.d(TAG, "in passwordValid()");
        boolean result = true;          //assume it to be true --> becomes false if any of the check become false

        //1.) Check if pass == retyped pass
        if (!password.equals(retypedPass)) {
            Log.d(TAG, "in passwordValid():   password and re-typed password don't match");
            showLongTopToast("Your password and re-typed password don't match!");
            result = false;
        }

        //check length req, TODO make 7, and colors a constant, don't hardcode it
        if (password.length() < 7) {
            result = false;
            lengthReq.setTextColor(Color.parseColor("#FF0000"));
        }
        else {  //make it green
            lengthReq.setTextColor(Color.parseColor("#00FF00"));
        }


        //check capital, lowercase, and number req with one forloop:
        boolean hasCapital = false;
        boolean hasLowerCase = false;
        boolean hasNumber = false;
        //loop through string, change flags if requirement is met:
        for (int index = 0; index < password.length(); index++) {
            char ch = password.charAt(index);
            if (Character.isUpperCase(ch)) {
                hasCapital = true;
            }
            if (Character.isLowerCase(ch)) {
                hasLowerCase = true;
            }
            if (Character.isDigit(ch)) {
                hasNumber = true;
            }
        }

        //if still false --> req not met
        if (!hasCapital) {
            result = false;
            tvCapLetter.setTextColor(Color.parseColor("#FF0000"));     //make red
        }
        else {  //make it green
            tvCapLetter.setTextColor(Color.parseColor("#00FF00"));
        }

        if (!hasLowerCase) {
            result = false;
            tvLowerLetter.setTextColor(Color.parseColor("#FF0000"));
        }
        else {  //make it green
            tvLowerLetter.setTextColor(Color.parseColor("#00FF00"));
        }

        if (!hasNumber) {
            result = false;
            numReq.setTextColor(Color.parseColor("#FF0000"));
        }
        else {  //make it green
            numReq.setTextColor(Color.parseColor("#00FF00"));
        }

        return result;
    }

    /**
     * Purpose:         attempts to sign up the User field, "userToRegister"
     */
    private void createAccount() throws ParseException {
        Log.d(TAG, " in createAccount()");
        password = userToRegister.getPassword();
        Log.d(TAG, "createAccount(): password=" + password);

        //3.) Attempt sign up in another thread, and set up callback handler:
        userToRegister.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                //If success:
                if(e == null){
                    Log.i(TAG, "createAccount():  User successful sign up! password="+password );
                    Log.d(TAG, "createAccount(): user=" + userToRegister.toString());
                    showShortTopToast("You're now signed up!");

                    //Log this new user in immediately and go to MainActivity
                    try {
                        login();
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                }

                //On failure:
                else{
                    Log.e(TAG, "createAccount():  User sign up failure:  error=", e);
                    showShortTopToast("Sorry, we couldn't sign you up!");
                }
            }
        });



    }

    /**
     * Purpose:         to login the new user automatically.
     */
    private void login() throws ParseException {
        Log.d(TAG,"in login(): ");
        String username = userToRegister.getUsername();
      //  String password = userToRegister.getPassword();
        Log.d(TAG,"in login(): userToRegister.password="+password);

        userToRegister.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                //if there is a user --> tell StartActivity of successful login
                if(user != null){
                    Log.d(TAG, "login(): user logged in!");
                    showShortTopToast("You're logged in!");
                    listener.onLogin(KEY_SEND_PROFILESETUPFRAG);
                }
                else {
                    showShortTopToast("We couldn't sign in your new account! Try it again.");
                    listener.toLoginFragment();
                    Log.d(TAG, "logInInBackground(): failed" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showLongTopToast(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    private void showShortTopToast(String message){
        //Notify user of success (set to display at top of screen):
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

}