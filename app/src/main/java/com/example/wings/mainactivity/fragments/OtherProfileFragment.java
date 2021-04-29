package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.User;
import com.parse.ParseFile;
import com.parse.ParseUser;


//All auto-filled stuff, just follow the samples I left behind!


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OtherProfileFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class OtherProfileFragment extends Fragment {

    public static final String TAG = "OtherProfileFragment";

    private MAFragmentsListener listener;

    private ImageView otherPic;
    private TextView otherName;
    private TextView otherEmail;
    private TextView otherFriends;
    private TextView distance;
    private RatingBar profileRating;
    private ImageButton editBtn;
    private ImageButton deleteBtn;


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_other_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        otherPic = view.findViewById(R.id.otherPic);
        otherName = view.findViewById(R.id.otherName);
        otherEmail = view.findViewById(R.id.otherEmail);
        otherFriends = view.findViewById(R.id.otherFriends);
        distance = view.findViewById(R.id.distance);
        profileRating = view.findViewById(R.id.profileRating);
        editBtn = view.findViewById(R.id.editBtn);
        deleteBtn = view.findViewById(R.id.deleteBtn);

        ParseUser current = ParseUser.getCurrentUser();
        otherName.setText(current.getString(User.KEY_FIRSTNAME));
        otherEmail.setText(current.getString(User.KEY_EMAIL));
        profileRating.setRating((float) current.getInt(User.KEY_RATING));

        ParseFile image = current.getParseFile(User.KEY_PROFILEPICTURE);
        if(image != null){
            Glide.with(getContext())
                    .load(image.getUrl())
                    .override(400, 400)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(otherPic);
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Edit Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Delete Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }
}