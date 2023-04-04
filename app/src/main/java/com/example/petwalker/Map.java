package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

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

public class Map extends AppCompatActivity implements LocationListener {
    private TextView txtStepCountBox, txtTimeCountBox, txtDistanceCountBox, txtTimeTask, txtDistanceTask;
    private StepDetector stepDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private long startTime, elapsedTime;
    private float stepLength = 0.66f; // In meters, you can customize this value
    private int taskTime = 150;
    private float walkedDistance = 0f;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef;

    private User currentUserData = new User();

    private MapView map;
    private LocationManager locationManager;
    private MyLocationNewOverlay locationOverlay;

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

        // Step Count and update UI
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        txtStepCountBox = findViewById(R.id.txt_step_count_box);
        txtTimeCountBox = findViewById(R.id.txt_time_count_box);
        txtDistanceCountBox = findViewById(R.id.txt_distance_count_box);
        txtTimeTask = findViewById(R.id.txt_time_task);
        txtDistanceTask = findViewById(R.id.txt_distance_task);

        stepDetector = new StepDetector();
        stepDetector.setOnStepListener(stepCount -> {
            // Update UI of step count box
            txtStepCountBox.setText(String.valueOf(stepCount));

            // Update UI of distance count box
            stepLength = getStepLength(50,160,"Male");
            walkedDistance = stepCount * stepLength;
            txtDistanceCountBox.setText(String.format("%.1f", walkedDistance)+"m");

            // Update UI of time count box
            elapsedTime = System.currentTimeMillis() - startTime;
            txtTimeCountBox.setText(String.format("%d:%02d",
                    (int) (elapsedTime / 60000),
                    (int) (elapsedTime % 60000 / 1000)));

            // Update UI of Task
            initTaskCard(taskTime, getTaskDistance(50));
            changeTaskCard("time", checkTimeTask());
            changeTaskCard("distance", checkDistanceTask());
            changeTaskCard("target", checkTargetTask());
        });

    }

    private void initializeMapView() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(17);

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
        sensorManager.registerListener(stepDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(stepDetector, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Configuration.getInstance().save(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
        sensorManager.unregisterListener(stepDetector);
    }

    static float getStepLength(int userAge, double userHeight, String userGender){
        float stepLength = 0f;
        if (userGender.equals("Male")) {
            // Calculate step length for male based on age and height
            if (userAge >= 18 && userAge <= 49) {
                stepLength = (float) (userHeight * 0.415);
            } else if (userAge >= 50 && userAge <= 59) {
                stepLength = (float) (userHeight * 0.40);
            } else if (userAge >= 60 && userAge <= 69) {
                stepLength = (float) (userHeight * 0.385);
            } else if (userAge >= 70 && userAge <= 79) {
                stepLength = (float) (userHeight * 0.37);
            } else {
                stepLength = (float) (userHeight * 0.355);
            }
        } else if (userGender.equals("Female")) {
            // Calculate step length for female based on age and height
            if (userAge >= 18 && userAge <= 49) {
                stepLength = (float) (userHeight * 0.413);
            } else if (userAge >= 50 && userAge <= 59) {
                stepLength = (float) (userHeight * 0.395);
            } else if (userAge >= 60 && userAge <= 69) {
                stepLength = (float) (userHeight * 0.38);
            } else if (userAge >= 70 && userAge <= 79) {
                stepLength = (float) (userHeight * 0.365);
            } else {
                stepLength = (float) (userHeight * 0.35);
            }
        }
        return stepLength/2/100;

        /*
        Reference:

        Studenski, S., Perera, S., Patel, K., Rosano, C., Faulkner, K., Inzitari, M., Brach, J., Chandler, J., Cawthon, P., Connor, E. B., Nevitt, M., Visser, M., Kritchevsky, S., Badinelli, S., Harris, T., Newman, A. B., Cauley, J., Ferrucci, L., Guralnik, J., & Life Study Investigators. (2011). Age-related changes in gait and mobility: impact on intervention strategies. Geriatrics & gerontology international, 11(4), 292–302. https://doi.org/10.1111/j.1447-0594.2010.00607.x

        Shangguan, Y., Liang, Y., Zhou, Y., & Zhang, K. (2017). Age-related differences in step length variability during a continuous normal walking protocol. Journal of physical therapy science, 29(8), 1395–1399. https://doi.org/10.1589/jpts.29.1395

        Roig, M., Montesinos, L., Sanabria, D., Valldecabres, R., Ballester, R., Torner, A., & Benavent-Caballer, V. (2020). Walking in Old Age and Its Relationship with Physical and Cognitive Function. Frontiers in psychology, 11, 1451. https://doi.org/10.3389/fpsyg.2020.01451
        */

    }

    public static int getTaskDistance(int userAge) {
        int taskDistance = 0;
        if (userAge <= 49) {
            taskDistance = 8000;
        } else if (userAge >= 50 && userAge <= 59) {
            taskDistance = 6400;
        } else if (userAge >= 60 && userAge <= 69) {
            taskDistance = 5600;
        } else if (userAge >= 70 && userAge <= 79) {
            taskDistance = 4800;
        } else if (userAge >= 80) {
            taskDistance = 4000;
        }
        return taskDistance;

        /*
        Reference:

        Bassett, D. R. Jr., Wyatt, H. R., Thompson, H., Peters, J. C., & Hill, J. O. (2010). Pedometer-measured physical activity and health behaviors in United States adults. Medicine & Science in Sports & Exercise, 42(10), 1819-1825.

        Physical Activity Guidelines Advisory Committee. (2018). 2018 Physical activity guidelines advisory committee scientific report. US Department of Health and Human Services.

        Tudor-Locke, C., Craig, C. L., Brown, W. J., Clemes, S. A., De Cocker, K., Giles-Corti, B., ... & Blair, S. N. (2011). How many steps/day are enough? For adults. International Journal of Behavioral Nutrition and Physical Activity, 8(1), 79.

        World Health Organization. (2010). Global recommendations on physical activity for health. Geneva: World Health Organization.
        */

    }

    private void initTaskCard (int taskTime, int taskDistance) {
        changeTaskCard("time", checkTimeTask());
        changeTaskCard("distance", checkDistanceTask());
        changeTaskCard("target", checkTargetTask());
        txtTimeTask.setText(Integer.toString(taskTime)+"min");
        txtDistanceTask.setText(Integer.toString(taskDistance)+"m");
    }

    private void changeTaskCard(String task, Boolean isTaskDone) {

        ConstraintLayout constraintLayout = null;
        ImageView imageView = null;
        TextView textView = null;
        TextView textView2 = null;

        if (task.equals("time")) {
            // Time
            constraintLayout = findViewById(R.id.constraintLayout_time_task);
            imageView = findViewById(R.id.img_time_task);
            textView = findViewById(R.id.txt_time_task);
            textView2 = findViewById(R.id.txt_time_task2);
        } else if (task.equals("distance")) {
            // Distance
            constraintLayout = findViewById(R.id.constraintLayout_distance_task);
            imageView = findViewById(R.id.img_distance_task);
            textView = findViewById(R.id.txt_distance_task);
            textView2 = findViewById(R.id.txt_distance_task2);
        } else if (task.equals("target")) {
            // Target
            constraintLayout = findViewById(R.id.constraintLayout_target_task);
            imageView = findViewById(R.id.img_target_task);
            textView = findViewById(R.id.txt_target_task);
            textView2 = findViewById(R.id.txt_target_task2);
        }

        if (isTaskDone) {
            // Done task
            constraintLayout.setBackgroundColor(Color.parseColor("#000000"));
            imageView.setImageResource(R.drawable.task_done);
            textView.setTextColor(Color.parseColor("#5CF6DB"));
            textView2.setTextColor(Color.parseColor("#5CF6DB"));
        } else {
            // Not done task
            constraintLayout.setBackground(getResources().getDrawable(R.drawable.color_box));
            imageView.setImageResource(R.drawable.task_not_done);
            textView.setTextColor(Color.parseColor("#000000"));
            textView2.setTextColor(Color.parseColor("#000000"));
        }
    }

    private Boolean checkTimeTask() {
        if ((int) (elapsedTime / 60000) >= taskTime) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean checkDistanceTask() {
        if (walkedDistance >= getTaskDistance(50)) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean checkTargetTask() {
        return false;
    }
}

/*
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
 */