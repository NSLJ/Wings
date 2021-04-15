package com.example.wings.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("User")
public class User extends ParseObject {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_FIRSTNAME = "FirstName";
    public static final String KEY_LASTNAME = "LastName";
    public static final String KEY_PIN = "Pin";
    public static final String KEY_PROFILEPICTURE = "ProfilePicture";
    public static final String KEY_TRUSTEDCONTACTS = "trusedContacts";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_CURRENTLOCATION = "currentLocation";
    public static final String KEY_PROFILESETUP  = "ProfileSetUp";
    public static final String KEY_RATING = "rating";
    //not sure if we will need this
    public static final String KEY_OBJECTID = "objectId";

    //returns the username part of the cpp email (Example: billybronco@cpp.edu would return billybronco)
    public String getUsername(){
        String username = "";
        //index of the @ char
        int atindex = getEmail().indexOf("@");

        //gets substring of email address until @
        username = getEmail().substring(0, atindex);
        return username;
    }

    //not sure if we will need this, might just call it in getUsername...
    public void setUsername(String username){
        put(KEY_USERNAME, username);
    }

    public String getPassword(){
        return getString(KEY_PASSWORD);
    }

    public void setPassword(String password){
        put(KEY_PASSWORD, password);
    }

    public String getEmail(){
        return getString(KEY_EMAIL);
    }

    public void setEmail(String Email){
        put(KEY_EMAIL, Email);
    }
    public String getFirstName(){
        return getString(KEY_FIRSTNAME);
    }

    public void setFirstName(String firstName){
        put(KEY_FIRSTNAME, firstName);
    }

    public String getLastName(){
        return getString(KEY_LASTNAME);
    }

    public void setLastName(String LastName){
        put(KEY_LASTNAME, LastName);
    }

    public int getPin(){
        return getInt(KEY_PIN);
    }

    //can do more error handling in execution
    public void setPin(int pin){
        if((pin > 999) && (pin < 10000)){
            put(KEY_PIN, pin);
        }
    }

    public ParseFile getProfilePic(){
        return getParseFile(KEY_PROFILEPICTURE);
    }

    public void setProfilePic(ParseFile imageURL){
        put(KEY_PROFILEPICTURE, imageURL);
    }

    public Boolean getProfileSetUp(){
        return getBoolean(KEY_PROFILESETUP);
    }

    public void setProfileSetUp(Boolean setup){
        put(KEY_PROFILESETUP, setup);
    }

    public int getRating(){
        return getInt(KEY_RATING);
    }

    public void setRating(int rating){
        put(KEY_RATING, rating);
    }

    public String getObjectID(){
        return getString(KEY_OBJECTID);
    }
}
