package com.example.wings.mainactivity.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.example.wings.R;
import com.example.wings.models.BuddyMeetUp;
import com.example.wings.models.BuddyTrip;


public class ConfirmSafeArrivalDialog extends DialogFragment {
    private static final String TAG = "ConfirmSafeArrivalDialog";
    private static final String KEY_BUDDYTRIPID = "buddyTripId";

    private ConfirmSafeArrivalDialog.ResultListener listener;
    private String buddyTripId;
    private BuddyTrip buddyTrip;

    public interface ResultListener{
        public void onSafe();
        public void onReject();
    }

    public ConfirmSafeArrivalDialog() {
        // Required empty public constructor
    }

    public static ConfirmSafeArrivalDialog newInstance(String buddyTripId) {
        ConfirmSafeArrivalDialog fragment = new ConfirmSafeArrivalDialog();
        Bundle args = new Bundle();
        args.putString(KEY_BUDDYTRIPID, buddyTripId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (ConfirmSafeArrivalDialog.ResultListener) getTargetFragment();
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
        dialog.setContentView(R.layout.fragment_confirm_safe_arrival_dialog);

// set color transpartent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        View view =  inflater.inflate(R.layout.fragment_confirm_safe_arrival_dialog, container, false);
        return view;


    }
}