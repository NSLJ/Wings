package com.example.wings;

import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.TextView;

public class ConfirmDestinationDialog extends DialogFragment {
    private static final String TAG = "ConfirmDestinationDialog";
    private static final String KEY_DESTINATION = "destination";
    private ImageButton ibttnAccept;
    private ImageButton ibttnReject;
    private TextView tvDestination;

    private ResultListener listener;

    public ConfirmDestinationDialog() {
        // Required empty public constructor
    }

    public interface ResultListener{
        public void onAccept();
        public void onReject();
    }

    public static ConfirmDestinationDialog newInstance(String destination) {
        ConfirmDestinationDialog fragment = new ConfirmDestinationDialog();
        Bundle args = new Bundle();
        args.putString(KEY_DESTINATION, destination);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (ResultListener) getTargetFragment();
        }
        catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = getDialog().getWindow();

        // set "origin" to top left corner, so to speak
        window.setGravity(Gravity.TOP| Gravity.LEFT);

        // after that, setting values for x and y works "naturally"
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 110;
        params.y = 1190;
        window.setAttributes(params);

        return inflater.inflate(R.layout.fragment_confirm_destination_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String destination = getArguments().getString(KEY_DESTINATION, "defaultVal");
        tvDestination = (TextView) view.findViewById(R.id.tvDestination);
        tvDestination.setText("Destination: " + destination);

        // Get field from view
        ibttnAccept = (ImageButton) view.findViewById(R.id.ibttnAccept);
        ibttnReject = (ImageButton) view.findViewById(R.id.ibttnReject);

        ibttnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ibttnReject - onClick:");
                listener.onReject();
                getDialog().dismiss();
            }
        });

        ibttnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ibttnAccept - onClick():");
                listener.onAccept();
                getDialog().dismiss();
            }
        });
        // Fetch arguments from bundle and set title
        //String title = getArguments().getString("title", "Enter Name");
        //getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
       // ibttnAccept.requestFocus();
        //getDialog().getWindow().setSoftInputMode(
          //      WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);*/
    }
}