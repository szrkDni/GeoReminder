package com.example.gmapapplication;

import static com.example.gmapapplication.Calculation.currentLatLng;
import static com.example.gmapapplication.Calculation.halfWayPosition;
import static com.example.gmapapplication.Calculation.zoomLevel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapUiUtils {

    private final Context ctx;
    private final GoogleMap map;
    private final TextView distanceTextView;

    public MapUiUtils(Context ctx, GoogleMap map, TextView distanceTextView) {
        this.ctx = ctx;
        this.map = map;
        this.distanceTextView = distanceTextView;
    }

    public BitmapDescriptor makeIcon(int drawableId) {
        Drawable d = ContextCompat.getDrawable(ctx, drawableId);
        int w = d.getIntrinsicWidth() * 2;
        int h = d.getIntrinsicHeight() * 2;
        d.setBounds(0, 0, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        d.draw(new Canvas(bmp));
        return BitmapDescriptorFactory.fromBitmap(bmp);
    }

    public void updateMarkers(LatLng current, LatLng destination, int currentIconRes, int destIconRes) {
        map.clear();
        map.addMarker(new MarkerOptions()
                .position(current)
                .icon(makeIcon(currentIconRes))
        );
        if (destination != null) {
            map.addMarker(new MarkerOptions()
                    .position(destination)
                    .icon(makeIcon(destIconRes))
            );

            LatLng halfWayPos = halfWayPosition(currentLatLng(),destination);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(halfWayPos, (float) zoomLevel(destination)));

        }
        else{
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 12));
        }
    }

    public void updateDistance(double distanceKm) {
        String text = "Estimated distance: " + ((int) distanceKm) + " km";
        distanceTextView.setText(text);
    }
}
