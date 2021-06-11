package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wings.R;
import com.example.wings.adapters.TrustedContactsAdapter;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.inParseServer.TrustedContact;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

//Ideas for persisting adapter info --> save all information into TrustedContact objects even if not all completed, when add button or save button is clicked --> adapter will automatically load what info that is saved in the List<TrustedContacts>
public class EditTrustedContactsFragment extends Fragment {
    public static final String TAG = "EditTrustedContactsFragment";
    public static final String KEY_TRUSTED_CONTACTS = "receiveTrustedContacts";         //used so MainActivity may send a List<TrustedContact> to work on/save

    private MAFragmentsListener listener;

    private RecyclerView rvTrustedContacts;
    private ImageButton btnNewTC;
    private Button btnSave;

    private TrustedContactsAdapter adapter;
    private List<TrustedContact> trustedContacts = new ArrayList<>();


    public EditTrustedContactsFragment() {}

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }
    @Override
    //Purpose:      called once Fragment created, will obtain the User object it was passed in
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            ParcelableObject receivedData = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_TRUSTED_CONTACTS));
            trustedContacts.addAll(receivedData.getTrustedContactList());
            Log.d(TAG, "trustedContacts passed in=" + trustedContacts.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_edit_trusted_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        //2.) Connect views:
        rvTrustedContacts = view.findViewById(R.id.rvTrustedContacts);
        btnNewTC = view.findViewById(R.id.btnNewTC);
        btnSave = view.findViewById(R.id.btnSave);

        //3.) Set up RecyclerView:
        adapter = new TrustedContactsAdapter(getContext(), trustedContacts, this);
        rvTrustedContacts.setAdapter(adapter);
        rvTrustedContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        if(trustedContacts.size() == 0) {
            //Start with a blank TrustedContact to show 1 item_trusted_contact by default:
            addEmptyTrustedContact();
        }

        //4.) Set up onClick listeners:
        //4a.) Save button: --> navigate back to the ProfileSetUpFrag IF there's at least one valid TrustedContact saved:
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trustedContacts.size() > 0){

                    //Instruct all ViewHolders to check their fields for data that was inputted --> updates the trustedContacts list in adapter:
                    for(int i = 0; i < trustedContacts.size(); i++) {
                        TrustedContactsAdapter.ViewHolder currentViewHolder = (TrustedContactsAdapter.ViewHolder) rvTrustedContacts.findViewHolderForLayoutPosition(i);          //assuming there
                        //currentViewHolder.isCompleted(i);
                        currentViewHolder.isValid(i);
                    }
                    if(adapter.isAllValid()){
                        //Then we can go back to ProfileSetupFrag passing in this list --> ProfileSetUpFrag in charge of saving to Parse
                        listener.toProfileSetupFragment(adapter.getTrustedContacts());
                    }
                    else{
                        Toast.makeText(getContext(), "You have not fulfilled all sections!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //4b.) plus button --> make another Trusted Contact:
        btnNewTC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Plus button was clicked: trustedContacts size = " + trustedContacts.size() + "      trustedContacts = " + trustedContacts.toString());
                if(trustedContacts.size() < 5){     //we only store 5 trusted contacts per user at max
                   addEmptyTrustedContact();
                }
            }
        });
    }

    //Purpose:          Adds an empty TrustedContact object to our model to --> inflate an empty item_trusted_contact layout
    private void addEmptyTrustedContact(){
        TrustedContact newContact = new TrustedContact();
        trustedContacts.add(newContact);
        adapter.notifyDataSetChanged();
    }

    //Purpose:          Deletes an emtpty TrustedContact when the btnClose is clicked in adapter.viewholder
    public void deleteEmptyTrustedContact(int position){
        trustedContacts.remove(position);
        adapter.notifyDataSetChanged();
    }

}
