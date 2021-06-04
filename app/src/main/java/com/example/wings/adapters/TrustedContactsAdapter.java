package com.example.wings.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wings.R;
import com.example.wings.models.inParseServer.TrustedContact;


import java.util.List;

import static java.security.AccessController.getContext;

public class TrustedContactsAdapter extends RecyclerView.Adapter<TrustedContactsAdapter.ViewHolder> {
    private static final String TAG = "TrustedContactsAdapter";

    private List<TrustedContact> trustedContacts;

    public TrustedContactsAdapter (List<TrustedContact> trustedContacts) {
        this.trustedContacts = trustedContacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trusted_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrustedContactsAdapter.ViewHolder holder, int position) {
        TrustedContact trustedContact = trustedContacts.get(position);
        holder.bind(trustedContact);
    }

    @Override
    public int getItemCount() {
        return trustedContacts.size();
    }

    public void clear(){
        trustedContacts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<TrustedContact> list){
        trustedContacts.addAll(list);
        notifyDataSetChanged();
    }

    public List<TrustedContact> getTrustedContacts(){
        return trustedContacts;
    }

    public boolean isAllCompleted(){
        Log.d(TAG, "isAllCompleted(): trustedContacts = " + trustedContacts.toString());

        for(int i = 0; i < trustedContacts.size(); i++) {
            if (!trustedContacts.get(i).getIsComplete()) {
                return false;
            }
        }
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private EditText etName;
        private EditText etRelationship;
        private EditText etEmail;
        private EditText etPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            etName = itemView.findViewById(R.id.etName);
            etRelationship = itemView.findViewById(R.id.etRelationship);
            etEmail = itemView.findViewById(R.id.etEmail);
            etPhone = itemView.findViewById(R.id.etPhone);
        }

        public void bind(TrustedContact tc) {
            if(tc.getIsComplete()) {
                etName.setText(tc.getName());
                etRelationship.setText(tc.getRelationship());
                etEmail.setText(tc.getEmail());
                etPhone.setText(tc.getPhoneNumber());
            }
            //else --> not a completed trusted contact nothing needs to be binded
            else{
                Log.d(TAG, "Trusted contact is not complete!");
                etName.setText("");
                etRelationship.setText("");
                etEmail.setText("");
                etPhone.setText("");
            }
        }

        public void isCompleted(int position){
            Log.d(TAG, "ViewHolder: isCompleted()");

            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String relationship = etRelationship.getText().toString();
            String phone = etPhone.getText().toString();

            if(!name.equals("") && !email.equals("") && !relationship.equals("") && !phone.equals("")){
                Log.d(TAG, "ViewHolder: isCompleted(): completely filled out!");
                TrustedContact trustedContact = trustedContacts.get(position);
                trustedContact.setEmail(email);
                trustedContact.setName(name);
                trustedContact.setRelationship(relationship);
                trustedContact.setPhoneNumber(phone);
                trustedContact.setIsComplete(true);
                //this should make the trustedContact.complete() return true
            }
            else{
                Log.d(TAG, "ViewHolder: isCompleted(): wasn't completely filled out");
            }
        }
    }
}
