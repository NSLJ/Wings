package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wings.R;
import com.example.wings.TimeFormatter;
import com.example.wings.adapters.ReviewAdapter;
import com.example.wings.databinding.FragmentOtherProfileBinding;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.Review;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;


public class OtherProfileFragment extends Fragment {
    public static final String TAG = "OtherProfileFragment";
    public static final String KEY_OTHER_USER = "which user to show?";

    private FragmentOtherProfileBinding binding;
    private MAFragmentsListener listener;
    private ParseUser otherUser;
    private User localParseUser;

    private List<Review> reviews;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private TextView tvNumReviews;

    public OtherProfileFragment() {}         // Required empty public constructor

    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement MAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
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
        if(getArguments() != null){
            ParcelableObject data = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_OTHER_USER));
            otherUser = data.getOtherParseUser();
            localParseUser = new User(otherUser);
        }
        else{
            Log.e(TAG, "There was no user given for us to populate!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOtherProfileBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(otherUser != null && localParseUser != null) {
            //1.) Connect views:
            ImageView otherPic = binding.otherPic;
            TextView otherName = binding.otherName;
            TextView otherUsername = binding.tvUsername;
            RatingBar profileRating = binding.profileRating;
            TextView tvNumRatings = binding.tvNumRatings;
            TextView tvNumTrips = binding.tvNumTrips;
            tvNumReviews = binding.tvNumReviews;
            TextView tvMemberTime = binding.tvMemberTime;
            rvReviews = binding.rvReviews;

            //2.) Populate views, EXCEPT for tvNumReviews --> waits until query is finished and updates there!:
            otherName.setText(localParseUser.getFirstName() + " " + localParseUser.getLastName());
            otherUsername.setText("@"+localParseUser.getParseUsername());
            profileRating.setRating((float) localParseUser.getRating());
            tvNumRatings.setText("(" + localParseUser.getNumRatings() + " ratings)");
            tvMemberTime.setText("Member since:     " + TimeFormatter.getProperDate(otherUser.getCreatedAt().toString()));
            tvNumTrips.setText("Total Completed Trips:   " + localParseUser.getNumTrips() + " trips");
            ParseFile image = localParseUser.getProfilePic();
            if (image != null) {
                Glide.with(getContext())
                        .load(image.getUrl())
                        .fitCenter()
                        .into(otherPic);
            }

            //3.) Set up the recycler view:
            reviews = new ArrayList<>();
            reviewAdapter = new ReviewAdapter(getContext(), reviews);
            rvReviews.setAdapter(reviewAdapter);
            rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));

            //4.) Populate info into the modeL;
            queryForReviews();
        }
        else{
            Toast.makeText(getContext(), "There was an error! Did not receive a user to populate :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryForReviews(){
        String forUserId = otherUser.getObjectId();
        ParseQuery<Review> query = ParseQuery.getQuery(Review.class);
        query.whereEqualTo(Review.KEY_FOR_USER, forUserId);
        query.findInBackground(new FindCallback<Review>() {
            @Override
            public void done(List<Review> objects, ParseException e) {
                if(e == null){
                    Log.d(TAG, "query sucessful: object = " + objects.toString());
                    if(objects == null || objects.size() == 0){
                        Log.d(TAG, "Query did not return anything");
                    }
                    else{
                        Log.d(TAG, "query found reviews for this person!");
                        reviews.addAll(objects);
                        reviewAdapter.notifyDataSetChanged();
                        tvNumReviews.setText("(" + reviews.size() + " reviews)");
                    }
                }
            }
        });
    }
}