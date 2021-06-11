package com.example.wings.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wings.databinding.ItemReviewBinding;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.Review;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private static final String TAG = "ReviewAdapter";

    Context context;
    List<Review> allReviews;
    ItemReviewBinding binding;

    public ReviewAdapter(Context context, List<Review> allReviews){
        this.context = context;
        this.allReviews = allReviews;
    }
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        binding = ItemReviewBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ReviewAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(allReviews.get(position));
    }

    @Override
    public int getItemCount() {
        return allReviews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        ImageView ivProfile;
        TextView tvMemberSince;
        RatingBar ratingBar;
        TextView tvReviewDate;
        TextView tvBody;

        private Review reviewToPost;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvName = binding.tvName;
            ivProfile = binding.ivPic;
            tvMemberSince = binding.tvMemberSince;
            ratingBar = binding.ratingBar;
            tvReviewDate = binding.tvReviewDate;
            tvBody = binding.tvBody;
        }

        public void bind(Review review){
            // 1.) Obtain the fromUser from this review:
            String fromUserId = review.getFromUserId();
            Log.d(TAG, "reviewToPost = " + review.getObjectId());
            reviewToPost = review;
            queryForFromUser(fromUserId);
        }

        // Assumes reviewToPost is already initialized by bind()
        private void bindHelper(ParseUser fromUser){
            Log.d(TAG, "bindHelper(): fromUser == null:  " + (fromUser == null));
            if(fromUser != null) {
                User localParseUser = new User(fromUser);       //encapsulates in User{} for easy calling to Parse
                tvName.setText(localParseUser.getFirstName() + " " + localParseUser.getLastName());
                tvMemberSince.setText("Member since:   " + fromUser.getCreatedAt().toString());
                ratingBar.setRating(reviewToPost.getRating());
                tvReviewDate.setText(reviewToPost.getCreatedAt().toString());
                tvBody.setText(reviewToPost.getBody());
                ParseFile image = localParseUser.getProfilePic();
                if (image != null) {
                    Glide.with(context)
                            .load(image.getUrl())
                            .fitCenter()
                            .into(ivProfile);
                }
            }
        }

        public void queryForFromUser(String id){
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", id);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if(e == null){
                        Log.d(TAG, "Made query okay");
                        if(objects != null || objects.size() > 0){
                            Log.d(TAG, "found ParseUsers = " + objects.toString());
                            if(objects.size() == 1){
                                bindHelper(objects.get(0));
                            }
                            else{
                                Log.d(TAG, "Query returned more than 1 ParseUser");
                            }
                        }
                    }
                    else{
                        Log.e(TAG, "Error getting fromUser: ", e);
                    }
                }
            });
        }
    }
}
