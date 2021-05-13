package com.example.wings.models;

import android.nfc.cardemulation.HostApduService;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("Buddy")
public class Buddy extends ParseObject {
    private static final String TAG = "Buddy";

    //Keys correspond exactly to how the attributes are named in Parse database
    public static final String KEY_OBJECTID = "objectId";
    public static final String KEY_USER = "user";
    public static final String KEY_DESTINATION = "intendedDestination";

    public static final String KEY_MEETINGPOINT = "MeetingPoint";
    public static final String KEY_RECIEVEDREQUESTS = "receivedBuddyRequests";
    public static final String KEY_SENTREQUESTS = "sentBuddyRequests";
    public static final String KEY_HASBUDDY = "hasBuddy";
    public static final String KEY_ONMEETINGBUDDY = "onMeetingBuddy";
    public static final String KEY_ONBUDDYTRIP = "onBuddyTrip";
    public static final String KEY_BUDDYTRIP = "buddyTrip";

    public Buddy(){}

    //Getters and setters
    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }
    public void setUser(ParseUser sender){
        put(KEY_USER, sender);
    }

    public ParseGeoPoint getMeetingPoint(){
        return getParseGeoPoint(KEY_MEETINGPOINT);
    }
    public void setMeetingPoint(ParseGeoPoint meetingPoint){
        put(KEY_MEETINGPOINT, meetingPoint);
    }

    public boolean getHasBuddy(){
        ParseUser user = ParseUser.getCurrentUser();
        Boolean hasBuddy = user.getBoolean(KEY_HASBUDDY);

        if(hasBuddy == null){
            hasBuddy = false;
            user.put(KEY_HASBUDDY, false);
            saveUserData("hasBuddy");
        }
        return hasBuddy;
    }
    public void setHasBuddy(boolean has){
        put(KEY_HASBUDDY, has);
    }

    public boolean getOnMeetingBuddy(){
        return getBoolean(KEY_ONMEETINGBUDDY);
    }
    public void setOnMeetingBuddy(boolean meeting){
        put(KEY_ONMEETINGBUDDY, meeting);
    }

    public boolean getOnBuddyTrip(){
        return getBoolean(KEY_ONBUDDYTRIP);
    }
    public void setOnBuddyTrip(boolean onTrip){
        put(KEY_ONBUDDYTRIP, onTrip);
    }

    public ParseObject getBuddyTrip(){
        return getParseObject(KEY_BUDDYTRIP);
    }

    public String getObjectID() {
        return getString(KEY_OBJECTID);
    }

    public void setDestination(WingsGeoPoint destination){
        put(KEY_DESTINATION, destination);
    }

    //Helper method:
    private void saveUserData(String text){
        ParseUser user = ParseUser.getCurrentUser();
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Log.d(TAG, "no Error saving " + text);
                }
                else{
                    Log.d(TAG, "Error saving " + text + " error=" + e.getMessage());
                }
            }
        });
    }
}
