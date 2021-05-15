package com.example.wings;

import android.app.Application;

import com.example.wings.models.Buddy;
import com.example.wings.models.BuddyMeetUp;
import com.example.wings.models.BuddyRequest;
import com.example.wings.models.BuddyTrip;
import com.example.wings.models.TrustedContact;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * ParseApplication.java
 * Purpose:         To initialize the Parse SDK as soon as the application is launched.
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseUser.registerSubclass(User.class);
        //ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Buddy.class);
        ParseObject.registerSubclass(BuddyRequest.class);
        ParseObject.registerSubclass(BuddyTrip.class);
        ParseObject.registerSubclass(TrustedContact.class);
        ParseObject.registerSubclass(WingsGeoPoint.class);
        ParseObject.registerSubclass(BuddyMeetUp.class);

        // Initializes Parse SDK as soon as the application is created
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("72bJpnuYmdfcxDrigJ0PnmJyghvSk5dzRXTnWhc4")
                .clientKey("lZY7K2bp0kmXkVJDbVHop4ZxDnOaZ6u58YLHmlok")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
