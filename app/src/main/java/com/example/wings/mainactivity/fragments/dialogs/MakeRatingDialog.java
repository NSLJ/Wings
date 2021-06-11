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
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.wings.R;
import com.example.wings.databinding.FragmentMakeRatingDialogBinding;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.helpers.UpdateHandler;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class MakeRatingDialog extends DialogFragment {
    private static final String TAG = "MakeRatingDialong";
    private static final String KEY_FOR_USER = "ratingWhichUser?";

    MAFragmentsListener listener;

    FragmentMakeRatingDialogBinding binding;
    RatingBar ratingBar;
    ParseUser forUser;              //which user is this rating for?

    public MakeRatingDialog() {}

    public static MakeRatingDialog newInstance(ParseUser forUser) {
        Log.d("Jo", "newInstance(): forUser = " + forUser.getObjectId());
        MakeRatingDialog fragment = new MakeRatingDialog();
        Bundle bundle = new Bundle();
        ParcelableObject data = new ParcelableObject();
        data.setOtherParseUser(forUser);
        bundle.putParcelable(KEY_FOR_USER, Parcels.wrap(data));
        fragment.setArguments(bundle);
        return fragment;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
            Log.d(TAG, "listener is attached");
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ParcelableObject dataReceived = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_FOR_USER));
            forUser = dataReceived.getOtherParseUser();
            Log.d("Jo", "onCreate(): forUser = " + forUser.getObjectId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMakeRatingDialogBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Views:
        Button btnSubmit = binding.btnConfirmSafety;
        Button btnNo = binding.btnNo;
        TextView tvGoReviewFrag = binding.tvToReviewFrag;
        ratingBar = binding.ratingBar;

        //2.) Set on click listeners:
        //2a.) btnNo --> do not save anything
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingBar.getRating();
                getDialog().dismiss();
            }
        });

        //2b.) btnConfirm  -->  save the rating:
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "forUserId = " + forUser.getObjectId());

                UpdateHandler.updateRatings(forUser.getObjectId(), ratingBar.getRating());              //should update/create an Update object dedicated to this userId
                getDialog().dismiss();
            }
        });

        //2c.) tvToReviewFrag --> go to Review Frag:
        tvGoReviewFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toReviewFragment(forUser);
            }
        });
    }
}