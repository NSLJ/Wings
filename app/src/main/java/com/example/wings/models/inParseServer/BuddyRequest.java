package com.example.wings.models.inParseServer;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;

//lJoWkyVJ55
@ParseClassName("BuddyRequest")
public class BuddyRequest extends ParseObject {
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_ISCONFIRMED = "isConfirmed";
    public static final String KEY_TRIPDESTINATION = "tripDestination";
    public BuddyRequest() {}

    public BuddyRequest(Buddy sender, Buddy receiver, WingsGeoPoint destination){
        setSender(sender);
        setReceiver(receiver);
        setIsConfirmed(false);
        setDestination(destination);
    }

    public Buddy getSender(){
        Buddy buddy = (Buddy) getParseObject(KEY_SENDER);
        try {
            buddy.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return buddy;
    }
    public void setSender(Buddy buddy){
        put(KEY_SENDER, buddy);
    }

    public Buddy getReceiver(){
        Buddy buddy = (Buddy) getParseObject(KEY_RECEIVER);
        try {
            buddy.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return buddy;
    }
    public void setReceiver(Buddy buddy){
        put(KEY_RECEIVER, buddy);
    }

    public Boolean getIsConfirmed(){
        return getBoolean(KEY_ISCONFIRMED);
    }
    public void setIsConfirmed(Boolean confirm){
        put(KEY_ISCONFIRMED, confirm);
    }

    public void setDestination(WingsGeoPoint point){
        put(KEY_TRIPDESTINATION, point);
    }
    public WingsGeoPoint getDestination(){
        WingsGeoPoint destination = (WingsGeoPoint) getParseObject(KEY_TRIPDESTINATION);
        try {
            destination.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return destination;
    }

}
