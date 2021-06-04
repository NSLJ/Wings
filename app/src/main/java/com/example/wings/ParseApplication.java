package com.example.wings;

import android.app.Application;

import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyRequest;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.example.wings.models.inParseServer.TrustedContact;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.parse.Parse;
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
                .applicationId("1RaG1OyUOkupAelJwPAOxtp80KZ2BokQuVvRq1SP")
                .clientKey("HZqvpoD3UrgiAMucL83uSMode9HHr1bZp9rFh2OC")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
