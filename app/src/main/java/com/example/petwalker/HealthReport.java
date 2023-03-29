package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Locale;

public class HealthReport extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_report);

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
                // Change background of button when it's pressed
                btn_monthly.setBackgroundResource(R.drawable.color_box);

                // Reset background of other buttons
                btn_daily.setBackgroundResource(R.drawable.box);
                btn_weekly.setBackgroundResource(R.drawable.box);
            }
        });


        BarChart barChart = findViewById(R.id.barChart);
        setupBarChart(barChart);
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