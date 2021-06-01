package com.example.wings.mainactivity.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.User;
import com.parse.ParseUser;

/**
 * Purpose:         Diaplys options for Safety Toolkit button. Asks the user to choose one of the options and enter their PIN. MainActivity will then listen to this response and invoke the correct action.
 */
public class SafetyOptionsDialog extends DialogFragment {
    private static final String TAG = "SafetyOptionsDialog";

    CheckBox cbEmergency;
    CheckBox cbNotify;
    Button btnConfirm;
    EditText etPin;
    TextView tvNotify;
    TextView tvEmergency;
    ImageButton btnExit;

    SafetyToolkitListener listener;

    public interface SafetyToolkitListener{
        void onNotifyContacts();
        void onEmergency();
    }

    public SafetyOptionsDialog() {}

    public static SafetyOptionsDialog newInstance() {
        SafetyOptionsDialog fragment = new SafetyOptionsDialog();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_safety_options_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), "Select one of the options below. Click on each to show more info.", Toast.LENGTH_LONG).show();

        cbEmergency = view.findViewById(R.id.cbEmergency);
        cbNotify = view.findViewById(R.id.cbNotify);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        etPin = view.findViewById(R.id.etPin);
        //tvNotify = view.findViewById(R.id.tvNotify);
       // tvEmergency = view.findViewById(R.id.tvEmergency);
        btnExit = view.findViewById(R.id.btnExit);

        cbNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Notifies your Trusted Contacts of all trip information and current location until the end of the trip.", Toast.LENGTH_LONG).show();       //TODO: implement way to select which specfic Trsuted Contacts get notified
            }
        });
        cbEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Immediately dials 911, and notifies your Trusted Contacts of your immediate emergency.", Toast.LENGTH_LONG).show();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currUser = ParseUser.getCurrentUser();
                String correctPin = currUser.getString(User.KEY_PIN);
                String attemptedPin = etPin.getText().toString();

                if(attemptedPin.equals(correctPin)){
                    //Find which option selected:
                    if(cbNotify.isChecked() && !cbEmergency.isChecked()){
                        listener.onNotifyContacts();
                    }
                    else if(cbEmergency.isChecked()){           //doesn't matter if cbNotify is also checked as cbEmergency does all of the above
                        listener.onEmergency();
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

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }
}