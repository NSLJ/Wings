package com.example.wings.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;

@ParseClassName("BuddyMeetUp")
public class BuddyMeetUp extends ParseObject {
    private static final String TAG = "BuddyMeetUp";

    private static final String KEY_SENDERBUDDY = "senderBuddy";
    private static final String KEY_SENDERBUDDYID = "senderBuddyId";
    private static final String KEY_RECEIVERBUDDYID = "receiverBuddyId";
    private static final String KEY_RECEIVERBUDDY = "receiverBuddy";
    private static final String KEY_SENDERCONFIRMATION = "senderGaveConfirmation";
    private static final String KEY_RECEIVERCONFIRMATION = "receiverGaveConfirmation";

    private static final String KEY_NEAREACHOTHER = "nearEachOther";        //are they near enouogh to each to show dialog/ask them to start confirming?

    public BuddyMeetUp(){}

    public BuddyMeetUp(Buddy senderBuddy, Buddy receiverBuddy){
        setSenderBuddy(senderBuddy);
        setSenderBuddyId(senderBuddy.getObjectId());
        setReceiverBuddy(receiverBuddy);
        setReceiverBuddyId(receiverBuddy.getObjectId());
        setSenderGaveConfirmation(false);
        setReceiverGaveConfirmation(false);
        setNearEachOther(false);
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

    public boolean getSenderGaveConfirmation(){
        return getBoolean(KEY_SENDERCONFIRMATION);
    }
    public void setSenderGaveConfirmation(boolean answer){
        put(KEY_SENDERCONFIRMATION, answer);
    }
    public boolean getReceiverGaveConfirmation(){
        return getBoolean(KEY_RECEIVERCONFIRMATION);
    }
    public void setReceiverGaveConfirmation(boolean answer){
        put(KEY_RECEIVERCONFIRMATION, answer);
    }

    public boolean getNearEachOther(){
        return getBoolean(KEY_NEAREACHOTHER);
    }
    public void setNearEachOther(boolean answer){
        put(KEY_NEAREACHOTHER, answer);
    }
}
