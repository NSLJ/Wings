package com.example.wings.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wings.R;
import com.example.wings.models.Setting;

import java.util.ArrayList;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private ArrayList<Setting> settingList;

    public SettingAdapter(ArrayList<Setting> settingList){
        this.settingList = settingList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvsetting;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvsetting = itemView.findViewById(R.id.tvSettingTitle);

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
    }

    @Override
    public int getItemCount() {
        return settingList.size();
    }
}
