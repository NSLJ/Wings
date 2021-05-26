package com.example.wings.models.inParseServer;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONObject;
@ParseClassName("TrustedContact")
public class TrustedContact extends ParseObject {
    public static final String KEY_TRUSTEDBY = "TrustedBy";
    public static final String KEY_FIRSTNAME = "FirstName";
    public static final String KEY_LASTNAME = "LastName";
    public static final String KEY_RELATIONSHIP = "Relationship";
    public static final String KEY_PHONENUMBER = "PhoneNumber";
    public static final String KEY_EMAIL = "Email";
    public static final String KEY_OBJECTID = "objectId";

    public TrustedContact() {}

    //Still playing around with
    public TrustedContact(JSONObject object){
        ParseObject trustedContact = new ParseObject("TrustedContact");

        //setUserTrustedBy(object.get);
    }

    public ParseUser getUserTrustedBy(){
        return getParseUser(KEY_TRUSTEDBY);
    }
    public void setUserTrustedBy(ParseUser userTrustedBy){
        put(KEY_TRUSTEDBY, userTrustedBy);
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
    public void setLastName(String lastname){
        put(KEY_LASTNAME, lastname);
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
}
