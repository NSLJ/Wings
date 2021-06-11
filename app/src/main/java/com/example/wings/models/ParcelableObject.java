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
import java.util.ArrayList;
import java.util.List;

//Purpose:          This is used specifically to encapsulate ParseObjects/Users in order to make it parcelable and send them from Fragment to Fragment. Enables just one uniform way of sending data!

@Parcel
public class ParcelableObject {
    User user;
    ParseUser currParseUser;
    ParseUser otherParseUser;
    Buddy userBuddy;
    Buddy otherBuddy;
    BuddyMeetUp buddyMeetUp;
    BuddyTrip buddyTrip;
    WingsGeoPoint wingsGeoPoint;
    LatLng someLocation;
    TrustedContact trustedContact;
    BuddyRequest buddyRequest;
    String mode = "";
    String contextFrom = "";
    String meetUpId = "";
    boolean someBoolean = false;
    List<TrustedContact> trustedContacts = new ArrayList<>();

    public ParcelableObject(){}

    public ParcelableObject(User user){
        this.user = user;
    }
    public ParcelableObject(String mode){
        this.mode = mode;
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
    public void setTrustedContactList(List<TrustedContact> newList){
        trustedContacts.addAll(newList);
    }
    public List<TrustedContact> getTrustedContactList(){
        return trustedContacts;
    }
    public void setMode(String string){
        mode = string;
    }
    public String getMode(){
        return mode;
    }
    public void setContextFrom(String string){
        contextFrom = string;
    }
    public String getContextFrom(){
        return contextFrom;
    }
    public void setMeetUpId(String string){
        meetUpId = string;
    }
    public String getMeetUpId(){
        return meetUpId;
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

    public Buddy getCurrBuddy() {
        return userBuddy;
    }
    public void setCurrBuddy(Buddy buddy) {
        userBuddy = buddy;
    }

    public Buddy getOtherBuddy() {
        return otherBuddy;
    }
    public void setOtherBuddy(Buddy buddy) {
        otherBuddy = buddy;
    }

    public ParseUser getCurrParseUser() {
        return currParseUser;
    }
    public void setCurrParseUser(ParseUser parseUser) {
        currParseUser = parseUser;
    }
    public ParseUser getOtherParseUser() {
        return otherParseUser;
    }
    public void setOtherParseUser(ParseUser parseUser) {
        otherParseUser = parseUser;
    }
    public boolean getBoolean(){
        return someBoolean;
    }
    public void setBoolean(boolean bool){
        someBoolean = bool;
    }

}
