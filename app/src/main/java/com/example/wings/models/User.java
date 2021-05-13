package com.example.wings.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("User")
public class User extends ParseUser {
    private static final String TAG = "User";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_FIRSTNAME = "FirstName";
    public static final String KEY_LASTNAME = "LastName";
    public static final String KEY_PIN = "Pin";
    public static final String KEY_PROFILEPICTURE = "ProfilePicture";
    public static final String KEY_TRUSTEDCONTACTS = "trustedContacts";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_CURRENTLOCATION = "currentLocation";
    public static final String KEY_PROFILESETUP  = "ProfileSetUp";
    public static final String KEY_RATING = "rating";
    public static final String KEY_QUERIEDDESTINATION = "queriedDestination";
    public static final String KEY_ISBUDDY = "isBuddy";
    public static final String KEY_DESTINATIONSTR = "destinationString";
    public static final String KEY_BUDDY = "buddyInstance";

    //not sure if we will need this
    public static final String KEY_OBJECTID = "objectId";

    private ParseUser user;
    public User(){}

    public User(ParseUser user){
        this.user = user;
    }
    //returns the username part of the cpp email (Example: billybronco@cpp.edu would return billybronco)
    public String getUsername(){
        String username = "";
        //index of the '@' char
        int atIndex = getEmail().indexOf("@");

        //gets substring of email address until '@'
        username = getEmail().substring(0, atIndex);
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
    public void setEmail(String email){
        put(KEY_EMAIL, email);
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
    public void setLastName(String lastName){ put(KEY_LASTNAME, lastName); }

    public void setCurrentLocation(WingsGeoPoint location){
        put(KEY_CURRENTLOCATION, location);
    }


    public void setQueriedDestination(WingsGeoPoint location){
        put(KEY_QUERIEDDESTINATION, location);
    }
    public WingsGeoPoint getQueriedDestination(){
        return (WingsGeoPoint) user.getParseObject(KEY_QUERIEDDESTINATION);
    }

    public void setIsBuddy(boolean answer){
        put(KEY_ISBUDDY, answer);
    }
    public boolean getIsBuddy(){
        Boolean isBuddy = user.getBoolean(KEY_ISBUDDY);

        if(isBuddy == null){
            isBuddy = false;
            user.put(KEY_ISBUDDY, false);
            saveUserData("isBuddy");
        }

        return isBuddy;
    }


    public void setDestinationString(String destinationString){
        put(KEY_DESTINATIONSTR, destinationString);
    }

    //Purpose:      Every getter method will get the data from Parse, IF the data has never been declared, an object is declared and updated and then returns the updated data
    public String getDestinationString(){
        ParseUser user = ParseUser.getCurrentUser();
        String destinationStr = user.getString(KEY_DESTINATIONSTR);

        //Set it to the default value if doesn't exist:
        if(destinationStr == null){
            destinationStr = "default";
            user.put(KEY_DESTINATIONSTR, false);
            saveUserData("destinationString");
        }

        return destinationStr;
    }

    public int getPin(){
        return getInt(KEY_PIN);
    }
    //can do more error handling in execution
    //returns true if pin was set and false if not. Can be used for error handaling
    public boolean setPin(int pin){
        if((pin > 999) && (pin < 10000)){
            put(KEY_PIN, pin);
            return true;
        } else{
            return false;
        }
    }

    public ParseFile getProfilePic(){
        return getParseFile(KEY_PROFILEPICTURE);
    }

    public void setProfilePic(ParseFile imageURL){
        put(KEY_PROFILEPICTURE, imageURL);
    }

    //seems like we need a specific way to do this...
    public List getTrustedContacts() throws JSONException {
        JSONArray jsonArray = getJSONArray(KEY_TRUSTEDCONTACTS);
        Log.d(TAG, jsonArray.toString());
       List<TrustedContact> trustedContacts = new ArrayList<TrustedContact>();
       /* for(int i = 0; i < jsonArray.length(); i++){
           trustedContacts.add(new TrustedContact(jsonArray.getJSONObject(i)));
        }*/
        return trustedContacts;
    }

    public void setTrustedContacts(List contacts){
        while(!contacts.isEmpty()){

        }
    }


    public Boolean getProfileSetUp(){ return getBoolean(KEY_PROFILESETUP); }
    public void setProfileSetUp(Boolean setup){ put(KEY_PROFILESETUP, setup); }

    public int getRating(){ return getInt(KEY_RATING);}

    //TODO: Set error handling so rating must <= 5
    //returns true if rating was set and false if not
    public boolean setRating(int rating){
        if((rating < 6) && (rating > 0)) {
            put(KEY_RATING, rating);
            return true;
        } else{
            return false;

        }
    }

    public String getObjectID(){ return getString(KEY_OBJECTID); }



    public Buddy getBuddy() {
        Buddy buddyInstance = (Buddy) user.getParseObject(KEY_BUDDY);
        //If the user's "buddyInstance" field is not yet declared for some reason:
        if (buddyInstance == null) {
            buddyInstance = createBuddy();     //creates corresponding buddy instance for currentUser field
        }

        //Actually fetch the data:
        try {
            buddyInstance.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return buddyInstance;
    }


    //Creates buddy object using user field
    public Buddy createBuddy(){
        Log.d(TAG, "createBuddy()");
        //Create a new Buddy instance and link it to this user:
        Buddy buddy = new Buddy();
        buddy.setUser(user);

        WingsGeoPoint destination = getQueriedDestination();
        buddy.setDestination(new WingsGeoPoint(user, destination.getLatitude(), destination.getLongitude()));
        buddy.setHasBuddy(false);
        saveUserData("buddyInstance");

        user.put(KEY_ISBUDDY, true);
        user.put(KEY_BUDDY, buddy);
        return buddy;
    }

    //Helper method:
    private void saveUserData(String text){
        ParseUser user = ParseUser.getCurrentUser();
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Log.d(TAG, "no Error saving " + text);
                }
                else{
                    Log.d(TAG, "Error saving " + text + " error=" + e.getMessage());
                }
            }
        });
    }
}
