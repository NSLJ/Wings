package com.example.wings.models.inParseServer;

import com.example.wings.models.User;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Review")
public class Review extends ParseObject {
    private static final String TAG = "Review";

    private static final String KEY_FOR_USER = "forUserId";
    private static final String KEY_FROM_USER = "fromUserId";
    private static final String KEY_BODY = "body";
    private static final String KEY_RATING = "rating";

    public Review(){}

    public Review(String forUserId, String fromUserId, String body, float rating){
        setForUserId(forUserId);
        setFromUserId(fromUserId);
        setBody(body);
        setRating(rating);
    }

    public void setForUserId(String userId){
        put(KEY_FOR_USER, userId);
    }
    public void setFromUserId(String userId){
        put(KEY_FROM_USER, userId);
    }
    public void setBody(String body){
        put(KEY_BODY, body);
    }
    public void setRating(float rating){
        put(KEY_RATING, rating);
    }

    public String getForUserId(){
        return getString(KEY_FOR_USER);
    }
    public String getFromUserId(){
        return getString(KEY_FROM_USER);
    }
    public String getBody(){
        return getString(KEY_BODY);
    }
    public float getRating(){
        return (float) getNumber(KEY_RATING);
    }

}
