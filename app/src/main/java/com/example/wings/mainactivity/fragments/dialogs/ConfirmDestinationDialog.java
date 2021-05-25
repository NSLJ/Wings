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
import android.widget.TextView;

import com.example.wings.R;

public class ConfirmDestinationDialog extends DialogFragment {
    private static final String TAG = "ConfirmDestinationDialog";
    private static final String KEY_DESTINATION = "destination";
    private ImageButton ibttnAccept;
    private ImageButton ibttnReject;
    private TextView tvDestination;
    private Button btnConfirmDestination;

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
       /* Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 140);

        // set "origin" to top left corner, so to speak
        window.setGravity(Gravity.TOP| Gravity.LEFT);

        // after that, setting values for x and y works "naturally"
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 110;
        params.y = 1190;
        window.setAttributes(params);
*/
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getDialog().getWindow()
                .setLayout(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );*/
        Dialog dialog = getDialog();
        dialog.getWindow().setGravity(Gravity.TOP| Gravity.LEFT);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.x = 110;
        params.y = 1190;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
// layout to display
        dialog.setContentView(R.layout.fragment_confirm_destination_dialog);

// set color transpartent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        View view = inflater.inflate(R.layout.fragment_confirm_destination_dialog, container, false);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String destination = getArguments().getString(KEY_DESTINATION, "defaultVal");
        tvDestination = (TextView) view.findViewById(R.id.tvDestination);
        tvDestination.setText("Destination: " + destination);
       /* btnConfirmDestination = view.findViewById(R.id.btnConfirmDestination);

        btnConfirmDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ibttnAccept - onClick():");
                listener.onAccept();
                getDialog().dismiss();
            }
        });*/

        // Get field from view
        ibttnAccept = (ImageButton) view.findViewById(R.id.ivAccept);
        ibttnReject = (ImageButton) view.findViewById(R.id.ivReject);

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