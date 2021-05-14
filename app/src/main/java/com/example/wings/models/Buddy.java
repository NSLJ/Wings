package com.example.wings.models;

import android.nfc.cardemulation.HostApduService;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Buddy")
public class Buddy extends ParseObject {
    private static final String TAG = "Buddy";

    private Buddy buddyInstance;

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

    public Buddy(ParseUser user, WingsGeoPoint intendedDestination){
        setUser(user);
        setDestination(intendedDestination);

        //All default values:
        List<BuddyRequest> receivedRequests = new ArrayList<>();
        setReceivedRequests(receivedRequests);

        List<BuddyRequest> sentRequests = new ArrayList<>();
        setSentRequests(sentRequests);

        setHasBuddy(false);
    }

    //Getters and setters
    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }
    public void setUser(ParseUser sender){
        put(KEY_USER, sender);
    }


    public void setDestination(WingsGeoPoint destination){
        put(KEY_DESTINATION, destination);
    }
    public WingsGeoPoint getDestination(){
        return (WingsGeoPoint) getParseObject(KEY_DESTINATION);
    }


    public ParseGeoPoint getMeetingPoint(){
        return getParseGeoPoint(KEY_MEETINGPOINT);
    }
    public void setMeetingPoint(ParseGeoPoint meetingPoint){
        put(KEY_MEETINGPOINT, meetingPoint);
    }

    public void setReceivedRequests(List<BuddyRequest> allReceivedRequests){
        put(KEY_RECIEVEDREQUESTS, allReceivedRequests);
    }
    //This may give problems
    public List<BuddyRequest> getReceivedRequests(){
        return getList(KEY_RECIEVEDREQUESTS);
    }


    public void setSentRequests(List<BuddyRequest> allSentRequests){
        put(KEY_SENTREQUESTS, allSentRequests);
    }
    //This may give problems
    public List<BuddyRequest> getSentRequests(){
        return getList(KEY_SENTREQUESTS);
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
