package com.example.gmapapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.List;
import java.util.Locale;

public class Tracker {
    public interface Callback {
        void onLocationReady();
        void onMapReady(GoogleMap map);
    }

    private final Context ctx;
    private final SupportMapFragment mapFragment;
    private final FusedLocationProviderClient fusedClient;
    private final LocationManager locManager;
    private LocationCallback continuousCallback;

    private final CancellationTokenSource tokenSource = new CancellationTokenSource();
    private Callback callback;

    public Tracker(Context ctx, SupportMapFragment mapFragment) {
        this.ctx = ctx;
        this.mapFragment = mapFragment;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(ctx);
        this.locManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
    }

    public void setCallback(Callback cb) {
        this.callback = cb;
    }

    public void startLocationFlow() {
        // 1) Engedélyek?
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (MainActivity) ctx,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    MainActivity.PERMISSION_REQUEST
            );
            return;
        }
        // 2) GPS on?
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(ctx, "Kapcsold be a GPS-t", Toast.LENGTH_LONG).show();
            ctx.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        // 3) Lekérjük a pozíciót
        fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.getToken()
        ).addOnSuccessListener(location -> {
            if (location != null && callback != null) {
                Calculation.currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                callback.onLocationReady();
            } else {
                Toast.makeText(ctx, "Helyadat lekérés sikertelen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initMap() {
        mapFragment.getMapAsync(gMap -> {
            if (callback != null) callback.onMapReady(gMap);
        });
    }

    public void startContinuousUpdates(OnLocationUpdateListener listener) {
        // ... permission checks omitted
        LocationRequest req = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (MainActivity) ctx,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    MainActivity.PERMISSION_REQUEST
            );
            return;
        }

        // Create and store one LocationCallback
        continuousCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);
                Location loc = result.getLastLocation();
                if (listener != null) listener.onLocationUpdate(loc);
            }
        };

        fusedClient.requestLocationUpdates(req, continuousCallback, ctx.getMainLooper());
    }

    public interface OnLocationUpdateListener {
        void onLocationUpdate(Location loc);
    }


    public void stopUpdates() {
        // cancel the one‑time request
        tokenSource.cancel();

        // unregister the continuous callback
        if (continuousCallback != null) {
            fusedClient.removeLocationUpdates(continuousCallback);
            continuousCallback = null;
        }
    }


    public String getCityName(LatLng pos) {
        try {
            List<Address> list = new Geocoder(ctx, Locale.getDefault())
                    .getFromLocation(pos.latitude, pos.longitude, 1);
            if (list != null && !list.isEmpty()) {
                return list.get(0).getLocality();
            }
        } catch (Exception ignored){}
        return "undefined";
    }
}
