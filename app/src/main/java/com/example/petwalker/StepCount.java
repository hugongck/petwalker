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

import java.util.Locale;


public class StepCount extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;

    private int stepCount = 0;
    private int previousStepCount = 0;
    private long totalTimeWalked = 0;
    private long previousTime = 0;
    private float totalDistance = 0;
    private float stepLength = 0f;
    private int taskStep = 0;
    private int taskTime = 150;
    private int taskDistance = 0;

    private TextView txtStepCountBox, txtWalkedStep, txtTimeCountBox, txtDistanceCountBox, txtTimeTask, txtDistanceTask, txtTargetTas, txtTotalStep;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseManager fypDB = FirebaseManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef;
    private String currentPage = "Daily";

    private User currentUserData = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        //Retrieve user data from database
        String uid = mAuth.getCurrentUser().getUid();
        currentUserRef = databaseRef.child("users").child(uid);
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the user data from the snapshot
                User currentUserData = dataSnapshot.getValue(User.class);

                // Use the retrieved values as needed
                stepLength = getStepLength(currentUserData.getAge(), currentUserData.getWeight(), currentUserData.getGender());
                taskStep = getTaskStep(currentUserData.getAge());
                taskDistance = getTaskDistance(currentUserData.getAge());
                txtTotalStep = findViewById(R.id.txt_total_step);
                txtTotalStep.setText("/"+Integer.toString(taskStep));
                txtTimeTask = findViewById(R.id.txt_time_task);
                txtTimeTask.setText(Integer.toString(taskTime)+"min");
                txtDistanceTask = findViewById(R.id.txt_distance_task);
                txtDistanceTask.setText(Integer.toString(taskDistance)+"m");

                //step count
                txtStepCountBox = findViewById(R.id.txt_step_count_box);
                txtWalkedStep = findViewById(R.id.txt_walked_step);
                txtTimeCountBox = findViewById(R.id.txt_time_count_box);
                txtDistanceCountBox = findViewById(R.id.txt_distance_count_box);

                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
                    stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                    // step count
                    if (previousStepCount == 0) {
                        // first time step count update
                        previousStepCount = stepCount;
                        previousTime = System.currentTimeMillis();
                    }
                    int steps = stepCount - previousStepCount;
                    long currentTime = System.currentTimeMillis();
                    txtStepCountBox.setText(String.valueOf(stepCount));
                    txtWalkedStep.setText(String.valueOf(stepCount));
                    updateProgressBar();
                    totalTimeWalked += currentTime - previousTime;
                    txtTimeCountBox.setText(getFormattedTime(totalTimeWalked));
                    totalDistance += steps * stepLength;
                    txtDistanceCountBox.setText(String.format(Locale.getDefault(), "%.2f m", totalDistance));
                    previousStepCount = stepCount;
                    previousTime = currentTime;
                    // Task
                    changeTaskCard("time", checkTimeTask());
                    changeTaskCard("distance", checkDistanceTask());
                    changeTaskCard("target", checkTargetTask());
                } else {
                    txtStepCountBox.setText("-");
                    txtWalkedStep.setText("-");
                    txtTimeCountBox.setText("-");
                    txtDistanceCountBox.setText("-");

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
                Intent intent = new Intent(StepCount.this, Help.class);
                startActivity(intent);
            }
        });
    }

    private Boolean checkTimeTask() {
        if (totalTimeWalked >= taskTime) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean checkDistanceTask() {
        if (totalDistance >= taskDistance) {
            return true;
        } else {
            return false;
        }
    }


    private Boolean checkTargetTask() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepSensor != null) {
            sensorManager.unregisterListener(this, stepSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
            stepCount = (int) sensorEvent.values[0];
            if (previousStepCount == 0) {
                // first time step count update
                previousStepCount = stepCount;
                previousTime = System.currentTimeMillis();
            }
            int steps = stepCount - previousStepCount;
            long currentTime = System.currentTimeMillis();
            if (currentTime - previousTime >= 1000) {
                // update UI every 1 seconds
                txtStepCountBox.setText(String.valueOf(stepCount));
                txtWalkedStep.setText(String.valueOf(stepCount));
                updateProgressBar();
                totalTimeWalked += currentTime - previousTime;
                txtTimeCountBox.setText(getFormattedTime(totalTimeWalked));
                totalDistance += steps * stepLength;
                txtDistanceCountBox.setText(String.format(Locale.getDefault(), "%.2f m", totalDistance));
                previousStepCount = stepCount;
                previousTime = currentTime;
                // Task
                changeTaskCard("time", checkTimeTask());
                changeTaskCard("distance", checkDistanceTask());
                changeTaskCard("target", checkTargetTask());
            }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private String getFormattedTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    private float getStepLength(int userAge, double userHeight, String userGender){
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
        return stepLength;

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

    public int getTaskDistance(int userAge) {
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

    private void updateProgressBar() {
        CircularProgressIndicator progressBar = findViewById(R.id.circular_progress_bar);
        int progressValue = (int) ((float)stepCount/taskStep*100); // Set the progress value here
        progressBar.setProgress(progressValue);
        if (progressValue >= 100) {
            txtWalkedStep.setText("Completed");
            progressBar.setIndicatorColor(Color.parseColor("#5CF6DB"));
        }
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

}