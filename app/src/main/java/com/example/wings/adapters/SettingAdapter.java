package com.example.wings.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wings.R;
import com.example.wings.models.helpers.Setting;

import java.util.ArrayList;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private ArrayList<Setting> settingList;

    public SettingAdapter(ArrayList<Setting> settingList) {
        this.settingList = settingList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvsetting;
        private LinearLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvsetting = itemView.findViewById(R.id.tvSettingTitle);
            container = itemView.findViewById(R.id.settingContainer);


        }
    }


        @NonNull
        @Override
        public SettingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemsetting, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SettingAdapter.ViewHolder holder, int position) {

            String settingname = settingList.get(position).getSetting();

            holder.tvsetting.setText(settingname);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent();
                    Log.i("SettingAdapter", settingname);

                }
            });


        }

        @Override
        public int getItemCount() {
            return settingList.size();
        }

}
