package com.example.wings.mainactivity.fragments;

import android.media.Rating;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.wings.R;
import com.example.wings.databinding.FragmentReviewBinding;
import com.example.wings.models.ParcelableObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

/**
 * ReviewFragment.java
 * Purpose:             In charge of displaying a review Buddy form for the user to submit a new Review object for the given Buddy.
 */
public class ReviewFragment extends Fragment {
    private static final String TAG = "ReviewFragment";
    public static final String KEY_FOR_USER = "userReviewFor";

    FragmentReviewBinding binding;

    ParseUser userReviewFor;

    //Views:
    TextView tvTitle;
    RatingBar ratingBar;
    EditText etBody;
    TextView tvPrompt;
    Button btnSubmit;

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ParcelableObject dataReceived = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_FOR_USER));
            userReviewFor = dataReceived.getOtherParseUser();
            Log.d(TAG, "user received = " + userReviewFor.getObjectId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReviewBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //1.) connect views:
        tvTitle = binding.tvTitle;
        ratingBar = binding.reviewRatingBar;
        etBody = binding.etReviewText;
        tvPrompt = binding.tvPromptReview;
        btnSubmit = binding.btnSubmit;
    }
}