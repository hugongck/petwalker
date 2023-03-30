package com.example.petwalker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private TextView stepTextView;
    private TextView timeTextView;
    private TextView distanceTextView;
    private TextView twentyminwalktextview;
    private TextView fivehundredmwalktextview;
    private TextView tenthousandstepstextview;
    private int stepCount;
    private float totalDistance;
    private long startTime;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the activity recognition permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        } else {
            // Permission is already granted, start tracking
            startTracking();
        }



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        stepTextView = findViewById(R.id.txt_step_count_box);
        timeTextView = findViewById(R.id.txt_time_count_box);
        distanceTextView = findViewById(R.id.txt_distance_count_box);
        twentyminwalktextview = findViewById(R.id.txt_time_task);
        fivehundredmwalktextview = findViewById(R.id.txt_distance_task);
        tenthousandstepstextview = findViewById(R.id.txt_target_task);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, start tracking
                startTracking();
            } else {
                // Permission is denied, show an error message
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void startTracking() {
        if (sensorManager != null) {
            // Register the step sensor listener
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

            // Start the timer
            startTime = SystemClock.elapsedRealtime();
            updateTime();

            // Reset the distance
            totalDistance = 0;
            updateDistance();

            // Check if the tasks have been completed
            checkTasks();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the step sensor listener
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            updateStepCount();
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void updateStepCount() {
        stepTextView.setText(stepCount);
        updateDistance();
        if (stepCount >= 10000) {
            tenthousandstepstextview.setText("10000 steps success");
        }
        checkTasks();
    }

    private void updateTime() {
        long elapsedTime = SystemClock.elapsedRealtime() - startTime;
        int hours = (int) (elapsedTime / 3600000);
        int minutes = (int) ((elapsedTime - hours * 3600000) / 60000);
        int seconds = (int) ((elapsedTime - hours * 3600000 - minutes * 60000) / 1000);
        timeTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        // Update the time every second
        timeTextView.postDelayed(this::updateTime, 1000);

        // Check if 20 minutes have passed
        if (minutes == 20) {
            twentyminwalktextview.setText("20 min walk success");
        }
    }

    private void updateDistance() {
        float stepLength = getStepLength();
        totalDistance = stepCount * stepLength ;
        distanceTextView.setText(String.format("%.2f m", totalDistance));
        if (totalDistance >= 500) {
            fivehundredmwalktextview.setText("500m walk success");
        }
    }

    private float getStepLength() {
        // The average step length for a person is 0.75 meters
        return 0.75f;
    }

    private void checkTasks() {
        /*if (stepCount >= 1000) {
            Toast.makeText(this, "Congratulations! You've completed 1000 steps!", Toast.LENGTH_SHORT).show();
        }

        if (totalDistance >= 5) {
            Toast.makeText(this, "Congratulations! You've walked 5 kilometers!", Toast.LENGTH_SHORT).show();
        }*/
    }
}