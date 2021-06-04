package com.example.wings.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wings.R;
import com.example.wings.models.inParseServer.TrustedContact;


import java.util.List;

public class TrustedContactsAdapter extends RecyclerView.Adapter<TrustedContactsAdapter.ViewHolder> {

    private Context context;
    private List<TrustedContact> trustedContacts;

    public TrustedContactsAdapter (Context context, List<TrustedContact> trustedContacts) {
        this.context = context;
        this.trustedContacts = trustedContacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemtc, parent, false);
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

    class ViewHolder extends RecyclerView.ViewHolder{

        private EditText tcName;
        private EditText tcRelationship;
        private EditText tcEmail;
        private EditText tcNum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tcName = itemView.findViewById(R.id.etName);
            tcRelationship = itemView.findViewById(R.id.etRelationship);
            tcEmail = itemView.findViewById(R.id.etEmail);
            tcNum = itemView.findViewById(R.id.etPhone);
        }

        public void bind(TrustedContact tc) {
            tcName.setText(tc.getFirstName() + " " + tc.getLastName());
            tcRelationship.setText(tc.getRelationship());
            tcEmail.setText(tc.getEmail());
            tcNum.setText(tc.getPhoneNumber());
        }
    }
}
