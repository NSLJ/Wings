package com.example.wings.network;

import android.util.Log;

import com.example.wings.models.helpers.WingsRoute;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose:             In charge of parsing the JSONObject returned by the network call to Google Directions API. Finds all of the possible routes and saves them as a List of WingsRoute objects
 */
public class DataParser {
    private static final String TAG = "DataParser";

    //Returns all possible routes as WingsRoute objects, this currently works
   public List<WingsRoute> parse(JSONObject jsonObject) {
       Log.d("DataParser", "in parse()");
       //List<List<HashMap<String, String>>> routes = new ArrayList<>(); //list of multiple routes
       JSONArray allRoutes = null;

       List<WingsRoute> routes = new ArrayList<>();

       try {
           //1.) get the "routes" JSONArray in the JSONObject
           allRoutes = jsonObject.getJSONArray("routes");

           //2.) For all available routes --> create an instance of route, go to legs of the route to get the distance and est, then go to steps in the legs and get all LatLngs in each step
           for (int i = 0; i < allRoutes.length(); i++) {
               Log.d("DataParser", "parse(): allRoutes.length=" + allRoutes.length());
               WingsRoute newRoute = new WingsRoute();
               List<LatLng> allIntermediatePoints = new ArrayList<>();


               //2a.) Get current route + all legs inside it:
               JSONObject currentRoute = (JSONObject) allRoutes.get(i);
               JSONArray allLegs = currentRoute.getJSONArray("legs");


               //2b.) i dont think there's ever multiple legs in a route unless there's multiple stops in the route --> just assume there's only one leg object

                  // Log.d("DataParser", "parse(): allLegs.length=" + allLegs.length());

                   //3.) Get the current Leg and obtain the total duration and distance of this route!
                   JSONObject currentLeg = (JSONObject) allLegs.get(0);

                   //3a.) Get distance object's "value" field (= the distance in meters) + set into the WingsRoute instance!
                   JSONObject distanceObject = (JSONObject) currentLeg.get("distance");
                   long distance = distanceObject.getLong("value");
                   newRoute.setDistance(distance);

                   //3b.) Same for duration object!
                   JSONObject durationObject = (JSONObject) currentLeg.get("duration");
                   long est = durationObject.getLong("value");
                   newRoute.setEst(est);

                   Log.d(TAG, "parse(): obtained distance=" + distance + "      duration=" + est);


                   //3c.) Get all the steps for this leg!
                   JSONArray allSteps = currentLeg.getJSONArray("steps");


                   //4.) To obtain and decode the polyline in each step/instruction in the route, save it and set it into our WingsRoute object
                   for (int k = 0; k < allSteps.length(); k++) {

                       //4a.) Get the current step and get the polyline object's "points" field as a string:
                       JSONObject currentStep = (JSONObject) allSteps.get(k);
                       JSONObject polylineObject = (JSONObject) currentStep.get("polyline");
                       String polyline = (String) polylineObject.get("points");

                       //4b.) Decode the polyline + append to total list of points:
                       List<LatLng> allPointsInPolyline = decodePoly(polyline);
                       allIntermediatePoints.addAll(allPointsInPolyline);       //may or may not cause trouble, but I expect it to just append to what is already in it
                   }

                   //5.) save allIntermediatePoints into our WingsRoute object:
                   newRoute.setMapCoordinates(allIntermediatePoints);
                   //routes.add(path);       //To save all routes as a List filled with Lists of HashMaps (that hold all the LatLngs), each List of HashMaps = just 1 decode polyline

               //6. Get and set the PolylineOptions into the WingsRoute (I just do this so I can easily use these options in map.addPolyline(polylineOptions)):
               PolylineOptions entireLineOptions = new PolylineOptions();
               entireLineOptions.addAll(allIntermediatePoints);
               newRoute.setLineOptions(entireLineOptions);

               //7.) Add the now setup WingsRoute to the List of WingsRoute to be returned later
                routes.add(newRoute);
           }
       } catch (JSONException e) {
           e.printStackTrace();
       }

       //Return our list of WingsRoute
       Log.d(TAG, "parse() is done!");
       return routes;
   }

   //
        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<>();

            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }

