package com.example.wings.models;

import android.nfc.cardemulation.HostApduService;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Buddy")
public class Buddy extends ParseObject {

    //Keys correspond exactly to how the attributes are named in Parse database
    public static final String KEY_OBJECTID = "objectId";
    public static final String KEY_SENDER = "Seeker";
    public static final String KEY_MEETINGPOINT = "MeetingPoint";
    public static final String KEY_RECIEVEDREQUESTS = "receivedBuddyRequests";
    public static final String KEY_SENTREQUESTS = "sentBuddyRequests";
    public static final String KEY_HASBUDDY = "hasBuddy";
    public static final String KEY_ONMEETINGBUDDY = "onMeetingBuddy";
    public static final String KEY_ONBUDDYTRIP = "onBuddyTrip";
    public static final String KEY_BUDDYTRIP = "buddyTrip";

    public Buddy(){}

    //Getters and setters
    public ParseUser getSender(){
        return getParseUser(KEY_SENDER);
    }
    public void setSender(ParseUser sender){
        put(KEY_SENDER, sender);
    }

    public ParseGeoPoint getMeetingPoint(){
        return getParseGeoPoint(KEY_MEETINGPOINT);
    }
    public void setMeetingPoint(ParseGeoPoint meetingPoint){
        put(KEY_MEETINGPOINT, meetingPoint);
    }

    public boolean getHasBuddy(){
        return getBoolean(KEY_HASBUDDY);
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
}
