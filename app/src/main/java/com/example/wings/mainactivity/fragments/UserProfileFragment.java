package com.example.wings.mainactivity.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.wings.R;
import com.example.wings.models.User;
import com.example.wings.startactivity.StartActivity;
import com.parse.ParseFile;
import com.parse.ParseObject;

//All auto-filled stuff, just follow the samples I left behind!


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = "UserProfileFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageView profilePic;
    private TextView profileName;
    private TextView profilePin;
    private TextView profileEmail;
    private RatingBar profileRating;
    private Button logOutBtn;

    public UserProfileFragment() {}   // Required empty public constructor


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profilePic = view.findViewById(R.id.profilePic);
        profileName = view.findViewById(R.id.profileName);
        profilePin = view.findViewById(R.id.profilePin);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileRating = view.findViewById(R.id.profileRating);
        logOutBtn = view.findViewById(R.id.logOutBtn);


//        //temp for testing
//        User user = new User();
//        user.setEmail("lms@cpp.edu");
//        user.setFirstName("Laura");
//        user.setLastName("Siu");
//        user.setProfileSetUp(false);
//        user.setRating(5);
//        user.setPin(1111);
//
    /*
        profileName.setText(post.getName());
        profilePin.setText(post.getUser().getPin());
        profileEmail.setText(post.getUser().getEmail());
        ParseFile image = post.getImage();
    */

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.) Log off the user using Parse:
                //    ParseUser.logOut();

                //2.) Intent to go to StartActivity, finish() this activity
                Intent intent;
                Log.d(TAG, ": Trying to do intent now.");
                intent = new Intent(getContext(), StartActivity.class);
                startActivity(intent);
            }
        });


    }
}