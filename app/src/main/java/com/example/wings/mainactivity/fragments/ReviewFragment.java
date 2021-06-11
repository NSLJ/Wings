package com.example.wings.mainactivity.fragments;

import android.content.Context;
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
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.User;
import com.example.wings.models.helpers.UpdateHandler;
import com.example.wings.models.inParseServer.Review;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

/**
 * ReviewFragment.java
 * Purpose:             In charge of displaying a review Buddy form for the user to submit a new Review object for the given Buddy.
 */
public class ReviewFragment extends Fragment {
    private static final String TAG = "ReviewFragment";
    public static final String KEY_FOR_USER = "userReviewFor";

    MAFragmentsListener listener;
    FragmentReviewBinding binding;

    ParseUser userReviewFor;
    User localParseUser;
    String userName;

    //Views:
    TextView tvTitle;
    RatingBar ratingBar;
    EditText etBody;
    TextView tvPrompt;
    Button btnSubmit;

    public ReviewFragment() {}

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ParcelableObject dataReceived = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_FOR_USER));
            userReviewFor = dataReceived.getOtherParseUser();
            localParseUser = new User(userReviewFor);                   //allows us to use getters instead of making repeated requests to database
            userName = localParseUser.getFirstName();
            Log.d(TAG, "user received = " + userReviewFor.getObjectId() + "   name = " + userName);
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

        if (userName != null) {
            //2.) Set up prompts (prompts are a bit personal to each user!'
            tvTitle.setText("Review " + userName + "?");
            tvPrompt.setText("How was " + userName + " as a Buddy?");

            //3.) Set on click listeners:
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    float rating = ratingBar.getRating();
                    String reviewBody = etBody.getText().toString();

                    //As long as its filled out --> create a Review object + update using UpdateHandler
                    if(!reviewBody.equals("") && rating > 0){
                        //1.) Create a Review instance:
                        Review review = new Review(userReviewFor.getObjectId(), ParseUser.getCurrentUser().getObjectId(), reviewBody, rating);

                        saveReview(review);
                    }
                }
            });
        }
    }

    private void saveReview(Review review){
        review.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //If no error saving the Review in Parse --> make an Update object for the user the review is for
                if(e == null){
                    UpdateHandler.updateReviews(userReviewFor.getObjectId(), review);
                    listener.toCurrentHomeFragment();
                }
            }
        });
    }
}