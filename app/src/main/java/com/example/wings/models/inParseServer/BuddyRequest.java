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

    public BuddyRequest() {}

    public BuddyRequest(Buddy sender, Buddy receiver){
        setSender(sender);
        setReceiver(receiver);
        setIsConfirmed(false);
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


}
