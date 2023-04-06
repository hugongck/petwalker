package com.example.petwalker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Map extends AppCompatActivity implements LocationListener {
    private TextView txtStepCountBox, txtTimeCountBox, txtDistanceCountBox, txtTimeTask, txtDistanceTask;
    private StepDetector stepDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private long startTime, elapsedTime;
    private float stepLength = 0.66f; // In meters, you can customize this value
    private int taskTime = 150;
    private float walkedDistance = 0f;
    private boolean targetTaskRefreshed = true;

    private MapView map;
    private LocationManager locationManager;
    private MyLocationNewOverlay locationOverlay;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ;
    private FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef;

    private User currentUserData = new User();

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

        // Initialize OpenStreetMap configuration
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
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
            stepLength = getStepLength(50, 160, "Male");
            walkedDistance = stepCount * stepLength;
            txtDistanceCountBox.setText(String.format("%.1f", walkedDistance) + "m");

            // Update UI of time count box
            elapsedTime = System.currentTimeMillis() - startTime;
            txtTimeCountBox.setText(String.format("%d:%02d",
                    (int) (elapsedTime / 60000),
                    (int) (elapsedTime % 60000 / 1000)));

            // Update UI of Task
            initTaskCard(taskTime, getTaskDistance(50));
        });

    }

    private void initializeMapView() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(18);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();

        // Change the location marker image
        Drawable currentLocationMarker = getResources().getDrawable(R.drawable.user_marker);
        Bitmap currentLocationBitmap = ((BitmapDrawable) currentLocationMarker).getBitmap();
        locationOverlay.setPersonIcon(Bitmap.createScaledBitmap(currentLocationBitmap, 100, 150, true));

        map.getOverlays().add(locationOverlay);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //      Consider calling
                //      ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    map.getController().animateTo(startPoint);

                    /**
                     * TODO: get from database
                     * */
                    double taskLat = 0.0; //change get from database
                    double taskLon = 0.0; //change get from database
                    targetTaskRefreshed = false; //change get from database
                    if (!targetTaskRefreshed) {
                        double latOffset = getRandomOffset(0.003);
                        double lonOffset = getRandomOffset(0.003);
                        taskLat = location.getLatitude() + latOffset; // update to database
                        taskLon = location.getLongitude() + lonOffset; // update to database
                        targetTaskRefreshed = true; // update to database
                        /**
                         *  TODO: update to database
                         *  */
                    }
                    /**
                     * TODO: change targetTaskRefreshed to false every day midnight in database
                     * */

                    GeoPoint endPoint = new GeoPoint(taskLat, taskLon);
                    Marker destinationMarker = new Marker(map);

                    /*
                    // Change the marker image
                    Drawable markerIcon = getResources().getDrawable(R.drawable.pin_marker);
                    Bitmap markerBitmap = ((BitmapDrawable) markerIcon).getBitmap();
                    destinationMarker.setIcon(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(markerBitmap, 100, 176, true)));
                    */
                    destinationMarker.setPosition(endPoint);
                    destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(destinationMarker);

                    List<GeoPoint> geoPoints = new ArrayList<>();
                    geoPoints.add(startPoint);
                    geoPoints.add(endPoint);

                    Polyline routeLine = new Polyline();
                    routeLine.setPoints(geoPoints);
                    routeLine.setColor(getResources().getColor(R.color.black));
                    routeLine.setWidth(10);
                    map.getOverlays().add(routeLine);
                    map.invalidate();

                    /**
                     * TODO: get from database
                     * */
                    boolean isTargetTaskDone = false;  //change get from database
                    if (!isTargetTaskDone) {
                        changeTaskCard("target", checkTargetTask(location, endPoint));
                    }


                }

                @Override
                public void onProviderDisabled(String provider) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            }, null);
        }
    }

    private double getRandomOffset(double offset) {
        // Generate a random number between -offset and +offset
        Random random = new Random();
        double randomOffset = random.nextDouble() * (2 * offset) - offset;

        // Exclude the range -offset/3 to +offset/3
        if (randomOffset > (-(offset / 3)) && randomOffset < (offset / 3)) {
            randomOffset = random.nextBoolean() ? (-(offset / 3)) : offset/3;
        }
        Toast.makeText(this, "offset: "+offset+" random: "+randomOffset, Toast.LENGTH_SHORT).show();
        return randomOffset;
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

        // Enable automatic centering of the map on the user's location
        locationOverlay.enableFollowLocation();

        /**
         * TODO: get from database
         * */
        boolean isTargetTaskDone = false;  //change get from database
        if (!isTargetTaskDone) {
            double taskLat = 0.0; //change get from database
            double taskLon = 0.0; //change get from database
            GeoPoint endPoint = new GeoPoint(taskLat, taskLon);
            changeTaskCard("target", checkTargetTask(location, endPoint));
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

        /**
         * TODO: get from database
         * */
        boolean isTargetTaskDone = false;  //change get from database
        changeTaskCard("target", isTargetTaskDone);

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

    private boolean checkTargetTask(Location currentLocation, GeoPoint targetPoint) {
        float[] results = new float[1];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                targetPoint.getLatitude(), targetPoint.getLongitude(), results);
        float distanceInMeters = results[0];
        return distanceInMeters <= 100; // within 100 meter
        /**
         * Update to database (isTargetTaskDone)
         * */
    }


}