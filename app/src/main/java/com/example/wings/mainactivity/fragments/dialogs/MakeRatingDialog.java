package com.example.wings.mainactivity.fragments.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import com.example.wings.R;

public class MakeRatingDialog extends DialogFragment {

    RatingBar ratingBar;

    public MakeRatingDialog() {}

    public static MakeRatingDialog newInstance() {
        MakeRatingDialog fragment = new MakeRatingDialog();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_make_rating_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Views:
        Button btnOk = view.findViewById(R.id.btnAdd);
        ratingBar = view.findViewById(R.id.ratingBar);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingBar.getRating();
                //TODO: somehow save this to otherUser --> authentication issues due to making direct changes to ParseUser
                getDialog().dismiss();
            }
        });


    }
}