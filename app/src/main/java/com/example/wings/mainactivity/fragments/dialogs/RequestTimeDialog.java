package com.example.wings.mainactivity.fragments.dialogs;

import android.content.Context;
import android.media.Image;
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
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.databinding.FragmentRequestTimeDialogBinding;
import com.example.wings.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.Parse;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;


public class RequestTimeDialog extends DialogFragment {

    ParseUser currUser = ParseUser.getCurrentUser();

    private FragmentRequestTimeDialogBinding binding;
    private CheckBox cbOption1;
    private CheckBox cbOption2;
    private EditText etPin;
    private Button btnSubmit;
    private ImageButton btnExit;

    RequestDialogListener listener;

    public interface RequestDialogListener{
        void setRequestTime(long time);
        void onClose();
    }

    public RequestTimeDialog() {}

    public static RequestTimeDialog newInstance() {
        RequestTimeDialog fragment = new RequestTimeDialog();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RequestDialogListener) {
            listener = (RequestDialogListener) context;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRequestTimeDialogBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cbOption1 = binding.cbOption1;
        cbOption2 = binding.cbOption2;
        etPin = binding.etPin;
        btnSubmit = binding.btnSubmit;
        btnExit = binding.btnExit;

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbOption1.isChecked() && cbOption2.isChecked()){
                    Toast.makeText(getContext(), "You must choose one option only.", Toast.LENGTH_SHORT).show();
                }
                else if(cbOption1.isChecked()){
                    //Hardcoded, represents 15 minutes:
                    handleClick(15/**60*/); //TODO: ensure to uncomment these! They are commented now just for easy testing!
                }
                else if(cbOption2.isChecked()){
                    //Hardcoded, represents 20 minutes:
                    handleClick(20/**60*/);
                }
                else{
                    Toast.makeText(getContext(), "No option is selected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleClick(long timeChosen){
        User localParseUser = new User(currUser);
        String attemptedPin = etPin.getText().toString();
        if(attemptedPin.equals(localParseUser.getPin())){
            listener.setRequestTime(timeChosen);        //for now just do it without partner's consent
            getDialog().dismiss();
            //Save to Parse
            //Show toast that we are waiting for other Buddy

            //Another method should be called when have both confirmations
        }
        else{
            Toast.makeText(getContext(), "Pin was incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}