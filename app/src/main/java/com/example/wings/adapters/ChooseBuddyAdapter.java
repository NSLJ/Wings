package com.example.wings.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.databinding.ItemChooseBuddyBinding;
import com.example.wings.models.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ChooseBuddyAdapter extends RecyclerView.Adapter<ChooseBuddyAdapter.ViewHolder> {
    private static final String TAG = "ChooseBuddyAdapter";

    private Context context;
    private List<ParseUser> buddiesToShow;
    private List<Double> distancesList;
    private OnClickListener clickListener;
    ItemChooseBuddyBinding binding;

    //To pass in the position of row clicked on:
    public interface OnClickListener{
        void onClick(int position);
        void goOtherProfile(ParseUser user);
    }

    //Pass in the models and an onClickListener:
    public ChooseBuddyAdapter(Context context, List<ParseUser> buddiesToShow, List<Double> distancesList, OnClickListener clickListener){
        this.context = context;
        this.buddiesToShow = buddiesToShow;
        this.distancesList = distancesList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ChooseBuddyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ChooseBuddyAdapter", "onCreateViewHolder");
        binding = ItemChooseBuddyBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding.getRoot());
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
        RelativeLayout rlContainer;
        ImageView ivProfilePic;
        TextView tvName;
        TextView tvDistance;
        RatingBar ratingBar;
        Button btnGoProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rlContainer = binding.rlContainer;
            ivProfilePic = binding.ivOtherProfile;
            tvName = binding.tvOtherName;
            tvDistance = binding.tvDistance;
            ratingBar = binding.rbOtherRatings;
            btnGoProfile = binding.btnToProfile;
        }

        //Do all binding to a specific buddy here:
        public void bind(ParseUser user, double distanceBetween){
            Log.d(TAG, "in bind()");

            //Listen to the recyclerView, when its clicked on --> invoke the onClick() given to us in constructor
            rlContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(getAdapterPosition());
                }
            });
            btnGoProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.goOtherProfile(user);
                }
            });
            try {
                user.fetchIfNeeded();

                ParseFile imageFile = user.getParseFile(User.KEY_PROFILEPICTURE);
                if( imageFile != null) {
                    Glide.with(context).load(imageFile.getFile()).into(ivProfilePic);
                }
                String fName = user.getString(User.KEY_FIRSTNAME);

                float rating = (float) user.getDouble(User.KEY_RATING);
                Log.d(TAG, "bind(): fName= " + fName + "    rating=" + rating);
                tvName.setText(fName);
                ratingBar.setRating(rating);

                double roundedDistance = Math.round(distanceBetween*100.0)/100.0;
                tvDistance.setText(Double.toString(roundedDistance) + " m away");
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

}