package com.example.wings.mainactivity.fragments;

import android.content.Context;
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
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.ParcelUser;
import com.example.wings.models.User;
import com.example.wings.startactivity.StartActivity;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

//All auto-filled stuff, just follow the samples I left behind!


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {
    public static final String TAG = "UserProfileFragment";

    private MAFragmentsListener listener;

    private ImageView profilePic;
    private TextView profileName;
    private TextView profilePin;
    private TextView profileEmail;
    private RatingBar profileRating;
    private Button logOutBtn;

    public UserProfileFragment() {}   // Required empty public constructor

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
        ParseUser current = ParseUser.getCurrentUser();
        profileName.setText(current.getString(User.KEY_FIRSTNAME));
        profilePin.setText(Integer.toString(current.getInt(User.KEY_PIN)));
        profileEmail.setText(current.getString(User.KEY_EMAIL));
        ParseFile image = current.getParseFile(User.KEY_PROFILEPICTURE);
        //profilePic.setImageResource(image);       we had to use Glide to upload pics in previous assignments!
        //                                      I think it's like this? :   Glide.with(getContext()).load(image).into(profilePic);

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.) Log off the user using Parse:
                current.logOut();

                //2.) Intent to go to StartActivity, finish() this activity
                Intent intent;
                Log.d(TAG, ": Trying to do intent now.");
                intent = new Intent(getContext(), StartActivity.class);
                startActivity(intent);
            }
        });


    }
}