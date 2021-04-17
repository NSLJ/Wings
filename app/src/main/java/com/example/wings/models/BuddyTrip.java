package com.example.wings.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("BuddyTrip")
public class BuddyTrip extends ParseObject {
    public static final String KEY_DESTINATION = "destination";
    public static final String KEY_BUDDYONE = "buddyOne";
    public static final String KEY_BUDDYTWO = "buddyTwo";
    public static final String KEY_EST = "EST";
    public static final String KEY_TIMEELASPED = "timeElasped";
    public static final String KEY_OBJECTID = "objectId";

    public BuddyTrip() {}

    public ParseGeoPoint getDestination(){
        return getParseGeoPoint(KEY_DESTINATION);
    }
    public void setKeyDestination(ParseGeoPoint dest){
        put(KEY_DESTINATION, dest);
    }

    public ParseUser getBuddyOne(){
        return getParseUser(KEY_BUDDYONE);
    }
    public void setBuddyOne(ParseUser user){
        put(KEY_BUDDYONE, user);
    }

    public ParseUser getBuddyTwo(){
        return getParseUser(KEY_BUDDYTWO);
    }
    public void setBuddyTwo(ParseUser user){
        put(KEY_BUDDYTWO, user);
    }

    public double getEST(){
        return (Double) getNumber(KEY_EST);
    }
    public void setEST(Double est){
        put(KEY_EST, est);
    }

    public double timeElasped(){
        return (Double) getNumber(KEY_TIMEELASPED);
    }
    public void setTimeElasped(double time){
        put(KEY_TIMEELASPED, time);
    }

    public String getObjectID() {
        return getString(KEY_OBJECTID);
    }
}
