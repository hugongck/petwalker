package com.example.petwalker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class StepCount extends AppCompatActivity implements SensorEventListener {

    private TextView textViewStepCounter, textViewStepDetector;
    private SensorManager sensorManager;
    private Sensor mStepCounter;
    private boolean isCounterSensorPresent;
    int stepCount = 0;

    private TextView stepTextView = findViewById(R.id.txt_step_count_box);
    private TextView timeTextView = findViewById(R.id.txt_time_count_box);;
    private TextView distanceTextView = findViewById(R.id.txt_distance_count_box);
    private TextView twentyminwalktextview = findViewById(R.id.txt_time_task);
    private TextView fivehundredmwalktextview = findViewById(R.id.txt_distance_task);
    private TextView tenthousandstepstextview = findViewById(R.id.txt_target_task);
    private float totalDistance;
    private long startTime;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

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
                Intent intent = new Intent(StepCount.this, Help.class);
                startActivity(intent);
            }
        });

        //step count
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) { //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        textViewStepCounter = findViewById(R.id.txt_walked_step);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            mStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isCounterSensorPresent = true;
        } else {
            textViewStepCounter.setText("-");
            isCounterSensorPresent = false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            sensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);

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
    public void onSensorChanged(SensorEvent event) {
        /*if(sensorevent.sensor == mStepCounter)
        {
            stepCount = (int) sensorevent.values[0];
            textViewStepCounter.setText(String.valueOf(stepCount));
            //progress bar
            CircularProgressIndicator progressBar = findViewById(R.id.circular_progress_bar);
            int progressValue = (int) ((float)stepCount/2000*100); // Set the progress value here
            progressBar.setProgress(progressValue);
            if (progressValue >= 100) {
                progressBar.setIndicatorColor(Color.parseColor("#5CF6DB"));
            }
        }*/
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            updateStepCount();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
    protected void onResume() {
        super.onResume();
        //if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null)
        //    sensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        /*super.onPause();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null)
            sensorManager.unregisterListener(this, mStepCounter);*/
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