package com.example.petwalker;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.commons.lang3.BooleanUtils;

public class StepCountActivity extends AppCompatActivity{
    private TextView txtStepCountBox, txtWalkedStep, txtTimeCountBox, txtDistanceCountBox, txtTotalStep, txtSteps, txtTimeTask, txtDistanceTask;
    private StepDetector stepDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private long startTime, elapsedTime;
    private float stepLength = 0.66f; // In meters, you can customize this value
    int stepCount;
    private int taskTime = 150;
    private float walkedDistance = 0f;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDBManager fypDB = new FirebaseDBManager();

    private DailyData dailyData;
    private User currentUser;
    private int dataLoadedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        txtStepCountBox = findViewById(R.id.txt_step_count_box);
        txtWalkedStep = findViewById(R.id.txt_walked_step);
        txtTimeCountBox = findViewById(R.id.txt_time_count_box);
        txtDistanceCountBox = findViewById(R.id.txt_distance_count_box);
        txtTotalStep = findViewById(R.id.txt_total_step);
        txtSteps = findViewById(R.id.txt_steps);
        txtTimeTask = findViewById(R.id.txt_time_task);
        txtDistanceTask = findViewById(R.id.txt_distance_task);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepDetector = new StepDetector();

        if(sensorManager == null || stepDetector == null) {
            // Handle the case where the system is not able to obtain a reference to the SENSOR_SERVICE
            txtStepCountBox.setText(String.valueOf("-"));
            txtWalkedStep.setText(String.valueOf("-"));
            updateProgressBar(0,0);
            txtDistanceCountBox.setText(String.format("%.1f", "-"+"m"));
        } else {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

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

        // Get the current user
        currentUser = new User(mAuth.getUid(), new User.UserLoadedCallback() {
            @Override
            public void onUserLoaded(User user) {
                // Increment the dataLoadedCount
                dataLoadedCount++;
                // Get the daily data for the current day
                String today = Time.getCurrentDate();
                dailyData = new DailyData(user.getUid(), today, new DailyData.DataLoadedCallback() {
                    @Override
                    public void onDataLoaded(DailyData data) {
                        // Increment the dataLoadedCount
                        dataLoadedCount++;
                        String msg="dailyData Loaded:";
                        Log.d(msg, Double.toString(data.getTaskLatitude()));
                        Log.d(msg, Double.toString(data.getTaskLongitude()));
                        // Check if both data objects have been loaded
                        if (dataLoadedCount == 2) {
                            Log.d(msg, "Using Step Detector");
                            usingStepDetector(data, user);
                        }
                    }
                });
            }
        });
        sensorManager.registerListener(stepDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(stepDetector, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void usingStepDetector(DailyData data, User user){
        //initialize UI with daily data
        txtStepCountBox.setText(String.valueOf(data.getStepCount()));
        txtWalkedStep.setText(String.valueOf(data.getStepCount()));
        updateProgressBar(data.getStepCount(),getTaskStep(user.getAge()));
        txtDistanceCountBox.setText(String.format("%.1f", data.getDistanceWalked())+"m");

        stepCount = data.getStepCount();
        stepDetector.setOnStepListener(count -> {
            stepCount++;
            String msg = "";
            Log.d(msg, "Step Detected");
            // Update UI of step count box
            txtStepCountBox.setText(String.valueOf(stepCount));

            // Update UI of progress bar
            txtWalkedStep.setText(String.valueOf(stepCount));
            updateProgressBar(stepCount,getTaskStep(user.getAge()));

            // Update UI of distance count box
            stepLength = getStepLength(user.getAge(),user.getHeight(), user.getGender());
            walkedDistance = stepCount * stepLength;
            txtDistanceCountBox.setText(String.format("%.1f", walkedDistance)+"m");

            // Update UI of time count box
            elapsedTime = System.currentTimeMillis() - startTime;
            txtTimeCountBox.setText(String.format("%d:%02d",
                    (int) (elapsedTime / 60000),
                    (int) (elapsedTime % 60000 / 1000)));

            //Check Task Done
            data.setTaskDone(BooleanUtils.toInteger(checkTimeTask()) +BooleanUtils.toInteger(checkDistanceTask())+BooleanUtils.toInteger(checkTargetTask()));

            // Update daily data on Realtime Database
            data.setStepCount(stepCount);
            data.setDistanceWalked(walkedDistance);
            fypDB.getDatabaseRef().child("daily_data").child(Time.getCurrentDate()).child(mAuth.getUid()).setValue(data);


            // Update UI of Task
            initTaskCard(taskTime, getTaskDistance(user.getAge()));
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
        txtTimeTask.setText(taskTime +"min");
        txtDistanceTask.setText(taskDistance +"m");
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
            textView2.setText("walk");
        } else if (task.equals("distance")) {
            // Distance
            constraintLayout = findViewById(R.id.constraintLayout_distance_task);
            imageView = findViewById(R.id.img_distance_task);
            textView = findViewById(R.id.txt_distance_task);
            textView2 = findViewById(R.id.txt_distance_task2);
            textView2.setText("walk");
        } else if (task.equals("target")) {
            // Target
            constraintLayout = findViewById(R.id.constraintLayout_target_task);
            imageView = findViewById(R.id.img_target_task);
            textView = findViewById(R.id.txt_target_task);
            textView2 = findViewById(R.id.txt_target_task2);
            textView.setText("Target");
            textView2.setText("Area");
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
        if (walkedDistance >= getTaskDistance(currentUser.getAge())) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean checkTargetTask() {
        return false;
    }
}
