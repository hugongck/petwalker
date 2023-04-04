package com.example.petwalker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;

public class StepCountActivity extends AppCompatActivity{
    private TextView txtStepCountBox, txtWalkedStep, txtTimeCountBox, txtDistanceCountBox, txtTotalStep, txtSteps, txtTimeTask, txtDistanceTask;
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
                Intent intent = new Intent(StepCountActivity.this, Help.class);
                startActivity(intent);
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        txtStepCountBox = findViewById(R.id.txt_step_count_box);
        txtWalkedStep = findViewById(R.id.txt_walked_step);
        txtTimeCountBox = findViewById(R.id.txt_time_count_box);
        txtDistanceCountBox = findViewById(R.id.txt_distance_count_box);
        txtTotalStep = findViewById(R.id.txt_total_step);
        txtSteps = findViewById(R.id.txt_steps);
        txtTimeTask = findViewById(R.id.txt_time_task);
        txtDistanceTask = findViewById(R.id.txt_distance_task);

        stepDetector = new StepDetector();
        stepDetector.setOnStepListener(stepCount -> {
            // Update UI of step count box
            txtStepCountBox.setText(String.valueOf(stepCount));

            // Update UI of progress bar
            txtWalkedStep.setText(String.valueOf(stepCount));
            updateProgressBar(stepCount,getTaskStep(50));

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

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(stepDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(stepDetector, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(stepDetector);
    }

    private void updateProgressBar(int stepCount, int taskStep) {
        CircularProgressIndicator progressBar = findViewById(R.id.circular_progress_bar);
        int progressValue = (int) ((float)stepCount/taskStep*100); // Set the progress value here
        progressBar.setProgress(progressValue);
        txtTotalStep.setText("/"+String.valueOf(getTaskStep(50)));
        if (progressValue >= 100) {
            txtWalkedStep.setText("Completed");
            txtWalkedStep.setTextSize(40);
            txtTotalStep.setText("");
            txtSteps.setText("");
            progressBar.setIndicatorColor(Color.parseColor("#5CF6DB"));
        }
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
        stepCounter.stepLength = StepCounter.getStepLength(currentUser.getAge(), currentUser.getWeight(), currentUser.getGender());
        stepCounter.taskStep = StepCounter.getTaskStep(currentUser.getAge());
        stepCounter.taskTime = 150;
        stepCounter.taskDistance = StepCounter.getTaskDistance(currentUser.getAge());
        stepCounter.txtTotalStep.setText("/"+Integer.toString(stepCounter.taskStep));
        stepCounter.txtTimeTask.setText(Integer.toString(stepCounter.taskTime)+"min");
        stepCounter.txtDistanceTask.setText(Integer.toString(stepCounter.taskDistance)+"m");
    }

}
*/
