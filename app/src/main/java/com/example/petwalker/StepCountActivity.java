package com.example.petwalker;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;


public class StepCountActivity extends AppCompatActivity{
    private StepCounter stepCounter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef, dailyDataRef;
    private User currentUser = new User();

    private DailyData userDailyData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        //Retrieve user data from database
        String uid = mAuth.getCurrentUser().getUid();
        currentUserRef = databaseRef.child("users").child(uid);
        dailyDataRef = databaseRef.child("daily_data").child(Time.getCurrentDate()).child(uid);

        //create stepCounter.java object
        stepCounter = new StepCounter(this, dailyDataRef);
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
                    currentUser = dataSnapshot.getValue(User.class);
                    // Do something with the currentUserData object
                    userDailyData = new DailyData(currentUser);
                    setStepCounterWithUserData(currentUser);
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
                DailyData dailyData = dataSnapshot.getValue(DailyData.class);
                if (dailyData != null) {
                    // Do something with the daily data
                    stepCounter.stepCount = dailyData.getStepCount();
                    stepCounter.totalDistance = dailyData.getDistanceWalked();
                    stepCounter.taskDone = dailyData.getTaskDone();
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

    private void setStepCounterWithUserData(User currentUser){
        // Assign the retrieved values from currentUserRef as needed
        stepCounter.stepLength = StepCounter.getStepLength(currentUser.getAge(), currentUser.getHeight(), currentUser.getGender());
        stepCounter.taskStep = StepCounter.getTaskStep(currentUser.getAge());
        stepCounter.taskDistance = StepCounter.getTaskDistance(currentUser.getAge());
        stepCounter.txtTotalStep.setText("/"+Integer.toString(stepCounter.taskStep));
        stepCounter.txtTimeTask.setText(Integer.toString(stepCounter.taskTime)+"min");
        stepCounter.txtDistanceTask.setText(Integer.toString(stepCounter.taskDistance)+"m");
    }

}