package com.example.wings.models.helpers;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

//Keeps track of all info needed from a route after making the network request for directions
public class WingsRoute {

    private LatLng startLocation;       //which is the current location as well just the LatLng
    private LatLng destination;
    private List<LatLng> mapCoordinates;            //after we get the route, stores all intermediate coordinates (LatLngs) needed to get from startLocation to destination (froom decoding the polyline)

    private PolylineOptions lineOptions;            //to hold style of polyline, may not actually need
    private long est;           //in seconds b/c the "duration value" field in route is already in sec
    private long distance;      //in meters, b/c the "distance value" field already in meters


    public WingsRoute(){}

    public LatLng getStartLocation() {
        return startLocation;
    }
    public LatLng getDestination() {
        return destination;
    }
    public List<LatLng> getMapCoordinates() {
        return mapCoordinates;
    }
    public PolylineOptions getLineOptions() {
        return lineOptions;
    }
    public long getEst() {
        return est;
    }
    public long getDistance() {
        return distance;
    }


    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }
    public void setDestination(LatLng destination) {
        this.destination = destination;
    }
    public void setMapCoordinates(List<LatLng> mapCoordinates) {
        this.mapCoordinates = mapCoordinates;
    }
    public void setLineOptions(PolylineOptions lineOptions) {
        this.lineOptions = lineOptions;
    }
    public void setEst(long est) {
        //Calculate a very lenient est:     est + 1/2*est

        this.est = est;
    }
    public void setDistance(long distance) {
        this.distance = distance;
    }

}
