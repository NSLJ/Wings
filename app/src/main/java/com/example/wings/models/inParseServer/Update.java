package com.example.wings.models.inParseServer;


import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;

@ParseClassName("Update")
//Purpose:              Everytime a user needs to save infomation about another user --> save it into an Update object dedicated to that user's object id.
//                      It is then that user's task to update itself whenever they log in to the app --> app needs to constantly check for new Update objects about itself (e.g. userNeedUpdateId = own object id)
public class Update extends ParseObject {
    private static final String TAG = "Update";
    public static final String KEY_USER_NEED_UPDATE_ID = "userNeedUpdateId";
    public static final String KEY_IS_BUDDY = "isBuddy";
    public static final String KEY_NEW_RATINGS = "newRatings";
    public static final String KEY_NEW_REVIEWS = "newReviews";
    public static final String KEY_NEW_RECEIVED_REQUESTS = "newReceivedRequests";

    public Update(){}

    public Update(String userId){
        setUserNeedUpdateId(userId);
    }
    //Setters:
    public void setUserNeedUpdateId(String id){
        put(KEY_USER_NEED_UPDATE_ID, id);
    }
    public void setIsBuddy(boolean answer){
        put(KEY_IS_BUDDY, answer);
    }
    public void setNewRatings(List<Float> newRatings){
        put(KEY_NEW_RATINGS, newRatings);
    }
    public void setNewReviews(List<Review> newReviews){
        put(KEY_NEW_REVIEWS, newReviews);
    }
    public void setNewReceivedRequests(List<BuddyRequest> newReceivedRequests){
        put(KEY_NEW_RECEIVED_REQUESTS, newReceivedRequests);
    }


    //Getters:
    public String getUserNeedUpdateId(){
        return getString(KEY_USER_NEED_UPDATE_ID);
    }
    public boolean getIsBuddy(){
        return getBoolean(KEY_IS_BUDDY);
    }
    public List<Float> getRatings(){
        return getList(KEY_NEW_RATINGS);
    }
    public List<Review> getReviews(){
        return getList(KEY_NEW_REVIEWS);
    }
    public List<BuddyRequest> getReceivedRequests(){
        return getList(KEY_NEW_RECEIVED_REQUESTS);
    }

}
