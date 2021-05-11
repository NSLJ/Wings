package com.example.wings.models;

public class Setting {
    //item for setting rv
    private String setting;

    public Setting(String setting) {
        this.setting = setting;
    }

    public String getSetting(){
        return setting;
    }

    public void setSetting(String settingName){
        setting = settingName;
    }
}
