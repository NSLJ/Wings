package com.example.wings.models;

import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class BuddyRequest extends ParseObject {

    public static final String KEY_OBJECTID = "objectId";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_TARGETDEST = "targetDestination";
    public static final String KEY_ISCONFIRMED = "isConfirmed";
    public static final String KEY_MEETINGMETHOD = "meetingMethod";

    public BuddyRequest() {
    }

    public ParseUser getSender(){
        return getParseUser(KEY_SENDER);
    }

    public void setSender(ParseUser user){
        put(KEY_SENDER, user);
    }

    public ParseUser getReveiver(){
        return getParseUser(KEY_RECEIVER);
    }

    public void setReceiver(ParseUser user){
        put(KEY_RECEIVER, user);
    }

    public ParseGeoPoint getTargetDestination(){
        return getParseGeoPoint(KEY_TARGETDEST);
    }

    public void setTargetDestination(ParseGeoPoint dest){
        put(KEY_TARGETDEST, dest);
    }

    public Boolean getIsConfirmed(){
        return getBoolean(KEY_ISCONFIRMED);
    }

    public void setIsConfirmed(Boolean confirm){
        put(KEY_ISCONFIRMED, confirm);
    }

    public String getMeetingMethod(){
        return getString(KEY_MEETINGMETHOD);
    }

    public void setMeetingMethod(String method){
        put(KEY_MEETINGMETHOD, method);
    }

    public String getObjectID() {
        return getString(KEY_OBJECTID);
    }
}
