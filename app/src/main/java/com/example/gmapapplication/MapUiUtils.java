package com.example.gmapapplication;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
import static android.provider.Settings.System.NOTIFICATION_SOUND;
import static android.provider.Settings.System.getString;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.example.gmapapplication.Calculation.getCurrentLatLng;
import static com.example.gmapapplication.Calculation.halfWayPosition;
import static com.example.gmapapplication.Calculation.zoomLevel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompatExtras;
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

            LatLng halfWayPos = halfWayPosition(getCurrentLatLng(), destination);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(halfWayPos, (float) zoomLevel(destination)));

        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 12));
        }
    }

    public double update()
    {

        double distance = Calculation.distanceInKm(Calculation.destination);

        updateDistance(distance);

        updateMarkers(Calculation.currentLocation, Calculation.destination, R.drawable.baseline_person_pin_24, R.drawable.baseline_place_24);

        return distance;
    }

    public void updateDistance(double distanceKm) {
        String text = "Estimated distance: " + ((int) distanceKm) + " km";
        distanceTextView.setText(text);
    }
}
