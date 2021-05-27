package com.example.wings.models;

import com.example.wings.models.inParseServer.Buddy;
import com.example.wings.models.inParseServer.BuddyMeetUp;
import com.example.wings.models.inParseServer.BuddyRequest;
import com.example.wings.models.inParseServer.BuddyTrip;
import com.example.wings.models.inParseServer.TrustedContact;
import com.example.wings.models.inParseServer.WingsGeoPoint;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.io.File;
import java.util.List;

//Purpose:          This was used specifically to encapsulate ParseObjects/Users in order to make it parcelable and send them from Fragment to Fragment.
//                  However, I totally forgot about doing this, therefore most Fragments pass objectIds to corresponding ParseObjects --> every Fragment would have to query for information they need.
//TODO: Make app frame more robust by  implementing this to encapsulate ParseObjects in a Parcelable object
@Parcel
public class ParcelableObject {
    User user;
    ParseUser parseUser;
    Buddy buddy;
    BuddyMeetUp buddyMeetUp;
    BuddyTrip buddyTrip;
    WingsGeoPoint wingsGeoPoint;
    LatLng someLocation;
    TrustedContact trustedContact;
    BuddyRequest buddyRequest;
    String someString;

    public ParcelableObject(){}

    public ParcelableObject(User user){
        this.user = user;
    }

    public User getUser(){
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public BuddyRequest getBuddyRequest() {
        return buddyRequest;
    }
    public void setBuddyRequest(BuddyRequest buddyRequest) {
        this.buddyRequest = buddyRequest;
    }

    public TrustedContact getTrustedContact() {
        return trustedContact;
    }
    public void setTrustedContact(TrustedContact trustedContact) {
        this.trustedContact = trustedContact;
    }

    public void setString(String string){
        someString = string;
    }
    public String getString(){
        return someString;
    }

    public LatLng getLocation() {
        return someLocation;
    }
    public void setLocation(LatLng someLocation) {
        this.someLocation = someLocation;
    }

    public WingsGeoPoint getWingsGeoPoint() {
        return wingsGeoPoint;
    }
    public void setWingsGeoPoint(WingsGeoPoint wingsGeoPoint) {
        this.wingsGeoPoint = wingsGeoPoint;
    }

    public BuddyTrip getBuddyTrip() {
        return buddyTrip;
    }

    public void setBuddyTrip(BuddyTrip buddyTrip) {
        this.buddyTrip = buddyTrip;
    }

    public BuddyMeetUp getBuddyMeetUp() {
        return buddyMeetUp;
    }

    public void setBuddyMeetUp(BuddyMeetUp buddyMeetUp) {
        this.buddyMeetUp = buddyMeetUp;
    }

    public Buddy getBuddy() {
        return buddy;
    }

    public void setBuddy(Buddy buddy) {
        this.buddy = buddy;
    }

    public ParseUser getParseUser() {
        return parseUser;
    }

    public void setParseUser(ParseUser parseUser) {
        this.parseUser = parseUser;
    }
}
