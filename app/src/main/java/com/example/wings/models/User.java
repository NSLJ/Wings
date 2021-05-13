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
    public static final String KEY_PIN = "pin";
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

    //Ensure everything is declared
    public User(){
      /*  setUsername("");
        setPassword("");
        setEmail("");
        setFirstName("");
        setLastName("");
        setProfileSetUp(false);
        setRating(2.5);
       // setProfilePic(new ParseFile());
        setPin("0000");
        setCurrentLocation(new WingsGeoPoint(this, 0, 0));
        setQueriedDestination(new WingsGeoPoint(this, 0, 0));
        setDestinationString("default");
        setIsBuddy(false);

        List<TrustedContact> trustedContacts = new ArrayList<>();
        setTrustedContacts(trustedContacts);

        List<ParseUser> friends = new ArrayList<>();
        setFriends(friends);*/
    }

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
        WingsGeoPoint queriedDestination = (WingsGeoPoint) user.getParseObject(KEY_QUERIEDDESTINATION);
        if(queriedDestination == null){
            queriedDestination = new WingsGeoPoint(ParseUser.getCurrentUser(), 0, 0);
            saveUserData("queriedDestination");
        }

        //Ensure to get the data inside the object
        try {
            queriedDestination.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return queriedDestination;
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

    public String getPin(){
        return getString(KEY_PIN);
    }
    public void setPin(String pin){
        put(KEY_PIN, pin);
    }

    public ParseFile getProfilePic(){
        return getParseFile(KEY_PROFILEPICTURE);
    }

    public void setProfilePic(ParseFile imageURL){
        put(KEY_PROFILEPICTURE, imageURL);
    }

    //seems like we need a specific way to do this...
  /*  public List getTrustedContacts() throws JSONException {
        JSONArray jsonArray = getJSONArray(KEY_TRUSTEDCONTACTS);
        Log.d(TAG, jsonArray.toString());
       List<TrustedContact> trustedContacts = new ArrayList<TrustedContact>();
       /* for(int i = 0; i < jsonArray.length(); i++){
           trustedContacts.add(new TrustedContact(jsonArray.getJSONObject(i)));
        }
        return trustedContacts;
    }*/

    public List<TrustedContact> getTrustedContacts(){
        return getList(KEY_TRUSTEDCONTACTS);
    }
    public void setTrustedContacts(List<TrustedContact> contacts){
        put(KEY_TRUSTEDCONTACTS, contacts);
    }

    public List<ParseUser> getFriends(){
        return getList(KEY_FRIENDS);
    }
    public void setFriends(List<ParseUser> friends){
        put(KEY_FRIENDS, friends);
    }

    public Boolean getProfileSetUp(){ return getBoolean(KEY_PROFILESETUP); }
    public void setProfileSetUp(Boolean setup){ put(KEY_PROFILESETUP, setup); }

    public double getRating(){ return getDouble(KEY_RATING);}
    public boolean setRating(double rating){
        if((rating <= 5) && (rating > 0)) {
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

        WingsGeoPoint queriedDestination = getQueriedDestination();
        //buddy.setDestination(new WingsGeoPoint(user, WingsGeoPoint.getLatitude(queriedDestination), queriedDestination.getLongitude()));
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
