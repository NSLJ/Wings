package com.example.wings.mainactivity.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wings.R;
import com.example.wings.models.Buddy;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

//Purpose:      To display a Dialog Box to confirm a buddy request with a specific user!
public class ConfirmSendRequestDialog extends DialogFragment {
    private static final String TAG = "ConfirmSendRequestDialog";
    public static final String KEY_USERID = "buddyId";

    private ConfirmSendRequestDialog.ResultListener listener;
    private String potentialBuddyId;
    private ParseUser potentialBuddy;
    private double distance;

    //Views:
    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvDistance;
    private ImageButton bttnAccept;
    private ImageButton bttnReject;
    private Button bttnSendRequest;
    private RatingBar ratingBar;

    //Purpose:      so the PotentialBuddyFragment will be able to handle info from the Dialog!
    public interface ResultListener{
        public void onAccept(Buddy buddy);
        public void onReject();
    }

    public ConfirmSendRequestDialog() {}

    public static ConfirmSendRequestDialog newInstance(String otherUserId) {
        ConfirmSendRequestDialog fragment = new ConfirmSendRequestDialog();
        Bundle args = new Bundle();
        args.putString(KEY_USERID, otherUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (ConfirmSendRequestDialog.ResultListener) getTargetFragment();
        }
        catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Initalize potentialBuddyId:
            potentialBuddyId = getArguments().getString(KEY_USERID, "defaultVal");
            Log.d(TAG, "onCreate():  potentialBuddyId =" + potentialBuddyId);
        }
       // this.setCanceledOnTouchOutside(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Dialog dialog = getDialog();
        dialog.getWindow().setGravity(Gravity.TOP| Gravity.LEFT);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.x = 110;
        params.y = 1190;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
// layout to display
        dialog.setContentView(R.layout.fragment_confirm_send_request_dialog);

// set color transpartent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        return inflater.inflate(R.layout.fragment_confirm_send_request_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Views:
        ivProfile = view.findViewById(R.id.ivOtherPic);
        tvName = view.findViewById(R.id.tvFirstName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvDistance = view.findViewById(R.id.tvDistance);
        //bttnAccept = view.findViewById(R.id.ibttnAccept);
       // bttnReject = view.findViewById(R.id.ibttnReject);
        ratingBar = view.findViewById(R.id.rbRating);
        bttnSendRequest= view.findViewById(R.id.btnSendRequest);

        //Initalize ParseUser and Buddy:
        if(potentialBuddyId != null) {
            queryPotentialBuddy();
        }
        else{
            Log.d(TAG, "potentialBuddyId = null");
        }
    }

    private void queryPotentialBuddy(){
        Log.d(TAG, "in queryPotentialBuddy(): potentialBuddyId="+ potentialBuddyId);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ParseUser.KEY_OBJECT_ID, potentialBuddyId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    Log.d(TAG, "in queryPotentialBuddy(): success!: response=" + objects.toString());
                    setPotentialBuddy(objects.get(0));
                    setUp();
                }
            }
        });
    }

    private void setUp(){
        if(potentialBuddy != null) {
            Log.d(TAG, "in setUp(): potentialBuddy is NOT null");
            WingsGeoPoint otherCurrLocationGeoPoint = (WingsGeoPoint) potentialBuddy.getParseObject(User.KEY_CURRENTLOCATION);
            try {
                //Initialized potentialBuddyLocation:
                otherCurrLocationGeoPoint.fetchIfNeeded();
                ParseGeoPoint potentialBuddyLocation = new ParseGeoPoint(otherCurrLocationGeoPoint.getLatitude(), otherCurrLocationGeoPoint.getLongitude());

                //Get current user's location as ParseGeoPoint
                ParseUser currentUser = ParseUser.getCurrentUser();
                WingsGeoPoint currLocationGeoPoint = (WingsGeoPoint) currentUser.getParseObject(User.KEY_CURRENTLOCATION);
                currLocationGeoPoint.fetchIfNeeded();
                ParseGeoPoint currentLocation = currLocationGeoPoint.getLocation();

                distance = currentLocation.distanceInKilometersTo(potentialBuddyLocation)*1000;      //* 1000 to convert to m


                //Fill in Views:
                double roundedDistance = Math.round(distance*100.0)/100.0;
                tvDistance.setText(Double.toString(roundedDistance) + " m away from your destination");

                ParseFile imageFile = potentialBuddy.getParseFile(User.KEY_PROFILEPICTURE);
                if(imageFile != null) {
                    Glide.with(getContext()).load(imageFile.getFile()).into(ivProfile);
                }
                tvName.setText(potentialBuddy.getString(User.KEY_FIRSTNAME));
                tvEmail.setText(potentialBuddy.getString(User.KEY_EMAIL));
                ratingBar.setRating((float) potentialBuddy.getDouble(User.KEY_RATING));

                //Set onClickListeners:
               /* bttnReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bttnReject - onClick:");
                        listener.onReject();
                        getDialog().dismiss();
                    }
                });

                bttnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bttnAccept - onClick():");
                        Buddy buddyInstance = (Buddy) potentialBuddy.getParseObject(User.KEY_BUDDY);
                        try {
                            buddyInstance.fetchIfNeeded();
                            listener.onAccept(buddyInstance);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        getDialog().dismiss();
                    }
                });*/

                bttnSendRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bttnSendRequest - onClick():");
                        Buddy buddyInstance = (Buddy) potentialBuddy.getParseObject(User.KEY_BUDDY);
                        try {
                            buddyInstance.fetchIfNeeded();
                            listener.onAccept(buddyInstance);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        getDialog().dismiss();
                    }
                });

            } catch (ParseException e) {
                Log.d(TAG, "onViewCreated(): error fetching buddy instance");
                e.printStackTrace();
            }
        }
    }

    private void setPotentialBuddy(ParseUser otherUser){
        Log.d(TAG, "setPotentialBuddy()");
        potentialBuddy = otherUser;
    }

}