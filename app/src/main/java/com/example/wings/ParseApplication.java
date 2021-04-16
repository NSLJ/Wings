package com.example.wings;

import android.app.Application;

import com.example.wings.models.Buddy;
import com.example.wings.models.BuddyRequest;
import com.example.wings.models.BuddyTrip;
import com.example.wings.models.TrustedContact;
import com.example.wings.models.User;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Buddy.class);
        ParseObject.registerSubclass(BuddyRequest.class);
        ParseObject.registerSubclass(BuddyTrip.class);
        ParseObject.registerSubclass(TrustedContact.class);

        // Initializes Parse SDK as soon as the application is created
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("72bJpnuYmdfcxDrigJ0PnmJyghvSk5dzRXTnWhc4")
                .clientKey("lZY7K2bp0kmXkVJDbVHop4ZxDnOaZ6u58YLHmlok")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
