package com.example.petwalker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class HealthReport extends AppCompatActivity {

    public TextView txt_step, txt_distance, txt_energy, txt_duration, txt_speed;
    public TextView txt_step_count, txt_distance_count, txt_energy_count, txt_duration_count, txt_speed_count;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef;
    private String currentPage = "Daily";

    private User currentUserData = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_report);

        //Retrieve user data from database
        String uid = mAuth.getCurrentUser().getUid();
        currentUserRef = databaseRef.child("users").child(uid);
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the user data from the snapshot
                User currentUserData = dataSnapshot.getValue(User.class);

                // Use the retrieved values as needed
                // ...
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
                Intent intent = new Intent(HealthReport.this, Help.class);
                startActivity(intent);
            }
        });

        // button
        Button btn_daily = findViewById(R.id.btn_daily);
        Button btn_weekly = findViewById(R.id.btn_weekly);
        Button btn_monthly = findViewById(R.id.btn_monthly);

        btn_daily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = "Daily";
                // Change background of button when it's pressed
                btn_daily.setBackgroundResource(R.drawable.color_box);
                // Reset background of other buttons
                btn_weekly.setBackgroundResource(R.drawable.box);
                btn_monthly.setBackgroundResource(R.drawable.box);

                setText(currentPage);
                changeAlert(currentPage);
                BarChart barChart = findViewById(R.id.barChart);
                setupBarChart(barChart, currentPage);
            }
        });

        btn_weekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = "Weekly";
                // Change background of button when it's pressed
                btn_weekly.setBackgroundResource(R.drawable.color_box);

                // Reset background of other buttons
                btn_daily.setBackgroundResource(R.drawable.box);
                btn_monthly.setBackgroundResource(R.drawable.box);

                setText(currentPage);
                changeAlert(currentPage);
                BarChart barChart = findViewById(R.id.barChart);
                setupBarChart(barChart, currentPage);
            }
        });

        btn_monthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = "Monthly";
                // Change background of button when it's pressed
                btn_monthly.setBackgroundResource(R.drawable.color_box);

                // Reset background of other buttons
                btn_daily.setBackgroundResource(R.drawable.box);
                btn_weekly.setBackgroundResource(R.drawable.box);

                setText(currentPage);
                changeAlert(currentPage);
                BarChart barChart = findViewById(R.id.barChart);
                setupBarChart(barChart, currentPage);
            }
        });

        changeAlert(currentPage);

        setText(currentPage);

        BarChart barChart = findViewById(R.id.barChart);
        setupBarChart(barChart, currentPage);

    }

    private void changeAlert(String currentPage) {
        /*****************************************
         *  steps
         *  get from database
         *
         *  and check yesterday data = null?
         *****************************************/
        int thisStep = 0;
        int lastStep = 0;
        if (currentPage == "Daily") {
            thisStep = 2200;
            lastStep = 1550;
        } else if (currentPage == "Weekly") {
            thisStep = 1200*7;
            lastStep = 1550*7;
        } else if (currentPage == "Monthly") {
            thisStep = 1400*30;
            lastStep = 1550*30;
        }
        /******************************************/

        if ( lastStep == 0) {
            changeAlertTxt("hide", 0, currentPage);
        } else if (thisStep > lastStep) {
            changeAlertTxt("good", thisStep - lastStep, currentPage);
        } else if (thisStep < lastStep) {
            changeAlertTxt("bad", lastStep - thisStep, currentPage);
        }
    }

    private void changeAlertTxt(String alert, int number, String currentPage) {
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout_alert);
        ImageView imageView = findViewById(R.id.img_alert);
        TextView textView = findViewById(R.id.txt_alert);
        String alertTxt = "";
        if (currentPage.equals("Daily")) {
            alertTxt = "yesterday";
        } else if (currentPage.equals("Weekly")) {
            alertTxt = "last week";
        } else if (currentPage.equals("Monthly")) {
            alertTxt = "last month";
        }
        if (alert == "hide") {
            constraintLayout.setVisibility(View.GONE);
        } else if (alert == "bad") {
            // Bad Alert
            constraintLayout.setVisibility(View.VISIBLE);
            constraintLayout.setBackgroundColor(Color.parseColor("#000000"));
            imageView.setImageResource(R.drawable.color_alert);
            textView.setTextColor(Color.parseColor("#5CF6DB"));
            textView.setText("You walk " + number + " steps fewer than " + alertTxt + ".");
        } else if (alert == "good") {
            // Good Alert
            constraintLayout.setVisibility(View.VISIBLE);
            constraintLayout.setBackground(getResources().getDrawable(R.drawable.box));
            imageView.setImageResource(R.drawable.black_tick);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setText("You walk " + number + " steps more than " + alertTxt + ".");
        }
    }

    private void setupBarChart(BarChart barChart, String currentPage) {
        //ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout6);
        //constraintLayout.setVisibility(View.GONE);

        // Get the x-axis and y-axis objects from the chart
        XAxis xAxis = barChart.getXAxis();
        YAxis rightYAxis = barChart.getAxisRight();
        // Remove description text
        barChart.getDescription().setEnabled(false);
        // Move the x-axis label from top to bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // Remove the right y-axis label
        rightYAxis.setEnabled(false);

        // Disable the legend
        barChart.getLegend().setEnabled(false);

        // remove vertical grid lines
        barChart.getXAxis().setDrawGridLines(false);

        ArrayList<BarEntry> dataValues = new ArrayList<>();
        if (currentPage == "Daily") {
            /*****************************
             *  stepCount
             *  get from database
             *****************************/
            int[] stepCount = new int[24];
            for (int i = 0; i < 24; i++) {
                stepCount[i] = (i + 2) * 2;
            }
            /*****************************/

            for (int i = 0; i < 24; i++) {
                dataValues.add(new BarEntry(i, stepCount[i]));
            }
        } else if (currentPage == "Weekly") {
            /*****************************
             *  stepCount
             *  get from database
             *****************************/
            int[] stepCount = new int[7];
            for (int i = 0; i < 7; i++) {
                stepCount[i] = (i + 2) * 2;
            }
            /*****************************/

            for (int i = 0; i < 7; i++) {
                dataValues.add(new BarEntry(i, stepCount[i]));
            }
        } else if (currentPage == "Monthly") {
            /*****************************
             *  stepCount
             *  get from database
             *****************************/
            int[] stepCount = new int[30];
            for (int i = 0; i < 30; i++) {
                stepCount[i] = (i + 2) * 2;
            }
            /*****************************/

            for (int i = 0; i < 30; i++) {
                dataValues.add(new BarEntry(i, stepCount[i]));
            }
        }

        BarDataSet barDataSet = new BarDataSet(dataValues, "Steps");
        // Increase the size of the data value
        barDataSet.setValueTextSize(10f);
        barDataSet.setColor(ColorTemplate.rgb("#000000")); // Set histogram bar color to black
        // Reduce decimal point
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate(); // Refresh the chart
    }

    private void setText(String currentPage) {

        txt_step = findViewById(R.id.txt_step);
        txt_distance = findViewById(R.id.txt_distance);
        txt_energy = findViewById(R.id.txt_energy);
        txt_duration = findViewById(R.id.txt_duration);
        txt_speed = findViewById(R.id.txt_speed);
        txt_step_count = findViewById(R.id.txt_step_count);
        txt_distance_count = findViewById(R.id.txt_distance_count);
        txt_energy_count = findViewById(R.id.txt_energy_count);
        txt_duration_count = findViewById(R.id.txt_duration_count);
        txt_speed_count = findViewById(R.id.txt_speed_count);

        int numberOfDay = 0;
        if (currentPage.equals("Daily")) {
            numberOfDay = 1;
            txt_step.setText("Total Step");
            txt_distance.setText("Total Distance");
            txt_energy.setText("Total Burnt Energy");
            txt_duration.setText("Total Duration");
            txt_speed.setText("Avg. Walking Speed");
        } else if (currentPage.equals("Weekly")) {
            numberOfDay = 7;
            txt_step.setText("Avg. Step");
            txt_distance.setText("Avg. Distance");
            txt_energy.setText("Avg. Burnt Energy");
            txt_duration.setText("Avg. Duration");
            txt_speed.setText("Avg. Walking Speed");
        } if (currentPage.equals("Monthly")) {
            numberOfDay = 30;
            txt_step.setText("Avg. Step");
            txt_distance.setText("Avg. Distance");
            txt_energy.setText("Avg. Burnt Energy");
            txt_duration.setText("Avg. Duration");
            txt_speed.setText("Avg. Walking Speed");
        }

        /*****************************
         *  stepCount and duration
         *  get from database
         *****************************/
        int stepCount = 0;
        int duration = 0;
        if (currentPage.equals("Daily")) {
            stepCount = 3000 * numberOfDay; // total steps
            duration = 5500 * numberOfDay; // in second
        } else if (currentPage.equals("Weekly")) {
            stepCount = 9000 * numberOfDay; // total steps
            duration = 5000 * numberOfDay; // in second
        } if (currentPage.equals("Monthly")) {
            stepCount = 7500 * numberOfDay; // total steps
            duration = 6000 * numberOfDay; // in second
        }
        /*******************************************************/

        float distance = stepCount * getStepLength(50, 160, "Male");
        float energy = stepCount * 0.04f;
        float speed = distance / duration;

        txt_step_count.setText(Integer.toString(stepCount / numberOfDay));
        if (distance / numberOfDay >= 1000) {
            txt_distance_count.setText(String.format("%."+checkDecimal_0(distance / numberOfDay /1000, 1)+"f", distance / numberOfDay / 1000) + "km");
        } else {
            txt_distance_count.setText(String.format("%."+checkDecimal_0(distance / numberOfDay, 1)+"f", distance / numberOfDay) + "m");
        }
        if (energy / numberOfDay >= 1000) {
            txt_energy_count.setText(String.format("%."+checkDecimal_0(energy / numberOfDay /1000, 2)+"f", energy / numberOfDay / 1000) + "kcal");
        } else {
            txt_energy_count.setText(String.format("%."+checkDecimal_0(energy / numberOfDay, 1)+"f", energy / numberOfDay) + "cal");
        }
        if (duration / numberOfDay >= 3600) {
            txt_duration_count.setText(String.format("%.0f", duration / numberOfDay / 3600f)+"hr "+String.format("%.0f", duration / numberOfDay % 3600f / 60f)+"m");
        } else {
            txt_duration_count.setText(String.format("%.0f", duration / numberOfDay / 60f)+"min");
        }
        if (speed < 10) {
            txt_speed_count.setText(String.format("%.0f", speed * 10) + "cm/s");
        } else {
            txt_speed_count.setText(String.format("%.1f", speed) + "m/s");
        }

    }

    private static String checkDecimal_0(float number, int x) {
        String decimalString = String.format("%." + x + "f", number).split("\\.")[1];
        return decimalString.charAt(decimalString.length() - 1) == '0' ? Integer.toString(x-1) : Integer.toString(x);
    }

    public float getStepLength(int userAge, double userHeight, String userGender){
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
}