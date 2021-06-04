package com.example.wings.models.inParseServer;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("TrustedContact")
public class TrustedContact extends ParseObject {
    private static final String TAG = "TrustedContact";

    public static final String KEY_USER = "user";
    public static final String KEY_NAME = "fullName";
    public static final String KEY_RELATIONSHIP = "relationship";
    public static final String KEY_PHONENUMBER = "phoneNumber";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_OBJECTID = "objectId";

    //Used to check if these fields are filled out by the currentUser (but not yet uploaded into Parse4):
    boolean isComplete = false;

    public TrustedContact() {}

    public ParseUser getUser(){
        ParseUser user = getParseUser(KEY_USER);
        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            Log.d(TAG, "error fetching user="+e.getLocalizedMessage());
        }
        return user;
    }
    public void setUser(ParseUser userTrustedBy){
        put(KEY_USER, userTrustedBy);
    }

    public String getName(){
        return getString(KEY_NAME);
    }
    public void setName(String name){
        put(KEY_NAME, name);
    }

    public String getRelationship(){
        return getString(KEY_RELATIONSHIP);
    }
    public void setRelationship(String relationship){
        put(KEY_RELATIONSHIP, relationship);
    }

    public String getPhoneNumber(){
        return getString(KEY_PHONENUMBER);
    }
    public void setPhoneNumber(String phonenumber){
        put(KEY_PHONENUMBER, phonenumber);
    }

    public String getEmail(){
        return getString(KEY_EMAIL);
    }
    public void setEmail(String email){
        put(KEY_EMAIL, email);
    }

    public String getObjectID() {
        return getString(KEY_OBJECTID);
    }

    public void setIsComplete(boolean answer){
        isComplete = answer;
    }
    public boolean getIsComplete(){
        return isComplete;
    }
}
