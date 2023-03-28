/*
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

import android.os.Handler;
import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.Calendar;
import java.util.Random;


public class Map extends AppCompatActivity implements LocationListener {

    private MapView map;
    private LocationManager locationManager;
    private MyLocationNewOverlay locationOverlay;

    private Marker destinationMarker;
    private ImageView targetTaskImageView;
    private RelativeLayout targetTaskBox;
    private boolean taskCompleted;
    private Handler refreshTaskHandler;
    private Random random;


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

        targetTaskImageView = findViewById(R.id.img_target_task);
        targetTaskBox = findViewById(R.id.constraintLayout_target_task);
        refreshTaskHandler = new Handler();
        random = new Random();

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


        if (!taskCompleted) {
            checkIfDestinationReached(location);
        }


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

    private void checkIfDestinationReached(Location location) {
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), destinationMarker.getPosition().getLatitude(), destinationMarker.getPosition().getLongitude(), results);
        float distanceInMeters = results[0];

        if (distanceInMeters < 50) { // Change this value based on the desired distance threshold
            taskCompleted = true;
            targetTaskImageView.setImageResource(R.drawable.task_done);
            targetTaskBox.setBackgroundResource(R.color.black);
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
            // Reset task completion status
            taskCompleted = false;

            // Update task UI elements
            targetTaskImageView.setImageResource(R.drawable.task_not_done);
            targetTaskBox.setBackgroundResource(R.drawable.color_box);
        }

        // Schedule next refresh for tomorrow
        refreshTaskHandler.postDelayed(this::refreshTaskAtMidnight, getMillisUntilMidnight());
    }

    private long getMillisUntilMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        if (now.after(midnight)) {
            midnight.add(Calendar.DAY_OF_MONTH, 1);
        }

        return midnight.getTimeInMillis() - now.getTimeInMillis();
    }

    private void resetTask() {
        taskCompleted = false;
        targetTaskImageView.setImageResource(R.drawable.task_not_done);
        targetTaskBox.setBackgroundResource(R.drawable.color_box);
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
        setupLocationManagerAndRefreshTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Configuration.getInstance().save(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
        refreshTaskHandler.removeCallbacksAndMessages(null);
    }
}
*/

package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import org.osmdroid.views.MapView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.widget.ImageView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Map extends AppCompatActivity {

    private MapView map;
    private LocationManager locationManager;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker targetMarker;
    private Polyline route;
    private ImageView imgTargetTask;
    private ConstraintLayout constraintLayoutTargetTask;

    private Location lastKnownLocation;
    private GeoPoint targetLocation;
    private boolean taskCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        // Initialize MyLocation Overlay
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);

        // Target task ImageView and ConstraintLayout
        imgTargetTask = findViewById(R.id.img_target_task);
        constraintLayoutTargetTask = findViewById(R.id.constraintLayout_target_task);

        // Generate target location and set up the task
        setupDailyTask();

        // Initialize and configure LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    private void setupDailyTask() {
        taskCompleted = false;
        targetLocation = generateRandomTargetLocation(lastKnownLocation);
        addMarkerAtTargetLocation(targetLocation);
        drawPathToTargetLocation(lastKnownLocation, targetLocation);
        updateTaskViews(false);
    }

    private GeoPoint generateRandomTargetLocation(Location currentLocation) {
        // Generate a random location within a certain radius (e.g., 500 meters)
        double radiusInMeters = 500.0;
        double x0 = currentLocation.getLatitude();
        double y0 = currentLocation.getLongitude();

        Random random = new Random();

        // Calculate random latitude and longitude within the radius
        double radiusInDegrees = radiusInMeters / 111320f;
        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        double newLat = x + x0;
        double newLon = y + y0;

        return new GeoPoint(newLat, newLon);
    }

    private void addMarkerAtTargetLocation(GeoPoint targetLocation) {
        if (targetMarker != null) {
            map.getOverlays().remove(targetMarker);
        }

        targetMarker = new Marker(map);
        targetMarker.setPosition(targetLocation);
        targetMarker.setIcon(getResources().getDrawable(R.drawable.logo));
        map.getOverlays().add(targetMarker);
    }

    private void drawPathToTargetLocation(Location currentLocation, GeoPoint targetLocation) {
        if (route != null) {
            map.getOverlays().remove(route);
        }

        // Add your logic to draw a path from the current location to the target location.
        // For simplicity, I am drawing a straight line here.
        route = new Polyline();
        route.addPoint(new GeoPoint(currentLocation));
        route.addPoint(targetLocation);
        map.getOverlays().add(route);
    }

    private void updateTaskViews(boolean completed) {
        if (completed) {
            imgTargetTask.setImageResource(R.drawable.task_done);
            constraintLayoutTargetTask.setBackground(new ColorDrawable(getResources().getColor(R.color.black)));
        } else {
            imgTargetTask.setImageResource(R.drawable.task_not_done);
            constraintLayoutTargetTask.setBackground(getResources().getDrawable(R.drawable.color_box));
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastKnownLocation = location;
            if (targetLocation != null) {
                float[] results = new float[1];
                Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                        targetLocation.getLatitude(), targetLocation.getLongitude(), results);

                if (results[0] < 50) { // Check if the user is within 50 meters of the target location
                    if (!taskCompleted) {
                        taskCompleted = true;
                        updateTaskViews(true);
                    }
                } else {
                    if (taskCompleted) {
                        taskCompleted = false;
                        updateTaskViews(false);
                    }
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        map.onPause();
    }

    private void resetTaskAtMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = (Calendar) now.clone();
        midnight.set(Calendar.HOUR_OF_DAY, 24);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        long timeUntilMidnight = midnight.getTimeInMillis() - now.getTimeInMillis();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupDailyTask();
                    }
                });
            }
        }, timeUntilMidnight, TimeUnit.DAYS.toMillis(1));
    }
}