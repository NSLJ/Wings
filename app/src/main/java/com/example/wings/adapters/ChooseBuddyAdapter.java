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
import com.example.wings.R;
import com.example.wings.models.User;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ChooseBuddyAdapter extends RecyclerView.Adapter<ChooseBuddyAdapter.ViewHolder> {
    private static final String TAG = "ChooseBuddyAdapter";

    private Context context;
    private List<ParseUser> buddiesToShow;
    private List<Double> distancesList;

    public ChooseBuddyAdapter(Context context, List<ParseUser> buddiesToShow, List<Double> distancesList){
        this.context = context;
        this.buddiesToShow = buddiesToShow;
        this.distancesList = distancesList;
    }

    @NonNull
    @Override
    public ChooseBuddyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ChooseBuddyAdapter", "onCreateViewHolder");
        View buddyView = LayoutInflater.from(context).inflate(R.layout.item_potential_buddy, parent, false);
        return new ViewHolder(buddyView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseBuddyAdapter.ViewHolder holder, int position) {
        Log.d("ChooseBuddyAdapter", "onBindViewHolder: position = " + position);
        ParseUser currentUser = buddiesToShow.get(position);
        double currentDistance = distancesList.get(position);
        holder.bind(currentUser, currentDistance);
    }

    @Override
    public int getItemCount() {
        return buddiesToShow.size();
    }

    public void clear() {
        buddiesToShow.clear();
        distancesList.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<ParseUser> newUsers, List<Double> newDistances){
        buddiesToShow.addAll(newUsers);
        distancesList.addAll(newDistances);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //All Views in item_potential_buddy layout
        ImageView ivProfilePic;
        TextView tvName;
        TextView tvDistance;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivOtherProfile);
            tvName = (TextView) itemView.findViewById(R.id.tvOtherName);
            tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rbOtherRatings);
        }

        //Do all binding to a specific buddy here:
        public void bind(ParseUser user, double distanceBetween){
            Log.d(TAG, "in bind()");
            try {
                user.fetchIfNeeded();

                if(user.getParseFile(User.KEY_PROFILEPICTURE) != null) {
                    Glide.with(context).load(user.getParseFile(User.KEY_PROFILEPICTURE)).into(ivProfilePic);
                }
                String fName = user.getString(User.KEY_FIRSTNAME);

                float rating = (float) user.getDouble(User.KEY_RATING);
                Log.d(TAG, "bind(): fName= " + fName + "    rating=" + rating);
                tvName.setText(fName);
                ratingBar.setRating(rating);

                tvDistance.setText(Double.toString(distanceBetween));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

}