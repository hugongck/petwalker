package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class epet extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef;

    private User currentUserData = new User();

    private TextView textViewStepCounter, textViewStepDetector;
    private SensorManager sensorManager;
    private Sensor mStepCounter;
    private boolean isCounterSensorPresent;
    int stepCount = 0;

    private TextView txtStep, txtWalkedStep, txtTimeCountBox, txtDistanceCountBox, txtTotalStep, txtSteps, txtTimeTask, txtDistanceTask;
    private StepDetector stepDetector;
    private Sensor accelerometer, gyroscope;
    private float walkedDistance = 0f;

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

        // AR button
        ImageButton btn_AR = findViewById(R.id.ar_pet_btn);

        btn_AR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent unityIntent = new Intent(epet.this, UnityPlayerActivity.class);

                startActivity(unityIntent);
            }
        });


        //step count
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        txtStep = findViewById(R.id.textView2);

        stepDetector = new StepDetector();
        stepDetector.setOnStepListener(stepCount -> {

            // Update UI
            updateProgressBar(stepCount, getTaskStep(50));

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(stepDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(stepDetector, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(stepDetector);
    }

    public static int getTaskStep(int userAge) {
        int taskStep = 0;
        if (userAge <= 49) {
            taskStep = 10000;
        } else if (userAge >= 50 && userAge <= 59) {
            taskStep = 8000;
        } else if (userAge >= 60 && userAge <= 69) {
            taskStep = 7000;
        } else if (userAge >= 70 && userAge <= 79) {
            taskStep = 6000;
        } else if (userAge >= 80) {
            taskStep = 5000;
        }
        return taskStep;

        /*
        Reference:

        Tudor-Locke, C., Craig, C. L., Brown, W. J., Clemes, S. A., De Cocker, K., Giles-Corti, B., ... & Blair, S. N. (2011). How many steps/day are enough for adults?. International Journal of Behavioral Nutrition and Physical Activity, 8(1), 79. https://doi.org/10.1186/1479-5868-8-79

        Lee, I. M., Shiroma, E. J., & Lobelo, F. (2012). Pedometer-based physical activity interventions: a meta-analysis. American Journal of Preventive Medicine, 43(3), 340-349. https://doi.org/10.1016/j.amepre.2012.05.006

        Bassett Jr, D. R., Wyatt, H. R., Thompson, H., & Peters, J. C. (2010). Hill JO. Pedometer-measured physical activity and health behaviors in US adults. Medicine and science in sports and exercise, 42(10), 1819-1825. https://doi.org/10.1249/MSS.0b013e3181dc2e54
        */

    }

    private void updateProgressBar(int stepCount, int taskStep) {
        LinearProgressIndicator progressBar = findViewById(R.id.progress_bar);
        int progressValue = (int) ((float)stepCount/taskStep*100); // Set the progress value here
        progressBar.setProgress(progressValue);
        txtStep.setText(String.valueOf(stepCount)+"/"+getTaskStep(50));
        if (progressValue >= 100) {
            txtStep.setText("Completed");
            progressBar.setIndicatorColor(Color.parseColor("#5CF6DB"));
        }
    }

}

