package com.example.wings.models;

import android.util.Log;

import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.example.wings.models.inParseServer.TrustedContact;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


//Purpose:          allows us to get info from Parse database through passing in a ParseUser as User{} cannot GET info by itself --> needs a ParseUser. BUT, User{} CAN instantiate and create a Parseuser to be saved in database.
//                  Rules then:     If you need to GET info --> pass in a ParseUser to get info from
//                                  If you need to SET info --> User{} assume you are making a new User to save as a ParseUser later
//                                      SO --> Setters do NOT update info into a ParseUser --> TODO: separate these inconsistencies or have User{} ONLY serve as connection between ParseUser and Not to initalize a new ParseUser in database.
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
        return user.getString(KEY_PASSWORD);
    }
    public void setPassword(String password){
        put(KEY_PASSWORD, password);
    }

    public String getEmail(){
        return user.getString(KEY_EMAIL);
    }
    public void setEmail(String email){
        put(KEY_EMAIL, email);
    }

    public String getFirstName(){
        return user.getString(KEY_FIRSTNAME);
    }
    public void setFirstName(String firstName){
        put(KEY_FIRSTNAME, firstName);
    }

    public String getLastName(){
        return user.getString(KEY_LASTNAME);
    }
    public void setLastName(String lastName){ put(KEY_LASTNAME, lastName); }

    public void setCurrentLocation(WingsGeoPoint location){
        put(KEY_CURRENTLOCATION, location);
    }
    public WingsGeoPoint getCurrentLocation(){
        WingsGeoPoint currLocation = (WingsGeoPoint) user.getParseObject(KEY_CURRENTLOCATION);
        try {
            currLocation.fetchIfNeeded();
        } catch (ParseException e) {
            Log.e(TAG, "getCurrentLocation(): error fetching currLocation, e=", e);
        }
        return currLocation;
    }

    public void setQueriedDestination(WingsGeoPoint location){
        put(KEY_QUERIEDDESTINATION, location);
    }
    public WingsGeoPoint getQueriedDestination(){
        WingsGeoPoint queriedDestination = (WingsGeoPoint) user.getParseObject(KEY_QUERIEDDESTINATION);
       /* if(queriedDestination == null){               //I do not remember why I did this as it was a few weeks ago --> may still need it
            queriedDestination = new WingsGeoPoint(ParseUser.getCurrentUser(), 0, 0);
            saveUserData("queriedDestination");
        }*/

        //Ensure to get the data inside the object
        try {
            queriedDestination.fetchIfNeeded();
        } catch (ParseException e) {
            Log.e(TAG, "getQueriedDestination(): error fetching queriedDestination, e=", e);
        }
        return queriedDestination;
    }

    public void setIsBuddy(boolean answer){
        put(KEY_ISBUDDY, answer);
    }
    public boolean getIsBuddy(){
        return user.getBoolean(KEY_ISBUDDY);
    }

    public void setDestinationString(String destinationString){
        put(KEY_DESTINATIONSTR, destinationString);
    }

    public String getDestinationString(){
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
        return user.getString(KEY_PIN);
    }
    public void setPin(String pin){
        put(KEY_PIN, pin);
    }

    public ParseFile getProfilePic(){
        return user.getParseFile(KEY_PROFILEPICTURE);
    }
    public void setProfilePic(ParseFile imageURL){
        put(KEY_PROFILEPICTURE, imageURL);
    }

   public List<TrustedContact> getTrustedContacts(){
        List<TrustedContact> trustedContacts = user.getList(KEY_TRUSTEDCONTACTS);
        Log.d(TAG, "getTrustedContacts():   trustedContacts = " + trustedContacts.toString());
        return trustedContacts;
    }
    public void setTrustedContacts(List<TrustedContact> contacts){
        put(KEY_TRUSTEDCONTACTS, contacts);
    }

    public List<ParseUser> getFriends(){
        return user.getList(KEY_FRIENDS);
    }
    public void setFriends(List<ParseUser> friends){
        put(KEY_FRIENDS, friends);
    }

    public Boolean getProfileSetUp(){ return user.getBoolean(KEY_PROFILESETUP); }
    public void setProfileSetUp(Boolean setup){ put(KEY_PROFILESETUP, setup); }

    public double getRating(){ return user.getDouble(KEY_RATING);}
    public boolean setRating(double rating){
        if((rating <= 5) && (rating > 0)) {
            put(KEY_RATING, rating);
            return true;
        } else{
            return false;
        }
    }

    public Buddy getBuddyInstance() {
        Buddy buddyInstance = (Buddy) user.getParseObject(KEY_BUDDY);
        //If the user's "buddyInstance" field is not yet declared for some reason:
      /*  if (buddyInstance == null) {
            buddyInstance = createBuddy();     //creates corresponding buddy instance for currentUser field
        }*/

        //Actually fetch the data:
        try {
            buddyInstance.fetchIfNeeded();
        } catch (ParseException e) {
            Log.e(TAG, "getBuddy(): error fetching user's buddyInstance, e=", e);
        }
        return buddyInstance;
    }



    //Creates buddy object using user field
   /* public Buddy createBuddy(){
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
    }*/

    //Helper method:
    private void saveUserData(String text){
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


    //TODO: time of current location, history of locations
    public String getEmergencyMessage(){
        String emergencyMesssage = "URGENT: I sent this through Wings' Emergency Service, I need urgent help right now! I am currently dialing the police, but here is my current information:\n"
                + getCurrentLocationMessage();

        if(!getIsBuddy()){
            emergencyMesssage += "\n\n\tI was NOT with a paired Buddy when I called for emergency services.";
        }
        else if(getBuddyInstance().getHasBuddy()){
            emergencyMesssage += "\n\n\tI was NOT with a paired Buddy when I called for emergency services.";
        }
        else if(getBuddyInstance().getOnMeetup()){
            emergencyMesssage += "\n\n\tI was MEETING with a paired Buddy when I called for emergency services. Here is their information:\n"
                    + getBuddyInfo()
                    + "\n\n\tHere is our Trip information:\n"
                    + getTripInfo();
        }
        else if(getBuddyInstance().getOnBuddyTrip()){
            emergencyMesssage += "\n\n\tI was WALKING with a paired Buddy when I called for emergency services. Here is their information:\n"
                    + getBuddyInfo()
                    + "\n\n\tHere is our Trip information:\n"
                    + getTripInfo();
        }
        else{   //User doesn't fit in one of these stages --> must be an error
            Log.e(TAG, "getEmergencyMessage(): user is not in one of the acceptable stages!");
        }

        emergencyMesssage += "\n\n\tHere are the others I have contacted with this EMERGENCY information. Get in contact with each other and the police! :" + getTCsNotifiedMessage();
        return emergencyMesssage;
    }
    public String getNotifyMessage(){
        String notifyMesssage = "I sent this through Wing's Notification Service. I feel unsafe right now, here is my current information just in case! I will notify again once I am okay!\n"
                + getCurrentLocationMessage();

        if(!getIsBuddy()){
            notifyMesssage += "\n\n\tI was NOT with a paired Buddy when I called for notification services.";
        }
        else if(getBuddyInstance().getHasBuddy()){
            notifyMesssage += "\n\n\tI was NOT with a paired Buddy when I called for notification services.";
        }
        else if(getBuddyInstance().getOnMeetup()){
            notifyMesssage += "\n\n\tI was MEETING with a paired Buddy when I called for notification services. Here is their information:\n"
                    + getBuddyInfo()
                    + "\n\n\tHere is our Trip information:\n"
                    + getTripInfo();
        }
        else if(getBuddyInstance().getOnBuddyTrip()){
            notifyMesssage += "\n\n\tI was WALKING with a paired Buddy when I called for notification services. Here is their information:\n"
                    + getBuddyInfo()
                    + "\n\n\tHere is our Trip information:\n"
                    + getTripInfo();
        }
        else{   //User doesn't fit in one of these stages --> must be an error
            Log.e(TAG, "getEmergencyMessage(): user is not in one of the acceptable stages!");
        }

        notifyMesssage += "\n\n\tHere are the others I have contacted with this information. If you don't hear back from me, you may want to contact each other!:" + getTCsNotifiedMessage();
        return notifyMesssage;
    }



    private static final String SHOW_MAP_BASE_URL = "https://maps.google.com/?q=%f,%f";             //When used --> use String.format() to fill in the %f's
    //Helper strings:
    public String getCurrentLocationMessage(){
        //1.) Build the URL:
        WingsGeoPoint currLocation = getCurrentLocation();
        return "\tMy current location:     " + getLocationLink(currLocation);
    }

    //Purpose:          Because this url is used many times w/ different locations --. reduce boilerplate
    public String getLocationLink(WingsGeoPoint locationToLink){
        return String.format(SHOW_MAP_BASE_URL, locationToLink.getLatitude(), locationToLink.getLongitude());
    }

    public String getTCsNotifiedMessage(){
        /*Here are the others I have contacted with this EMERGENCY information. Get in contact with each other and the police! :
            - Friend #2 (123) 123-1234
            - Friend #3 (123) 321-3211 */

        String message = "";

        List<TrustedContact> trustedContacts = getTrustedContacts();
        for(int i = 0; i < trustedContacts.size(); i++){
            TrustedContact currTC = trustedContacts.get(i);
            try {
                currTC.fetchIfNeeded();
                Log.d(TAG, "currTC = " + currTC.getObjectId());
                message += "\n\t\t- " + currTC.getName() + " " + currTC.getFormattedPhoneNumber();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return message;
    }


    public String getBuddyInfo(){
        /*Buddy's name:                        FName LastName
        Buddy's current location:              https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717
        */

        String result = "\t\tBuddy's name:                        ";
        //Find out which if user is Sender or ReceiverBuddy --> get the other buddy:
        Buddy buddyInstance = getBuddyInstance();

        //Are we on meetUp? --> check through buddyMeetUp instance:
        if(buddyInstance.getOnMeetup()){
            BuddyMeetUp meetUpInstance = buddyInstance.getBuddyMeetUpInstance();
            // User is the receiver
            if(user.getObjectId().equals(meetUpInstance.getReceiverBuddyId())){
                Buddy partner = meetUpInstance.getSenderBuddy();
                ParseUser partnerParseUser = partner.getUser();

                //Wrap the ParseUser in our User{}:         //user{} allows us to get info from Parse database through passing in a ParseUser. User{} needs a ParseUser to get info. B
                User partnerLocalUser = new User(partnerParseUser);
                result += partnerLocalUser.getFirstName() + " " + partnerLocalUser.getLastName() + "\n\t\tBuddy's current location:              ";
                result += getLocationLink(partnerLocalUser.getCurrentLocation());
            }
            else if(user.getObjectId().equals(meetUpInstance.getSenderBuddyId())){
                Buddy partner = meetUpInstance.getReceiverBuddy();
                ParseUser partnerParseUser = partner.getUser();         //TODO: remove repeated code

                //Wrap the ParseUser in our User{}:
                User partnerLocalUser = new User(partnerParseUser);
                result += partnerLocalUser.getFirstName() + " " + partnerLocalUser.getLastName() + "\n\t\tBuddy's current location:              ";
                result += getLocationLink(partnerLocalUser.getCurrentLocation());
            }
            else{
                Log.e(TAG, "User was not the receiver or sender?");
            }
        }

        //Are we onTrip? --> check through buddyTrip instance:
        else if(buddyInstance.getOnBuddyTrip()){
            BuddyTrip buddyTripInstance = buddyInstance.getBuddyTripInstance();
            // User is the receiver
            if(user.getObjectId().equals(buddyTripInstance.getReceiverBuddyId())){          //TODO: remove repeated code, this is the exact as buddyMeetUp
                Buddy partner = buddyTripInstance.getSenderBuddy();
                ParseUser partnerParseUser = partner.getUser();

                //Wrap the ParseUser in our User{}:
                User partnerLocalUser = new User(partnerParseUser);
                result += partnerLocalUser.getFirstName() + " " + partnerLocalUser.getLastName() + "\n\t\tBuddy's current location:              ";
                result += getLocationLink(partnerLocalUser.getCurrentLocation());
            }
            else if(user.getObjectId().equals(buddyTripInstance.getSenderBuddyId())){
                Buddy partner = buddyTripInstance.getReceiverBuddy();
                ParseUser partnerParseUser = partner.getUser();

                //Wrap the ParseUser in our User{}:
                User partnerLocalUser = new User(partnerParseUser);
                result += partnerLocalUser.getFirstName() + " " + partnerLocalUser.getLastName() + "\n\t\tBuddy's current location:              ";
                result += getLocationLink(partnerLocalUser.getCurrentLocation());
            }
            else{
                Log.e(TAG, "User was not the receiver or sender?");
            }
        }
        else{       //this method wouldn't be called if it wasn't the two situations above
            Log.e(TAG, "getBuddyInfo():  did not match onMeetup or onBuddyTrip");
            return "";
        }

        return result;
    }

    public String getTripInfo(){
        /*
         Here is our trip information:
                Approximate meet up location:                   https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717
                Our intended destination AFTER meeting up:      https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

         */
        String result ="";
        Buddy buddyInstance = getBuddyInstance();

        //Are we on meetUp? --> check through buddyMeetUp instance:
        if(buddyInstance.getOnMeetup()){
            BuddyMeetUp meetUpInstance = buddyInstance.getBuddyMeetUpInstance();
            // User is the receiver
            if(user.getObjectId().equals(meetUpInstance.getReceiverBuddyId())){
                Buddy partner = meetUpInstance.getSenderBuddy();
                ParseUser partnerParseUser = partner.getUser();

                //Wrap the ParseUser in our User{}:         //user{} allows us to get info from Parse database through passing in a ParseUser. User{} needs a ParseUser to get info. B
                User partnerLocalUser = new User(partnerParseUser);
                WingsGeoPoint partnerCurrLocation = partnerLocalUser.getCurrentLocation();
                WingsGeoPoint userCurrLocation = getCurrentLocation();

                //Find middle geopoint:
                double[] middlePoint = findMidPoint(userCurrLocation, partnerCurrLocation);
                result += "\t\tApproximate meet up location:                   " + getLocationLink(new WingsGeoPoint(middlePoint[0], middlePoint[1])) +"\n";         //may give us bugs in Parse due to constructor not usually used this way w/o user

            }
            else if(user.getObjectId().equals(meetUpInstance.getSenderBuddyId())){
                Buddy partner = meetUpInstance.getReceiverBuddy();
                ParseUser partnerParseUser = partner.getUser();         //TODO: remove repeated code

                //Wrap the ParseUser in our User{}:
                User partnerLocalUser = new User(partnerParseUser);
                WingsGeoPoint partnerCurrLocation = partnerLocalUser.getCurrentLocation();
                WingsGeoPoint userCurrLocation = getCurrentLocation();

                //Find middle geopoint:
                double[] middlePoint = findMidPoint(userCurrLocation, partnerCurrLocation);
                result += "\t\tApproximate meet up location:                   " + getLocationLink(new WingsGeoPoint(middlePoint[0], middlePoint[1])) +"\n";       //may give us bugs in Parse due to constructor not usually used this way w/o user
            }
            else{
                Log.e(TAG, "User was not the receiver or sender?");
            }
        }
        //Just assume we are on BuddyTrip --> append this no matter what:
        result += "\t\tOur intended destination AFTER meeting up:      " + getLocationLink(buddyInstance.getDestination());

        return result;
    }

    //TODO: This technically should be somewhere else and should've been saved in Parse database + users should'be been mapped here.
    //Purpose:      formula to compute midpoint between two geopoints. returns latitude in index 0, longitude iin index 1 of array
    // https://stackoverflow.com/questions/11682164/find-center-geopoint-between-start-geo-point-and-end-geo-point-on-android
    public double[] findMidPoint(WingsGeoPoint userCurrLocation, WingsGeoPoint partnerCurrLocation){
        double userLat = userCurrLocation.getLatitude();
        double userLong = userCurrLocation.getLongitude();
        double partnerLat = partnerCurrLocation.getLatitude();
        double partnerLong = partnerCurrLocation.getLongitude();


        double dLon = Math.toRadians(partnerLong - userLong);

//convert to radians
        userLat = Math.toRadians(userLat);
        partnerLat = Math.toRadians(partnerLat);
        userLong = Math.toRadians(userLong);

        double Bx = Math.cos(partnerLat) * Math.cos(dLon);
        double By = Math.cos(partnerLat) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(userLat) + Math.sin(partnerLat), Math.sqrt((Math.cos(userLat) + Bx) * (Math.cos(userLat) + Bx) + By * By));
        double lon3 = userLong + Math.atan2(By, Math.cos(userLat) + Bx);

//print out in degrees
        return new double[]{Math.toDegrees(lat3), Math.toDegrees(lon3)};
    }

            /*
        User is not a buddy:
        URGENT: I sent this through Wings' Emergency Service, I need urgent help right now! I am currently dialing the police, but here is my current information:
               My current location:     https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               I was NOT with a Buddy when I called for emergency services.
               Here are the others I have contacted with this EMERGENCY information. Get in contact with each other and the police! :
                    - Friend #2 (123) 123-1234
                    - Friend #3 (123) 321-3211
                                                                        */  //TODO: allow user to provide a text message to send
        /*
        User is meeting up:
        URGENT: I sent this through Wings' Emergency Service, I need urgent help right now! I am currently dialing the police, but here is my current information:
               My current location:     https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               I was MEETING with my Buddy when I called for emergency services. Here is their information:
                    Buddy's name:                                   FName LastName
                    Buddy's current location:                       https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               Here is our trip information:
                    Approximate meet up location:                   https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717
                    Our intended destination AFTER meeting up:      https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               Here are the others I have contacted with this EMERGENCY information. Get in contact with each other and the police! :
                    - Friend #2 (123) 123-1234
                    - Friend #3 (123) 321-3211
                                                                        */

        /*
        User is on a trip with Buddy:
           URGENT: I sent this through Wings' Emergency Service, I need urgent help right now! I am currently dialing the police, but here is my current information:
               My current location:     https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               I was WALKING WITH my Buddy when I called for emergency services. Here is their information:
                    Buddy's name:                                   FName LastName
                    Buddy's current location:                       https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               Here is our trip information:
                    Our intended destination AFTER meeting up:      https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               Here are the others I have contacted with this EMERGENCY information. Get in contact with each other and the police! :
                    - Friend #2 (123) 123-1234
                    - Friend #3 (123) 321-3211
         */
    /*
        User is not a Buddy:
        I sent this through Wing's Notification Service. I feel unsafe right now, here is my current information just in case! I will notify again once I am okay!
               My current location:     https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

               Here are the others I have contacted with this information. If you don't hear back from me, you may want to contact each other!:
                    - Friend #2 (123) 123-1234
                    - Friend #3 (123) 321-3211

               I was NOT with a paired Buddy when I called for notification services.
         */
        /*
        User is meeting up:
        I sent this through Wing's Notification Service. I feel unsafe right now, here is my current information just in case! I will notify again once I am okay!
            My current location:     https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

            I was MEETING with my paired Buddy when I called for notification services. Here is their information:
                Buddy's name:                                   FName LastName
                Buddy's current location:                       https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

            Here is our trip information:
                Approximate meet up location:                   https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717
                Our intended destination AFTER meeting up:      https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

            Here are the others I have contacted with this information. If you don't hear back from me, you may want to contact each other!:
                - Friend #2 (123) 123-1234
                - Friend #3 (123) 321-3211
        */
        /* //TODO: provide link to info about Wing's Notification/Emergency service --> maybe a HackMD for now lol  --> potential to build website
        User is on a trip with Buddy:
        I sent this through Wing's Notification Service. I feel unsafe right now, here is my current information just in case! I will notify again once I am okay!
            My current location:     https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

            I was WALKING WITH my paired Buddy when I called for notification services. Here is their information:
                Buddy's name:                                   FName LastName
                Buddy's current location:                       https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717
                //TODO: perhaps a link to their Wing's profile

            Here is our trip information:
                Our intended destination AFTER meeting up:      https://www.google.com/maps/place/34%C2%B003'29.9%22N+117%C2%B049'18.4%22W/@34.0577322,-117.8218361,17z/data=!4m5!3m4!1s0x0:0x0!8m2!3d34.05831!4d-117.8217717

            Here are the others I have contacted with this information. If you don't hear back from me, you may want to contact each other!:
                - Friend #2 (123) 123-1234
                - Friend #3 (123) 321-3211
        */


}
