package com.example.wings.startactivity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wings.R;
import com.example.wings.adapters.TrustedContactsAdapter;
import com.example.wings.models.inParseServer.TrustedContact;
import com.example.wings.startactivity.SAFragmentsListener;

import java.util.ArrayList;
import java.util.List;

public class EditTrustedContactsFragment extends Fragment {

    private SAFragmentsListener listener;

    public static final String TAG = "EditTrustedContactsFragment";
    private RecyclerView tcRecyclerList;
    //make this a parseUser adapter (double check with Trusted Contacts??)
    protected TrustedContactsAdapter adapter;
    protected List<TrustedContact> trustedContacts;
    protected SwipeRefreshLayout swipeContainer;


    public EditTrustedContactsFragment() {
        // Required empty public constructor
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SAFragmentsListener) {
            listener = (SAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_trusted_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tcRecyclerList = view.findViewById(R.id.tcRecyclerList);

        // replace these for trusted contacts list
        trustedContacts = new ArrayList<>();
        adapter = new TrustedContactsAdapter(getContext(), trustedContacts);

        tcRecyclerList.setAdapter(adapter);
        tcRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}
