package com.example.wings.models.inParseServer;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

//Needed because apparently Parse server will not allow multiple geopoints, so we'll save points to this class instead
@ParseClassName("WingsGeoPoint")
public class WingsGeoPoint extends ParseObject {
    private static final String TAG = "WingsGeoPoint";
    public static final String KEY_USER = "user";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    private WingsGeoPoint wingsGeoPointInstance;

    //Purpose:      Set everything up here, include default values:
    public WingsGeoPoint(ParseUser user, double latitude, double longitude){
        setUser(user);
        setLatitude(latitude);
        setLongitude(longitude);
        setLocation(latitude, longitude);
    }

    public WingsGeoPoint(){}            //required

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }
    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint(KEY_LOCATION);
    }
    public void setLocation(double latitude, double longitude){
        put(KEY_LOCATION, new ParseGeoPoint(latitude, longitude));
    }

    public double getLatitude(){
        return getNumber(KEY_LATITUDE).doubleValue();
    }
    public void setLatitude(double latitude){
        Log.d(TAG, "setLatitude()");
        put(KEY_LATITUDE, latitude);
    }

    public void setLongitude(double longitude){
        put(KEY_LONGITUDE, longitude);
    }
    public double getLongitude(){
        return getNumber(KEY_LONGITUDE).doubleValue();
    }

    public void reset(){
        setLatitude(0);
        setLongitude(0);
        setLocation(0, 0);
        this.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Log.d(TAG, "this WingsGeoPoint reset successfully.");
                }
                else{
                    Log.d(TAG, "this WingsGeoPoint reset successfully.");
                }
            }
        });
    }


}
