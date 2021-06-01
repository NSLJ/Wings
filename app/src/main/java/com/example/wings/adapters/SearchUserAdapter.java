package com.example.wings.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.models.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private static final String TAG = "SearchUserAdapter";
    private Context context;
    private List<ParseUser> users;
    private SearchUserOnClickListener listener;

    public interface SearchUserOnClickListener{
        void onClick(String nameToDisplay);
    }
    public SearchUserAdapter(Context context, List<ParseUser> users, SearchUserOnClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_queried_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
    }
    @Override
    public int getItemCount() {
        return users.size();
    }

    public void clear(){
        users.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<ParseUser> list){
        users.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUsername;
        private ImageView ivProfile;
        private RatingBar ratingBar;
        private TextView tvEmail;
        private RelativeLayout rlContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvOtherName);
            ivProfile = itemView.findViewById(R.id.ivOtherProfile);
            ratingBar = itemView.findViewById(R.id.rbOtherRatings);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            rlContainer = itemView.findViewById(R.id.rlContainer);
        }

        public void bind(ParseUser user) {
            rlContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(user.getString(User.KEY_FIRSTNAME));
                }
            });

            tvUsername.setText(user.getString(User.KEY_FIRSTNAME) + " " + user.getString(User.KEY_LASTNAME));
            float rating = (float) user.getDouble(User.KEY_RATING);
            ratingBar.setRating(rating);
            tvEmail.setText(User.KEY_EMAIL);

            ParseFile image = user.getParseFile(User.KEY_PROFILEPICTURE);
            if(image != null) {
                try {
                    Glide.with(context).load(image.getFile()).into(ivProfile);
                } catch (ParseException e) {
                    Log.e(TAG, "error with getting the profile image file from ParseFile, error =" +e.getLocalizedMessage());
                }
            }
            else{
                Log.d(TAG, "image given was null");
            }
        }
    }
}
