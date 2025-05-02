package com.example.gmapapplication;

import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Calculation {

    protected static Location currentLocation;

    static double zoomLevel(LatLng destination){
        int distance = distanceInKm(destination) * 1000;

        int pixels = 256;

        //Formula I found Google uses to calculate the zoom level of the map, based on the distance between two points
        double k = (double)pixels * 156543.03392 * Math.cos(destination.latitude * Math.PI / 180);

        int zoom = (int)((Math.round(Math.log((70 * k) / (distance * 43)) / 0.6931471805599453)) - 1);

        if (zoom > 1 && zoom < 18) {
            return zoom;
        }else{
            return 18;
        }
    }

    static int distanceInKm(Address destinationAddress){
        float[] distances = new float[3];

        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), destinationAddress.getLatitude(), destinationAddress.getLongitude(), distances);

        Log.i("Remaining: ", String.valueOf((int)(distances[0]/1000*1.30)));

        return (int)(distances[0]/1000*1.30);
    }

    static int distanceInKm(LatLng destination)
    {
        float[] distances = new float[3];

        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), destination.latitude, destination.longitude, distances);

        Log.i("Remaining: ", String.valueOf((int)(distances[0]/1000*1.30)));

        return (int)(distances[0]/1000*1.30);
    }
    static LatLng halfWayPosition(LatLng position1, LatLng position2){

        return new LatLng(((position1.latitude + position2.latitude) / 2), ((position1.longitude + position2.longitude) / 2));

    }

    static LatLng currentLatLng(){
        return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }
}
