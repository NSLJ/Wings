package com.example.wings.models;

import com.parse.ParseGeoPoint;

import org.parceler.Parcel;

import java.io.File;
import java.util.List;

@Parcel
public class ParcelUser {
    /*private String fName;
    private String lName;
    private List<TrustedContact> trustedContacts;
    private List<ParcelUser> friends;
    private String username;
    private boolean profileSetUp;
    private String password;
    private String email;
    private double rating;
    private File profilePic;
    private ParseGeoPoint currentLoc;*/
    private User user;

    public ParcelUser(){}

    public ParcelUser(User user){
        this.user = user;
    }

    public User getUser(){
        return user;
    }
    /*
    public String getfName() {
        return fName;
    }
    public void setfName(String fName) {
        this.fName = fName;
    }
    public String getlName() {
        return lName;
    }
    public void setlName(String lName) {
        this.lName = lName;
    }*/
}
