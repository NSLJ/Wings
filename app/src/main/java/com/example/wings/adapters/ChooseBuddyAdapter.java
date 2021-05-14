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

import com.example.wings.R;
import com.example.wings.models.Buddy;

import java.util.List;

public class ChooseBuddyAdapter extends RecyclerView.Adapter<ChooseBuddyAdapter.ViewHolder> {

    private Context context;
    private List<Buddy> buddiesToShow;

    public ChooseBuddyAdapter(Context context, List<Buddy> buddiesToShow){
        this.context = context;
        this.buddiesToShow = buddiesToShow;
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
        Buddy currentBuddy = buddiesToShow.get(position);
        holder.bind(currentBuddy);
    }

    @Override
    public int getItemCount() {
        return buddiesToShow.size();
    }

    public void clear() {
        buddiesToShow.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Buddy> list){
        buddiesToShow.addAll(list);
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
            ivProfilePic = itemView.findViewById(R.id.ivOtherProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ratingBar = itemView.findViewById(R.id.rbOtherRatings);
        }

        //Do all binding to a specific buddy here:
        public void bind(Buddy buddy){

        }
    }

}