package com.example.wings.models.inParseServer;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;

@ParseClassName("BuddyTrip")
public class BuddyTrip extends ParseObject {
    private static final String TAG = "BuddyMeetUp";

    private static final String KEY_SENDERBUDDY = "senderBuddy";
    private static final String KEY_SENDERBUDDYID = "senderBuddyId";
    private static final String KEY_RECEIVERBUDDYID = "receiverBuddyId";
    private static final String KEY_RECEIVERBUDDY = "receiverBuddy";

    private static final String KEY_NEAREACHOTHER = "nearEachOther";        //are they near enouogh to each to show d

    public static final String KEY_STARTLOCATION = "startLocation";
    public static final String KEY_DESTINATION = "destination";
    public static final String KEY_EST = "est";
    public static final String KEY_TIMEELAPSED = "timeElapsed";

    public BuddyTrip(){}

    public BuddyTrip(Buddy senderBuddy, Buddy receiverBuddy, WingsGeoPoint startLocation, WingsGeoPoint tripDestination){
        setSenderBuddy(senderBuddy);
        setSenderBuddyId(senderBuddy.getObjectId());
        setReceiverBuddy(receiverBuddy);
        setReceiverBuddyId(receiverBuddy.getObjectId());

        setNearEachOther(false);

        setStartLocation(startLocation);
        setDestination(tripDestination);
        setEst(1111);
        setTimeElapsed(0);
    }



    public void setSenderBuddy(Buddy sender){
        put(KEY_SENDERBUDDY, sender);
    }
    public Buddy getSenderBuddy(){
        Buddy sender = (Buddy) getParseObject(KEY_SENDERBUDDY);
        try {
            sender.fetchIfNeeded();
        } catch (ParseException e) {
            Log.d(TAG, "getSenderBuddy(): error fetching senderBuddy");
            e.printStackTrace();
        }
        return sender;
    }
    public void setSenderBuddyId(String id){
        put(KEY_SENDERBUDDYID, id);
    }
    public String getSenderBuddyId(){
        return getString(KEY_SENDERBUDDYID);
    }
    public void setReceiverBuddyId(String id){
        put(KEY_RECEIVERBUDDYID, id);
    }
    public String getReceiverBuddyId(){
        return getString(KEY_RECEIVERBUDDYID);
    }

    public void setReceiverBuddy(Buddy receiver){
        put(KEY_RECEIVERBUDDY, receiver);
    }
    public Buddy getReceiverBuddy(){
        Buddy receiver = (Buddy) getParseObject(KEY_RECEIVERBUDDY);
        try {
            receiver.fetchIfNeeded();
        } catch (ParseException e) {
            Log.d(TAG, "getReceiverBuddy(): error fetching senderBuddy");
            e.printStackTrace();
        }
        return receiver;
    }

    public boolean getNearEachOther(){
        return getBoolean(KEY_NEAREACHOTHER);
    }
    public void setNearEachOther(boolean answer){
        put(KEY_NEAREACHOTHER, answer);
    }

    public void setStartLocation(WingsGeoPoint point){
        put(KEY_STARTLOCATION, point);
    }
    public WingsGeoPoint getStartLocation(){
        WingsGeoPoint start = (WingsGeoPoint) getParseObject(KEY_STARTLOCATION);
        try {
            start.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return start;
    }
    public void setDestination(WingsGeoPoint point){
        put(KEY_DESTINATION, point);
    }
    public WingsGeoPoint getDestination(){
        WingsGeoPoint destination = (WingsGeoPoint) getParseObject(KEY_DESTINATION);
        try {
            destination.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return destination;
    }
    public long getEst(){
        return getLong(KEY_EST);
    }
    public void setEst(long newEst){
        put(KEY_EST, newEst);
    }
    public long getTimeElapsed(){
        return getLong(KEY_TIMEELAPSED);
    }
    public void setTimeElapsed(long newTime){
        put(KEY_TIMEELAPSED, newTime);
    }
}
