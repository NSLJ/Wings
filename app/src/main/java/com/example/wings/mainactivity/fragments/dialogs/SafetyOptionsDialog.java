package com.example.wings.mainactivity.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.wings.databinding.FragmentSafetyOptionsDialogBinding;
import com.example.wings.models.User;
import com.parse.ParseUser;

/**
 * Purpose:         Diaplys options for Safety Toolkit button. Asks the user to choose one of the options and enter their PIN. MainActivity will then listen to this response and invoke the correct action.
 */
public class SafetyOptionsDialog extends DialogFragment {
    private static final String TAG = "SafetyOptionsDialog";
    public static final String KEY_MODE = "whichMode?";             //to receive which container need to display

    FragmentSafetyOptionsDialogBinding binding;

    //ShowOptionsContainer + Views:
    RelativeLayout showOptionsContainer;
    CheckBox cbEmergency;
    CheckBox cbNotify;
    Button btnOptionsConfirm;
    EditText etOptionsPin;
    ImageButton btnOptionsExit;
    ImageView ivNotifyInfo;
    ImageView ivEmergencyInfo;

    //confirmSafetyContainer + Views:
    RelativeLayout confirmSafetyContainer;
    ImageButton btnConfirmSafetyExit;
    Button btnConfirmSafety;
    EditText etConfirmSafetyPin;

    SafetyToolkitListener listener;
    boolean waitingForOkay;                 //toggled to true by onCreateView() when need confirmSafetyContainer

    public interface SafetyToolkitListener{
        void onNotifyContacts();
        void onEmergency();
        void onOkayNow();
    }

    public SafetyOptionsDialog() {}

    public static SafetyOptionsDialog newInstance(boolean waitingForOkay) {
        SafetyOptionsDialog fragment = new SafetyOptionsDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_MODE, waitingForOkay);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SafetyToolkitListener) {
            listener = (SafetyToolkitListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement SafetyToolKitListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            waitingForOkay = getArguments().getBoolean(KEY_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSafetyOptionsDialogBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();          //returns the layout (as View)
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "waitingForOkay=" + waitingForOkay);
        //1.) Initialize both types of containers:  --> enables both to toggle visibility of each other when necessary
        showOptionsContainer = binding.safetyOptionsContainer;
        confirmSafetyContainer = binding.confirmSafetyContainer;

        //Toast.makeText(getContext(), "Select one of the options below. Click on each to show more info.", Toast.LENGTH_LONG).show();
        if(!waitingForOkay){
            setUpOptionsContainer();
        }
        else{
            setUpConfirmSafetyContainer();
        }

    }

    //Purpose:      Connects showOptionsContainer, ties onClick listeners, shown to User:
    public void setUpOptionsContainer(){
        Log.d(TAG, "setUpOptionsContainer()");
        //1.) Set visibility + Connect Views:
        confirmSafetyContainer.setVisibility(View.INVISIBLE);

        cbEmergency = binding.cbEmergency;
        cbNotify = binding.cbNotify;
        btnOptionsConfirm = binding.btnOptionsConfirm;
        etOptionsPin = binding.etOptionsPin;
        btnOptionsExit = binding.btnOptionsExit;
        ivNotifyInfo = binding.ivNotifyInfo;
        ivEmergencyInfo = binding.ivEmergencyInfo;


        //2.) Set up listeners:
        //2a.) Information buttons:
        ivNotifyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Notifies your Trusted Contacts of all trip information and current location until the end of the trip.", Toast.LENGTH_LONG).show();       //TODO: implement way to select which specfic Trsuted Contacts get notified
            }
        });
        ivEmergencyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Immediately dials 911, and notifies your Trusted Contacts of your immediate emergency.", Toast.LENGTH_LONG).show();
            }
        });

        //2b.) Exit button --> close this DialogFrag:
        btnOptionsExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        //2c.) Confirm button --> check PIN entered + invoke corresponding method:
        btnOptionsConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currUser = ParseUser.getCurrentUser();
                String correctPin = currUser.getString(User.KEY_PIN);
                String attemptedPin = etOptionsPin.getText().toString();

                if(attemptedPin.equals(correctPin)){
                    //Find which option selected:
                    if(cbNotify.isChecked() && !cbEmergency.isChecked()){
                        listener.onNotifyContacts();
                        getDialog().dismiss();
                    }
                    else if(cbEmergency.isChecked()){           //doesn't matter if cbNotify is also checked as cbEmergency does all of the above
                        listener.onEmergency();
                        getDialog().dismiss();
                    }
                    else{
                        Toast.makeText(getContext(), "You did not select one of the options!", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getContext(), "Your PIN was not correct.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    //Purpose:          Connects confirmSafetyContainer views + ties listener, toggle visibility
    public void setUpConfirmSafetyContainer(){
        Log.d(TAG, "setUpConfirmSafetyContainer");

        //1.) Set visibility + Connect Views:
        showOptionsContainer.setVisibility(View.INVISIBLE);

        btnConfirmSafetyExit = binding.btnConfirmSafetyExit;
        btnConfirmSafety = binding.btnConfirmSafety;
        etConfirmSafetyPin = binding.etConfirmSafetyPin;

        //2.) Connect onclick listeners:
        //2a.) Exit button --> close this DialogFrag:
        btnConfirmSafetyExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        //2c.) Confirm button --> check PIN entered + invoke corresponding method:
        btnConfirmSafety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currUser = ParseUser.getCurrentUser();
                String correctPin = currUser.getString(User.KEY_PIN);
                String attemptedPin = etConfirmSafetyPin.getText().toString();

                if(attemptedPin.equals(correctPin)){
                    listener.onOkayNow();
                    getDialog().dismiss();
                }
                else{
                    Toast.makeText(getContext(), "Your PIN was not correct.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}