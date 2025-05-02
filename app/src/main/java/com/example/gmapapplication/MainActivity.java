package com.example.gmapapplication;

import static com.example.gmapapplication.Calculation.zoomLevel;
import static com.example.gmapapplication.Calculation.currentLatLng;
import static com.example.gmapapplication.Calculation.distanceInKm;
import static com.example.gmapapplication.Calculation.halfWayPosition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, Tracker.Callback {

    static final int PERMISSION_REQUEST = 1001;
    private GoogleMap map;
    private Tracker tracker;
    private MapUiUtils uiUtils;
    private SearchView mapSearchView;
    private LinearLayout destinationsContainer;
    private Button controlButton;
    private TextView city1, city2, approximateDistanceText;
    private Dialog dialogBox;
    private Button okayBtn;

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;
    private Address destinationAddress;
    private LocationManager systemLocationManager;

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

        destinationsContainer.setVisibility(LinearLayout.INVISIBLE);
        controlButton.setVisibility(Button.INVISIBLE);

        // Hibadialog beállítása
        dialogBox = new Dialog(this);
        dialogBox.setContentView(R.layout.dialoge_box);
        dialogBox.getWindow().setBackgroundDrawable(getDrawable(R.drawable.search_bar_bg));
        dialogBox.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        okayBtn = dialogBox.findViewById(R.id.okayBtn);
        okayBtn.setOnClickListener(v -> dialogBox.dismiss());


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


                    uiUtils.updateMarkers(currentLatLng(), newLatlng,
                            R.drawable.baseline_person_pin_24, R.drawable.baseline_place_24);


                    city2.setText(givenLocationString);
                    destinationsContainer.setVisibility(View.VISIBLE);
                    controlButton.setVisibility(View.VISIBLE);

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
            tracker.startContinuousUpdates();
        } else {
            controlButton.setText(R.string.buttonStarttext);
            tracker.stopUpdates();
        }
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
        LatLng here = currentLatLng();
        uiUtils = new MapUiUtils(this, map, approximateDistanceText);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmapstyle));


        uiUtils.updateMarkers(here, null,
                R.drawable.baseline_person_pin_24, R.drawable.baseline_place_24);


        city1.setText(tracker.getCityName(here));
        destinationsContainer.setVisibility(LinearLayout.VISIBLE);
        controlButton.setVisibility(Button.VISIBLE);
    }

    @Override
    public void onLocationReady() {
        tracker.initMap();
    }

    private BitmapDescriptor icon(Context ctx, int drawableId) {
        Drawable d = ContextCompat.getDrawable(ctx, drawableId);
        int w = d.getIntrinsicWidth()*2, h = d.getIntrinsicHeight()*2;
        d.setBounds(0,0,w,h);
        Bitmap bmp = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        d.draw(new Canvas(bmp));
        return BitmapDescriptorFactory.fromBitmap(bmp);
    }

}
