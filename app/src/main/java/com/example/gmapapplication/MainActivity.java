package com.example.gmapapplication;

import static com.example.gmapapplication.Calculation.distanceInKm;
import static com.example.gmapapplication.Calculation.getCurrentLatLng;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, Tracker.Callback {

    static final int PERMISSION_REQUEST = 1001;
    private final String CHANNEL_ID = "my_channel";
    private final int NOTIFICATION_ID = 1;
    private GoogleMap map;
    private Tracker tracker;
    private MapUiUtils uiUtils;
    private SearchView mapSearchView;
    private LinearLayout destinationsContainer;
    private Button controlButton;
    private TextView city1, city2, approximateDistanceText;
    private Dialog dialogBox;
    private Button okayBtn;
    private ImageView arrowImg;

    public double distance;

    private Address destinationAddress;
    private NotificationManager notificationManager;

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private boolean isAlerting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // UI referenciák
        mapSearchView = findViewById(R.id.searchViewID);
        destinationsContainer = findViewById(R.id.startDestLinear);
        city1 = findViewById(R.id.startingCity);
        city2 = findViewById(R.id.destinationCity);
        controlButton = findViewById(R.id.StartButon);
        approximateDistanceText = findViewById(R.id.approximateDistance);
        arrowImg = findViewById(R.id.arrowImg);

        destinationsContainer.setVisibility(LinearLayout.INVISIBLE);
        controlButton.setVisibility(Button.INVISIBLE);

        arrowImg.setVisibility(ImageView.INVISIBLE);

        // Hibadialog beállítása
        dialogBox = new Dialog(this);
        dialogBox.setContentView(R.layout.dialoge_box);
        dialogBox.getWindow().setBackgroundDrawable(getDrawable(R.drawable.search_bar_bg));
        dialogBox.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        okayBtn = dialogBox.findViewById(R.id.okayBtn);
        okayBtn.setOnClickListener(v -> dialogBox.dismiss());


        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();


        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);


        // Engedélyek kérése
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, PERMISSION_REQUEST);

        SupportMapFragment mapFrag =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapID);
        tracker = new Tracker(this, mapFrag);
        tracker.setCallback(this);

        // Engedélykérés után:
        tracker.startLocationFlow();

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String givenLocationString = mapSearchView.getQuery().toString();
                mapSearchView.setIconified(true);
                List<Address> addressesList = null;


                Geocoder geocoder = new Geocoder(MainActivity.this);

                try {
                    addressesList = geocoder.getFromLocationName(givenLocationString,1);
                } catch (Exception e) {
                    dialogBox.show();
                }


                try {
                    destinationAddress = addressesList.get(0);

                }catch (Exception e){
                    dialogBox.show();
                }


                if (destinationAddress != null) {

                    LatLng newLatlng = new LatLng(destinationAddress.getLatitude(),destinationAddress.getLongitude());

                    Calculation.destination = newLatlng;

                    uiUtils.updateMarkers(getCurrentLatLng(), newLatlng,
                            R.drawable.baseline_person_pin_24, R.drawable.baseline_place_24);


                    city2.setText(givenLocationString);
                    destinationsContainer.setVisibility(View.VISIBLE);
                    controlButton.setVisibility(View.VISIBLE);
                    arrowImg.setVisibility(ImageView.VISIBLE);

                    uiUtils.updateDistance(distanceInKm(newLatlng));


                }

                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        controlButton.setOnClickListener(v -> toggleTracking());
    }


    private void toggleTracking() {
        boolean isStarting = controlButton.getText().toString().equalsIgnoreCase("start");
        if (isStarting) {
            controlButton.setText(R.string.buttonStoptext);
            tracker.startContinuousUpdates(location -> {
                Calculation.currentLocation = new LatLng(location.getLatitude(),location.getLongitude());

                distance = uiUtils.update();

                showNotification("Location update active", "Remaining: ", distance);

                if (distance <= 10) {
                    startAlert();
                }
            });
        } else {
            controlButton.setText(R.string.buttonStarttext);
            tracker.stopUpdates();
            stopAlert();
        }
    }

    public void showNotification(String title, String description, double distance)
    {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = null;

        builder = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_place_24)
                .setContentTitle(title)
                .setContentText(description + String.valueOf(distance) + " km")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification = builder.build();

        notificationManager.notify(NOTIFICATION_ID,notification);

    }


    public void createNotificationChannel(){

        CharSequence channelName = "My Channel";
        String channelDescription = "My channel description";

        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,channelName,importance);
        channel.setDescription(channelDescription);

        notificationManager.createNotificationChannel(channel);
    }
    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERMISSION_REQUEST &&
                results.length > 0 &&
                results[0] == PackageManager.PERMISSION_GRANTED) {
            tracker.startLocationFlow();
        } else {
            Toast.makeText(this, "Helymeghatározási engedély megtagadva", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        LatLng here = getCurrentLatLng();
        uiUtils = new MapUiUtils(this, map, approximateDistanceText);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmapstyle));


        uiUtils.updateMarkers(here, null,
                R.drawable.baseline_person_pin_24, R.drawable.baseline_place_24);


        city1.setText(tracker.getCityName(here));
        destinationsContainer.setVisibility(LinearLayout.VISIBLE);
    }

    @Override
    public void onLocationReady() {
        tracker.initMap();

    }

    private void startAlert() {
        if (isAlerting) return;
        isAlerting = true;

        // Vibrate with a repeating pattern: wait 0ms, vibrate 500ms, pause 300ms, repeat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 500, 300}, 0));
        } else {
            // deprecated but works pre‑Oreo
            vibrator.vibrate(new long[]{0, 500, 300}, 0);
        }

        // Start your alarm tone
        mediaPlayer.start();
    }

    private void stopAlert() {
        if (!isAlerting) return;
        isAlerting = false;

        // Cancel vibration
        vibrator.cancel();

        // Stop and reset MediaPlayer
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            // re‑prepare so you can play again later
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
            mediaPlayer.setLooping(true);
        }
    }


}
