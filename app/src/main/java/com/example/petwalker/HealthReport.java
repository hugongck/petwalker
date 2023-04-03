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

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseManager fypDB = FirebaseManager.getInstance();
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
            }
        });

        changeAlert("good");

        BarChart barChart = findViewById(R.id.barChart);
        setupBarChart(barChart);
    }

    private void changeAlert(String alert) {
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout_alert);
        ImageView imageView = findViewById(R.id.img_alert);
        TextView textView = findViewById(R.id.txt_alert);
        if (alert == "bad"){
            // Bad Alert
            constraintLayout.setBackgroundColor(Color.parseColor("#000000"));
            imageView.setImageResource(R.drawable.color_alert);
            textView.setTextColor(Color.parseColor("#5CF6DB"));
            textView.setText("You walk "+" steps fewer than last month.");
        } else if (alert == "good"){
            // Good Alert
            constraintLayout.setBackground(getResources().getDrawable(R.drawable.box));
            imageView.setImageResource(R.drawable.black_tick);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setText("You walk "+" steps more than yesterday.");
        }
    }

    private void setupBarChart(BarChart barChart) {
        // Get the x-axis and y-axis objects from the chart
        XAxis xAxis = barChart.getXAxis();
        YAxis rightYAxis = barChart.getAxisRight();
        // Move the x-axis label from top to bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // Remove the right y-axis label
        rightYAxis.setEnabled(false);

        // Disable the legend
        barChart.getLegend().setEnabled(false);

        // remove vertical grid lines
        barChart.getXAxis().setDrawGridLines(false);

        ArrayList<BarEntry> dataValues = new ArrayList<>();
        // TODO: Replace the sample data with your own data
        dataValues.add(new BarEntry(0, 5));
        dataValues.add(new BarEntry(1, 10));
        dataValues.add(new BarEntry(2, 15));
        dataValues.add(new BarEntry(3, 7));
        dataValues.add(new BarEntry(4, 12));
        dataValues.add(new BarEntry(5, 5));
        dataValues.add(new BarEntry(6, 10));
        dataValues.add(new BarEntry(7, 15));
        dataValues.add(new BarEntry(8, 7));
        dataValues.add(new BarEntry(9, 12));
        dataValues.add(new BarEntry(10, 5));
        dataValues.add(new BarEntry(11, 10));
        dataValues.add(new BarEntry(12, 15));
        dataValues.add(new BarEntry(13, 7));
        dataValues.add(new BarEntry(14, 12));
        dataValues.add(new BarEntry(15, 5));
        dataValues.add(new BarEntry(16, 10));
        dataValues.add(new BarEntry(17, 15));
        dataValues.add(new BarEntry(18, 7));
        dataValues.add(new BarEntry(19, 12));
        dataValues.add(new BarEntry(20, 5));
        dataValues.add(new BarEntry(21, 10));
        dataValues.add(new BarEntry(22, 15));
        dataValues.add(new BarEntry(23, 7));
        dataValues.add(new BarEntry(24, 12));


        BarDataSet barDataSet = new BarDataSet(dataValues, "Health Data");
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

}