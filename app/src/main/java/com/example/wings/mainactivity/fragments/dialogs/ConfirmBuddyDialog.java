package com.example.wings.mainactivity.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.wings.R;
import com.example.wings.models.Buddy;
import com.example.wings.models.BuddyMeetUp;
import com.example.wings.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class ConfirmBuddyDialog extends DialogFragment {
    private static final String TAG = "ConfirmBuddylDialog";
    private static final String KEY_MEETUPID = "meetUpId";

    private ParseUser currentUser = ParseUser.getCurrentUser();

    private ConfirmBuddyDialog.ResultListener listener;
    private String meetUpId;
    private BuddyMeetUp meetUpInstance;


    //Views:
    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvText;
    private TextView tvUserBuddyId;
    private TextView tvOtherBuddyId;
    private EditText etPin;
    private Button btnConfirmBuddy;

    public interface ResultListener{
        public void onConfirm();
        public void onReject();
    }

    public ConfirmBuddyDialog() {
        // Required empty public constructor
    }

    public static ConfirmBuddyDialog newInstance(String meetUpId) {
        ConfirmBuddyDialog fragment = new ConfirmBuddyDialog();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
        args.putString(KEY_MEETUPID, meetUpId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (ConfirmBuddyDialog.ResultListener) getTargetFragment();
        }
        catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
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
        dialog.setContentView(R.layout.fragment_confirm_buddy_dialog);

// set color transpartent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        View view = inflater.inflate(R.layout.fragment_confirm_buddy_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivProfile = view.findViewById(R.id.ivOtherPic);
        tvName = view.findViewById(R.id.tvFirstName);
        tvText = view.findViewById(R.id.tvText);
        tvUserBuddyId = view.findViewById(R.id.tvUserBuddyId);
        tvOtherBuddyId = view.findViewById(R.id.tvOtherBuddyId);
        etPin = view.findViewById(R.id.etPin);
        btnConfirmBuddy= view.findViewById(R.id.btnConfirmBuddy);

        //Get the BuddyMeetUp:
      //  queryBuddyMeetUp();
        //Get the otherBuddy's info:

    }

    private void queryBuddyMeetUp() {
        //Log.d(TAG, "in queryPotentialBuddy(): potentialBuddyId=" + potentialBuddyId);
        ParseQuery<BuddyMeetUp> query = ParseQuery.getQuery(BuddyMeetUp.class);
        query.whereEqualTo(BuddyMeetUp.KEY_OBJECT_ID, meetUpId);
        query.findInBackground(new FindCallback<BuddyMeetUp>() {
            @Override
            public void done(List<BuddyMeetUp> objects, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "in queryPotentialBuddy(): success!: response=" + objects.toString());
                    setBuddyMeetUp(objects.get(0));
                    setUp();
                }
            }
        });
    }
    private void setUp() {
        if (meetUpInstance != null) {
            //Get buddyInstance:
            Buddy currBuddyInstance = (Buddy) currentUser.getParseObject(User.KEY_BUDDY);
            try {
                currBuddyInstance.fetchIfNeeded();
                String currBuddyId = currBuddyInstance.getObjectId();

                //See if current user is classified as the "senderBuddy" or "receiverBuddy":
                String someBuddyId = meetUpInstance.getSenderBuddyId();
                if(!someBuddyId.equals(currBuddyId)){
                    //Then someBuddy is the other Buddy:

                }
                else{
                    //must get the receiverBuddy
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }
    }

    private void setBuddyMeetUp(BuddyMeetUp meetUp) {
      //  Log.d(TAG, "setPotentialBuddy()");
        meetUpInstance = meetUp;
    }


}