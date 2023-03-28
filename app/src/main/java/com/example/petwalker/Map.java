package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
/*
import android.os.Handler;
import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.Calendar;
import java.util.Random;
*/

public class Map extends AppCompatActivity implements LocationListener {

    private MapView map;
    private LocationManager locationManager;
    private MyLocationNewOverlay locationOverlay;
    /*
    private Marker destinationMarker;
    private ImageView targetTaskImageView;
    private RelativeLayout mapLayout;
    private boolean taskCompleted;
    private Handler refreshTaskHandler;
    private Random random;
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize OpenStreetMap configuration
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Back button
        Button btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Help button
        Button btn_help = findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Map.this, Help.class);
                startActivity(intent);
            }
        });

        // Initialize MapView
        initializeMapView();

        // Request location permissions
        checkLocationPermissions();
        /*
        targetTaskImageView = findViewById(R.id.img_target_task);
        mapLayout = findViewById(R.id.map);
        refreshTaskHandler = new Handler();
        random = new Random();
         */
    }

    private void initializeMapView() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(17.0);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setupLocationManager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationManager();
            }
        }
    }

    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        map.getController().animateTo(startPoint);

        /*
        if (!taskCompleted) {
            checkIfDestinationReached(location);
        }
         */

        // Set a pin point
        GeoPoint endPoint = new GeoPoint(location.getLatitude() + 0.005, location.getLongitude() + 0.005);
        Marker destinationMarker = new Marker(map);
        destinationMarker.setPosition(endPoint);
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(destinationMarker);

        // Show route path
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(startPoint);
        geoPoints.add(endPoint);

        Polyline routeLine = new Polyline();   //add Polyline
        routeLine.setPoints(geoPoints);
        routeLine.setColor(getResources().getColor(R.color.black));
        routeLine.setWidth(10);
        map.getOverlays().add(routeLine);
        map.invalidate();
        locationManager.removeUpdates(this); // Stop receiving location updates after drawing the route
    }
    /*
    private void checkIfDestinationReached(Location location) {
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), destinationMarker.getPosition().getLatitude(), destinationMarker.getPosition().getLongitude(), results);
        float distanceInMeters = results[0];

        if (distanceInMeters < 50) { // Change this value based on the desired distance threshold
            taskCompleted = true;
            targetTaskImageView.setImageResource(R.drawable.task_done);
            mapLayout.setBackgroundResource(R.color.black);
        }
    }

    private void refreshTaskAtMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        if (now.after(midnight)) {
            midnight.add(Calendar.DAY_OF_YEAR, 1);
        }

        long initialDelay = midnight.getTimeInMillis() - now.getTimeInMillis();
        refreshTaskHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                resetTask();
                refreshTaskAtMidnight();
            }
        }, SystemClock.uptimeMillis() + initialDelay);
    }

    private void resetTask() {
        taskCompleted = false;
        targetTaskImageView.setImageResource(R.drawable.task_not_done);
        mapLayout.setBackgroundResource(R.drawable.color_box);
        setRandomDestination();
    }

    private void setRandomDestination() {
        double randomLatitude = random.nextDouble() * 0.01 - 0.005;
        double randomLongitude = random.nextDouble() * 0.01 - 0.005;
        GeoPoint destination = new GeoPoint(locationOverlay.getMyLocation().getLatitude() + randomLatitude, locationOverlay.getMyLocation().getLongitude() + randomLongitude);
        destinationMarker.setPosition(destination);
    }

    // Call this method in onCreate and onResume
    private void setupLocationManagerAndRefreshTask() {
        setupLocationManager();
        refreshTaskAtMidnight();
    }

     */

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
//        setupLocationManagerAndRefreshTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Configuration.getInstance().save(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
//        refreshTaskHandler.removeCallbacksAndMessages(null);
    }
}