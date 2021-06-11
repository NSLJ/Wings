package com.example.wings.mainactivity.fragments.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;

/**
 * Purpose:         Invoked by the SearchUserFragment. Used when user finds another user they may like to add as a friend! This Dialog --> displays profile information about this potential friend and asks whether or not the user would like to add them
 *                  Later when there is more information to show --> can change into a regular Fragment to display a whole profile page --> displays other's reviews of them.
 */
public class SeeFriendDialog extends DialogFragment {
    private static final String TAG = "SeeFriendDialog";
    private static final String KEY_OTHERUSER = "otherUser's name";

    String otherName;

    ImageButton btnExit;
    TextView tvHeading;
    Button btnAdd;

    public SeeFriendDialog() {}

    public static SeeFriendDialog newInstance(String name) {
        SeeFriendDialog fragment = new SeeFriendDialog();
        Bundle args = new Bundle();
        args.putString(KEY_OTHERUSER, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.d(TAG, "getting Arguments");
            otherName = getArguments().getString(KEY_OTHERUSER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_see_friend_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnExit = view.findViewById(R.id.btnOptionsExit);
        btnAdd = view.findViewById(R.id.btnConfirmSafety);
        tvHeading = view.findViewById(R.id.tvOptionsHeading);

        tvHeading.setText("Add " + otherName+"?");
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Added " + otherName + " as a friend!", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
                //TODO: implement user's friends list parameter through Parse
            }
        });
    }
}