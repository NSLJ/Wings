package com.example.wings.mainactivity.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wings.R;
import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

//Purpose:          Just displays instructions for the user to place a pin in the map to denote the trip destination for this request being sent. Does not need to take or send data.

public class SendRequestStepsDialog extends DialogFragment {
    private static final String TAG = "SendRequestStepsDialog";

    private ParseUser currentUser = ParseUser.getCurrentUser();

    private String meetUpId;
    private BuddyMeetUp meetUpInstance;

    //Views:
    Button btnOk;


    public SendRequestStepsDialog() {}

    public static SendRequestStepsDialog newInstance() {
        SendRequestStepsDialog fragment = new SendRequestStepsDialog();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

      /*  Dialog dialog = getDialog();
        dialog.getWindow().setGravity(Gravity.TOP| Gravity.LEFT);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.x = 110;
        params.y = 1190;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
// layout to display
        dialog.setContentView(R.layout.fragment_confirm_buddy_dialog);

// set color transpartent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
*/
        View view = inflater.inflate(R.layout.fragment_send_request_steps_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }
}