package com.example.wings.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wings.R;
import com.example.wings.mainactivity.fragments.EditTrustedContactsFragment;
import com.example.wings.models.inParseServer.TrustedContact;


import java.util.List;

import static java.security.AccessController.getContext;

public class TrustedContactsAdapter extends RecyclerView.Adapter<TrustedContactsAdapter.ViewHolder> {
    private static final String TAG = "TrustedContactsAdapter";

    private Context context;
    private List<TrustedContact> trustedContacts;
    private EditTrustedContactsFragment fragment;           //used by btnExit --> to remove from the model onClick() --> calls fragment.deleteEmptyTrustedContact()

    public TrustedContactsAdapter (Context context, List<TrustedContact> trustedContacts, EditTrustedContactsFragment fragment) {
        this.trustedContacts = trustedContacts;
        this.fragment = fragment;
        this.context = context;
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
        holder.bind(trustedContact, position);
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

    public boolean isAllValid(){
        Log.d(TAG, "isAllValid(): trustedContacts = " + trustedContacts.toString());

        for(int i = 0; i < trustedContacts.size(); i++) {
            if (!trustedContacts.get(i).getIsValid()) {
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
        private ImageButton btnClose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            etName = itemView.findViewById(R.id.etName);
            etRelationship = itemView.findViewById(R.id.etRelationship);
            etEmail = itemView.findViewById(R.id.etEmail);
            etPhone = itemView.findViewById(R.id.etPhone);
            btnClose = itemView.findViewById(R.id.btnExit);
        }

        public void bind(TrustedContact tc, int position) {
            if(tc.getIsValid()) {
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
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.deleteEmptyTrustedContact(position);
                }
            });
        }

        public void saveInfo(int position, String name, String email, String relationship, String phone){
                TrustedContact trustedContact = trustedContacts.get(position);
                trustedContact.setEmail(email);
                trustedContact.setName(name);
                trustedContact.setRelationship(relationship);
                trustedContact.setPhoneNumber(phone);
                trustedContact.setIsValid(true);
        }

        public boolean isValid(int position){
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String relationship = etRelationship.getText().toString();
            String phone = etPhone.getText().toString();

            //1.) Ensure all fields are filled out:
            if(!name.equals("") && !email.equals("") && !relationship.equals("") && !phone.equals("")) {
                //Check phone has only 10 digits:
                int digitCount = 0;
                for(int i = 0; i < phone.length(); i++){
                    if(Character.isDigit(phone.charAt(i))){
                        digitCount++;
                    }
                }
                if(digitCount != 10){
                    //Toast.makeText(context, "")
                    return false;
                }

                //Ensure email has an '@' and '.'
                if(!(email.contains("@") && email.contains("."))){
                    return false;
                }

                //if execution even gets to this point --> must be valid, no checks needed
                saveInfo(position, name, email, relationship, phone);
                return true;
            }
            else{
                //Toast.makeText(context, "You did not fill out all the fields.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }
}
