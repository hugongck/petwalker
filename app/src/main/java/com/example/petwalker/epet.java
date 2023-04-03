package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class epet extends AppCompatActivity implements SensorEventListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseManager fypDB = FirebaseManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef;

    private User currentUserData = new User();

    private TextView textViewStepCounter, textViewStepDetector;
    private SensorManager sensorManager;
    private Sensor mStepCounter;
    private boolean isCounterSensorPresent;
    int stepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epet);

        //Retrieve user data from database
        String uid = mAuth.getCurrentUser().getUid();
        currentUserRef = databaseRef.child("users").child(uid);
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the user data from the snapshot
                User currentUserData = dataSnapshot.getValue(User.class);

                // Use the retrieved values as needed
                //Greeting message
                TextView epetTalk = findViewById(R.id.epet_talk);
                String currentDate = Time.getCurrentDate();
                String currentTime = Time.getCurrentTime();
                String currentDateTime = Time.getCurrentDateTime();
                String currentTime_H = currentTime.substring(0, 2);
                if(Integer.parseInt(currentTime_H) >= 0 && Integer.parseInt(currentTime_H) < 12){
                    epetTalk.setText("Good Morning, "+currentUserData.getName());
                }else if(Integer.parseInt(currentTime_H) >= 12 && Integer.parseInt(currentTime_H) < 16){
                    epetTalk.setText("Good Afternoon, "+currentUserData.getName());
                }else if(Integer.parseInt(currentTime_H) >= 16 && Integer.parseInt(currentTime_H) < 24){
                    epetTalk.setText("Good Evening, "+currentUserData.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                String TAG = "TAG: ";
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(getApplicationContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
                Intent intent = new Intent(epet.this, Help.class);
                startActivity(intent);
            }
        });

        //step count
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){ //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        textViewStepCounter = findViewById(R.id.textView2);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null)
        {
            mStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isCounterSensorPresent = true;
        }
        else
        {
            textViewStepCounter.setText("-/2000");
            isCounterSensorPresent = false;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorevent) {
        if(sensorevent.sensor == mStepCounter)
        {
            stepCount = (int) sensorevent.values[0];
            textViewStepCounter.setText(String.valueOf(stepCount)+"/2000");
            //progress bar
            LinearProgressIndicator progressBar = findViewById(R.id.progress_bar);
            int progressValue = (int) ((float)stepCount/2000*100); // Set the progress value here
            progressBar.setProgress(progressValue);
            if (progressValue >= 100) {
                textViewStepCounter.setText("Completed");
                progressBar.setIndicatorColor(Color.parseColor("#5CF6DB"));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null)
            sensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null)
            sensorManager.unregisterListener(this, mStepCounter);
    }

}

