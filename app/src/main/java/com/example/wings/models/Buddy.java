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
    public static final String KEY_RECIEVEDREQUESTS = "receivedBuddyRequests";
    public static final String KEY_SENTREQUESTS = "sentBuddyRequests";
    public static final String KEY_HASBUDDY = "hasBuddy";       //they are paired and they SHOULD also have either a BuddyMeetp or BuddyTrip

    public static final String KEY_ONMEETUP = "onMeetup";
    public static final String KEY_BUDDYMEETUP = "buddyMeetUpInstance";

    public static final String KEY_ONBUDDYTRIP = "onBuddyTrip";
    public static final String KEY_BUDDYTRIP = "buddyTripInstance";

    //for now just use their buddy objectID as their buddy id for everything --> typically should be regenerating it every time

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
        setOnMeetup(false);
        setBuddyMeetUp(new BuddyMeetUp());
        setOnBuddyTrip(false);
        setBuddyTrip(new BuddyTrip());
    }

    //Getters and setters
    public ParseUser getUser(){
        ParseUser user = getParseUser(KEY_USER);
        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return user;
    }
    public void setUser(ParseUser sender){
        put(KEY_USER, sender);
    }


    public void setDestination(WingsGeoPoint destination){
        put(KEY_DESTINATION, destination);
    }
    public WingsGeoPoint getDestination(){
        WingsGeoPoint point = (WingsGeoPoint) getParseObject(KEY_DESTINATION);
        try {
            point.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return point;
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

    public boolean getOnMeetup(){
        return getBoolean(KEY_ONMEETUP);
    }
    public void setOnMeetup(boolean answer){
        put(KEY_ONMEETUP, answer);
    }

    public BuddyMeetUp getBuddyMeetUpInstance(){
        BuddyMeetUp meetUp = (BuddyMeetUp) getParseObject(KEY_BUDDYMEETUP);
        try {
            meetUp.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return meetUp;
    }
    public void setBuddyMeetUp(BuddyMeetUp meetUp){
        put(KEY_BUDDYMEETUP, meetUp);
    }

    public boolean getOnBuddyTrip(){
        return getBoolean(KEY_ONBUDDYTRIP);
    }
    public void setOnBuddyTrip(boolean answer){
        put(KEY_ONBUDDYTRIP, answer);
    }
    public BuddyTrip getBuddyTripInstance(){
        BuddyTrip trip = (BuddyTrip) getParseObject(KEY_BUDDYTRIP);
        try {
            trip.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return trip;
    }
    public void setBuddyTrip(BuddyTrip trip){
        put(KEY_BUDDYTRIP, trip);
    }
}
