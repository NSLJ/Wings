package com.example.wings;

import android.app.Application;

import com.example.wings.models.User;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(User.class);

        // Initializes Parse SDK as soon as the application is created
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("sElWNZw3UaPAVWDt1unXKuTKKV3KVG63iNoYtHMJ")
                .clientKey("aQkw4udueaiRTNA3CGKeKNpxrVZ9T9xcFsu2ppCP")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
