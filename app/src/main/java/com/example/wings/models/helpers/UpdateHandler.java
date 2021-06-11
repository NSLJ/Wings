package com.example.wings.models.helpers;

//Purpose:      For easy handling with Update model class. Makes queries, updates Parse database accordingly.

import android.util.Log;

import com.example.wings.models.inParseServer.BuddyRequest;
import com.example.wings.models.inParseServer.Review;
import com.example.wings.models.inParseServer.Update;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class UpdateHandler {
    private static final String TAG = "UpdateHandler";

    //Different modes:
    private static final String SAVE_RATING = "saveRating";
    private static final String SAVE_IS_BUDDY = "saveIsBuddy";
    private static final String SAVE_REVIEWS = "saveReview";
    private static final String SAVE_RECEIVED_REQUESTS = "saveReceivedRequests";

    //Fields to help handle queries and whatnot:
    private static float ratingToSave;
    private static boolean isBuddyValueToSave;
    private static Review reviewToSave;
    private static BuddyRequest requestToSave;
    private static Update updateInstance;


    //Purpose:          Determines whether or not there is already an Update instance dedicated to this user --> calls the appropriate helper method to parse, add, and save the appropriate class field
    public static void queryAndSave(String forUserId, String mode){
        //1.) Make Query --> find if there already exists and Update instance dedicated for this user:
        ParseQuery<Update> query = ParseQuery.getQuery(Update.class);
        query.whereEqualTo(Update.KEY_USER_NEED_UPDATE_ID, forUserId);

        query.findInBackground(new FindCallback<Update>() {
            @Override
            public void done(List<Update> objects, ParseException e) {
                if(e == null){
                    Log.d(TAG, "query to find userId is successful");

                    //2a.) There is not an Update object --> we have to create one!
                    if(objects.size() == 0 || objects == null){
                        Log.d(TAG, "no Update objects for userId = " + forUserId);
                        handleMode(new Update(forUserId), mode, true);      //isNew = is this a new instance?
                    }
                    else{
                        Log.d(TAG, "there was an Update object for userId="+ forUserId);

                        //2b.) There is 1 Update instance --> get it, get ratings List out of it, add this rating to it!
                        if(objects.size() == 1){
                            handleMode(objects.get(0), mode, false);
                        }
                        else{
                            Log.e(TAG, "There was more than 1 Update object for userId = " + forUserId);
                        }
                    }
                }
            }
        });
    }

    //Purpose:          called by queryAndSave() --> given the Update object to save data to + mode, save it in updateInstance filed, figure out what we are saving, then call the helper method needed to save it/
    //                  The data to be save is assumed to have been initialized in the appropriate class field. e.g. rating needs to be saved --> updateRatings() would have updated the "ratingToSave" field for this method to access
    public static void handleMode(Update correctUpdateObject, String mode, boolean isNew){
        updateInstance = correctUpdateObject;
        switch (mode) {
            case SAVE_RATING:
                saveRating(isNew);
                break;
            case SAVE_IS_BUDDY:
                saveIsBuddy();
                break;
            case SAVE_REVIEWS:
                saveReviews(isNew);
                break;
            case SAVE_RECEIVED_REQUESTS:
                saveReceivedRequests(isNew);
                break;
            default:
                Log.e(TAG, "handleMode(): mode didn't match any of the options: mode=" + mode);
                return;
        }
        try {
            updateInstance.save();
        } catch (ParseException e) {
            Log.e(TAG, "error saving rating=", e);
        }
    }

    //Purpose:      The method to be statically called --> sets the ratingToSave field, then calls queryAndSave() to save it to the correct Update object.
    public static void updateRatings(String userId, float rating){
        ratingToSave = rating;
        queryAndSave(userId, SAVE_RATING);
    }
    //Purpose:      The helper method to actually save the ratingToSave field to the updateInstance field (assumed to be initalized by handleMode()). Does NOT save updateInstance to Parse. That is done by handleMode()
    public static void saveRating(boolean isNew){
        List<Float> newRatingsList = new ArrayList<>();
        if(!isNew) {
            newRatingsList = updateInstance.getRatings();
            Log.d(TAG, "newRatingsList = " + newRatingsList.toString());

            newRatingsList.add(ratingToSave);
        }
        newRatingsList.add(ratingToSave);
        updateInstance.setNewRatings(newRatingsList);
    }

    public static void updateIsBuddy(String userId, boolean update){
        isBuddyValueToSave = update;
        queryAndSave(userId, SAVE_IS_BUDDY);
    }
    public static void saveIsBuddy(){
        updateInstance.setIsBuddy(isBuddyValueToSave);
    }

    public static void updateReviews(String userId, Review review){
        reviewToSave = review;
        queryAndSave(userId, SAVE_REVIEWS);
    }
    public static void saveReviews(boolean isNew){
        List<Review> newReviewsList = new ArrayList<>();
        if(!isNew) {
            newReviewsList = updateInstance.getReviews();
            Log.d(TAG, "newReviewsList = " + newReviewsList.toString() + "   reviewToSave = " + reviewToSave);
        }
        newReviewsList.add(reviewToSave);
        updateInstance.setNewReviews(newReviewsList);
    }

    public static void updateReceivedRequests(String userId, BuddyRequest request){
        requestToSave = request;
        queryAndSave(userId, SAVE_RECEIVED_REQUESTS);
    }
    public static void saveReceivedRequests(boolean isNew){
        List<BuddyRequest> buddyRequests = new ArrayList<>();
        if(!isNew) {
            buddyRequests = updateInstance.getReceivedRequests();
            Log.d(TAG, "buddyRequest = " + buddyRequests.toString() + "    requestToSave = " + requestToSave);
        }
        buddyRequests.add(requestToSave);
        updateInstance.setNewReceivedRequests(buddyRequests);
    }
}
