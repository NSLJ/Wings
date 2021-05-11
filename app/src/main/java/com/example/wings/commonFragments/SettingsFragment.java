package com.example.wings.commonFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wings.R;
import com.example.wings.SettingAdapter;
import com.example.wings.models.Setting;

import org.json.JSONArray;

import java.util.ArrayList;

import static com.parse.Parse.getApplicationContext;

public class SettingsFragment extends Fragment {

    private ArrayList<Setting> settingList;
    private RecyclerView rvSettings;
    private SettingAdapter settingAdapter;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingList = new ArrayList<>();
        rvSettings = view.findViewById(R.id.rvsettings);

        setSettingList();

        settingAdapter = new SettingAdapter(settingList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvSettings.setLayoutManager(layoutManager);
        rvSettings.setItemAnimator(new DefaultItemAnimator());
        rvSettings.setAdapter(settingAdapter);


    }

    private void setSettingList() {
        settingList.add(new Setting("Edit Username"));
        settingList.add(new Setting("Edit Password"));
        settingList.add(new Setting("Edit Status"));
        settingList.add(new Setting("Edit Email"));
        settingList.add(new Setting("Edit Emergency Contacts"));


    }
}