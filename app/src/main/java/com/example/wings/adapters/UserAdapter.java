package com.example.wings.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.models.User;
import com.parse.ParseFile;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemuser, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

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

    public void addAll(List<User> list){
        users.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvusernmae;
        private ImageView ivprofilepic;
        private RatingBar rbuserrating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvusernmae = itemView.findViewById(R.id.tvTitle);
            ivprofilepic = itemView.findViewById(R.id.ivProfilePic);
            rbuserrating = itemView.findViewById(R.id.ratingBar);




        }

        public void bind(User user) {

            tvusernmae.setText(user.getFirstName() + " " + user.getLastName());
            rbuserrating.setRating((float)user.getRating());

            ParseFile image = user.getProfilePic();
//            if(image != null) {
//                Glide.with(context).load(user.getProfilePic().getUrl()).into(ivprofilepic);
//            }
        }
    }
}
