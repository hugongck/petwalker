package com.example.petwalker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DatabaseReference;

import java.text.BreakIterator;
import java.util.Locale;

public class StepCounter implements SensorEventListener {
    Activity currentActivity;
    private DatabaseReference dailyDataRef;

    int stepCount, previousStepCount, taskStep, taskDistance, taskTime, taskDone;
    double stepLength, totalDistance;
    long previousTime, totalTimeWalked;
    SensorEvent sensorEvent;
    SensorManager sensorManager;
    Sensor stepSensor;
    private TextView txtStepCountBox;
    private TextView txtWalkedStep;
    private TextView txtTimeCountBox;
    private TextView txtDistanceCountBox;
    TextView txtTimeTask;
    TextView txtDistanceTask;
    private TextView txtTargetTas;
    TextView txtTotalStep;

    public StepCounter(Activity activity, DatabaseReference dailyDataRef) {
        currentActivity = activity;
        this.dailyDataRef = dailyDataRef;
        sensorManager = (SensorManager) currentActivity.getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        txtTotalStep = currentActivity.findViewById(R.id.txt_total_step);
        txtTimeTask = currentActivity.findViewById(R.id.txt_time_task);
        txtDistanceTask = currentActivity.findViewById(R.id.txt_distance_task);
        txtStepCountBox = currentActivity.findViewById(R.id.txt_step_count_box);
        txtWalkedStep = currentActivity.findViewById(R.id.txt_walked_step);
        txtTimeCountBox = currentActivity.findViewById(R.id.txt_time_count_box);
        txtDistanceCountBox = currentActivity.findViewById(R.id.txt_distance_count_box);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // handle changes in sensor readings
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            // step count
            if (previousStepCount == 0) {
                // first time step count update
                previousStepCount = stepCount;
                previousTime = System.currentTimeMillis();
            }
            //read sensor value
            stepCount = (int) event.values[0];
            int steps = stepCount - previousStepCount;
            long currentTime = System.currentTimeMillis();
            updateUIBlocks();
            updateProgressBar();
            totalTimeWalked = currentTime - previousTime;
            totalDistance = steps * stepLength;
            previousStepCount = stepCount;
            previousTime = currentTime;

            // Update user's daily data to Realtime Database
            dailyDataRef.child("stepCount").setValue(stepCount);
            dailyDataRef.child("totalDistance").setValue(totalDistance);
            dailyDataRef.child("totalTimeWalked").setValue(totalTimeWalked);
            // Task
            //changeTaskCard("time", checkTimeTask());
            //changeTaskCard("distance", checkDistanceTask());
            //changeTaskCard("target", checkTargetTask());
        } else {
            txtStepCountBox.setText("-");
            txtWalkedStep.setText("-");
            txtTimeCountBox.setText("-");
            txtDistanceCountBox.setText("-");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // handle changes in sensor accuracy
    }

    // other methods and variables for the StepCount class
    private void updateProgressBar() {
        CircularProgressIndicator progressBar = currentActivity.findViewById(R.id.circular_progress_bar);
        int progressValue = (int) ((float)stepCount/taskStep*100); // Set the progress value here
        progressBar.setProgress(progressValue);
        if (progressValue >= 100) {
            txtWalkedStep.setText("Completed");
            progressBar.setIndicatorColor(Color.parseColor("#5CF6DB"));
        }
    }

    private void updateUIBlocks(){
        txtTimeCountBox.setText(getFormattedTime(totalTimeWalked));
        txtStepCountBox.setText(String.valueOf(stepCount));
        txtWalkedStep.setText(String.valueOf(stepCount));
        txtDistanceCountBox.setText(String.format(Locale.getDefault(), "%.2f m", totalDistance));
    }

    private String getFormattedTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeTaskCard(String task, Boolean isTaskDone) {

        ConstraintLayout constraintLayout = null;
        ImageView imageView = null;
        TextView textView = null;
        TextView textView2 = null;

        if (task.equals("time")) {
            // Time
            constraintLayout = currentActivity.findViewById(R.id.constraintLayout_time_task);
            imageView = currentActivity.findViewById(R.id.img_time_task);
            textView = currentActivity.findViewById(R.id.txt_time_task);
            textView2 = currentActivity.findViewById(R.id.txt_time_task2);
        } else if (task.equals("distance")) {
            // Distance
            constraintLayout = currentActivity.findViewById(R.id.constraintLayout_distance_task);
            imageView = currentActivity.findViewById(R.id.img_distance_task);
            textView = currentActivity.findViewById(R.id.txt_distance_task);
            textView2 = currentActivity.findViewById(R.id.txt_distance_task2);
        } else if (task.equals("target")) {
            // Target
            constraintLayout = currentActivity.findViewById(R.id.constraintLayout_target_task);
            imageView = currentActivity.findViewById(R.id.img_target_task);
            textView = currentActivity.findViewById(R.id.txt_target_task);
            textView2 = currentActivity.findViewById(R.id.txt_target_task2);
        }

        if (isTaskDone) {
            // Done task
            constraintLayout.setBackgroundColor(Color.parseColor("#000000"));
            imageView.setImageResource(R.drawable.task_done);
            textView.setTextColor(Color.parseColor("#5CF6DB"));
            textView2.setTextColor(Color.parseColor("#5CF6DB"));
        } else {
            // Not done task
            constraintLayout.setBackground(currentActivity.getResources().getDrawable(R.drawable.color_box));
            imageView.setImageResource(R.drawable.task_not_done);
            textView.setTextColor(Color.parseColor("#000000"));
            textView2.setTextColor(Color.parseColor("#000000"));
        }
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

}

