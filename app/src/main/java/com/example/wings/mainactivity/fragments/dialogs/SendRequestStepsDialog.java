package com.example.wings.mainactivity.fragments.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wings.R;

//Purpose:          Just displays instructions for the user to place a pin in the map to denote the trip destination for this request being sent. Does not need to take or send data.

public class SendRequestStepsDialog extends DialogFragment {
    private static final String TAG = "SendRequestStepsDialog";

    //Views:
    Button btnOk;

    public SendRequestStepsDialog() {}

    public static SendRequestStepsDialog newInstance() {
        SendRequestStepsDialog fragment = new SendRequestStepsDialog();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_request_steps_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnOk = view.findViewById(R.id.btnConfirm);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }
}