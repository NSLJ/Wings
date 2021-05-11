package com.example.wings.models;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

//Needed because apparently Parse server will not allow multiple geopoints, so we'll save points to this class instead
@ParseClassName("WingsGeoPoint")
public class WingsGeoPoint extends ParseObject {
    private static final String DEBUG_TAG = "WingsGeoPoint";
    public static final String KEY_USER = "user";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public WingsGeoPoint(ParseUser user, double latitude, double longitude){
        setUser(user);
        setLatitude(latitude);
        setLongitude(longitude);
        setLocation(latitude, longitude);
    }

    public WingsGeoPoint(){}            //required

    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }
    public void setLocation(double latitude, double longitude){
        put(KEY_LOCATION, new ParseGeoPoint(latitude, longitude));
    }
    public void setLatitude(double latitude){
        Log.d(DEBUG_TAG, "setLatitude()");
        put(KEY_LATITUDE, latitude);
    }
    public void setLongitude(double longitude){
        put(KEY_LONGITUDE, longitude);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint(KEY_LOCATION);
    }
    public double getLatitude(){
        return (double) getNumber(KEY_LATITUDE);
    }
    public double getLongitude(){
        return (double) getNumber(KEY_LONGITUDE);
    }
    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

}
