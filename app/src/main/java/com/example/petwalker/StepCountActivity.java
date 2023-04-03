package com.example.petwalker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.HashMap;
import java.util.Locale;


public class StepCountActivity extends AppCompatActivity{
    private StepCounter stepCounter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseManager fypDB = FirebaseManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef, dailyDataRef;
    private User currentUserData = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        //Retrieve user data from database
        String uid = mAuth.getCurrentUser().getUid();
        currentUserRef = databaseRef.child("users").child(uid);
        dailyDataRef = databaseRef.child("daily_data").child(Time.getCurrentDate()).child(uid);

        stepCounter = new StepCounter(this);
        if (stepCounter.stepSensor == null) {
            Toast.makeText(this, "Step sensor not available on this device", Toast.LENGTH_SHORT).show();
        } else {
            stepCounter.sensorManager.registerListener(stepCounter, stepCounter.stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    // Get the user data from the snapshot
                    currentUserData = dataSnapshot.getValue(User.class);
                    // Do something with the currentUserData object
                    setStepCounterWithUserData(currentUserData);
                } else {
                    // Handle case where data does not exist
                    Log.e("TAG", "Data does not exist");
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

        dailyDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                // Loop through the children of the dataSnapshot to retrieve each DailyData object
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve the DailyData object as a HashMap
                    HashMap<String, Object> dailyDataMap = (HashMap<String, Object>) childSnapshot.getValue();

                    // Create a new DailyData object from the HashMap
                    DailyData dailyData = new DailyData(
                            (double) dailyDataMap.get("distanceWalked"),
                            (String) dailyDataMap.get("finishTime"),
                            ((Long) dailyDataMap.get("stepCount")).intValue(),
                            ((Long) dailyDataMap.get("taskDone")).intValue(),
                            (String) dailyDataMap.get("uid")
                    );

                    // Do something with the DailyData object
                    // ...
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                String TAG = "";
                Log.w(TAG, "Failed to read value.", error.toException());
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
                Intent intent = new Intent(StepCountActivity.this, Help.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(stepCounter);
    }

    private void setStepCounterWithUserData(User currentUserData){
        // Assign the retrieved values from currentUserRef as needed
        stepCounter.stepLength = StepCounter.getStepLength(currentUserData.getAge(), currentUserData.getWeight(), currentUserData.getGender());
        stepCounter.taskStep = StepCounter.getTaskStep(currentUserData.getAge());
        stepCounter.taskDistance = StepCounter.getTaskDistance(currentUserData.getAge());
        stepCounter.txtTotalStep.setText("/"+Integer.toString(stepCounter.taskStep));
        stepCounter.txtTimeTask.setText(Integer.toString(stepCounter.taskTime)+"min");
        stepCounter.txtDistanceTask.setText(Integer.toString(stepCounter.taskDistance)+"m");
    }

}