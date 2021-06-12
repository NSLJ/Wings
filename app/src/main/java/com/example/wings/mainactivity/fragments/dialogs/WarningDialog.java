package com.example.wings.mainactivity.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.wings.R;
import com.example.wings.databinding.FragmentWarningDialogBinding;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.home.BuddyHomeFragment;

import org.jetbrains.annotations.NotNull;

public class WarningDialog extends DialogFragment implements RequestTimeDialog.RequestDialogListener {

    MAFragmentsListener listener;

    private FragmentWarningDialogBinding binding;
    private Button btnNo;
    private Button btnRequestTime;
    private ImageButton btnExit;
    public static WarningDialog thisFragInstance;

    public WarningDialog() {}

    public static WarningDialog newInstance() {
        WarningDialog fragment = new WarningDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement SafetyToolKitListener");
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
        binding = FragmentWarningDialogBinding.inflate(getLayoutInflater(), container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        thisFragInstance = this;

        btnNo = binding.btnNo;
        btnRequestTime = binding.btnRequestTime;
        btnExit = binding.btnExit;

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        btnRequestTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dialog to RequestTimeDialog
                RequestTimeDialog dialog = RequestTimeDialog.newInstance();
                dialog.setTargetFragment(thisFragInstance, 1);
                dialog.show(getFragmentManager(), "RequestTimeDialogTag");
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void setRequestTime(long time) {
        listener.setRequestTime(time);
    }
    @Override
    public void onClose(){
        listener.showSnackBar();
    }
}