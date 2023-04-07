package com.example.petwalker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

public class SensorGraph extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "SensorGraph";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private  Sensor sensors;
    public int stepCount1, stepCount2, stepCount3, stepCount4;
    private float[] lastAccelerometer2 = new float[3];
    private float[] lastAccelerometer3 = new float[3];
    private float[] lastAccelerometer4 = new float[3];
    private long lastStepTime1, lastStepTime2, lastStepTime3, lastStepTime4;
    // Low-pass filter smoothing factor ( lower >> smoother )
    private static final float ALPHA = 0.3f;
    // Step detection parameters
    private static final float STEP_THRESHOLD = 0.9f;
    private static final long STEP_TIME_THRESHOLD = 400;
    // Peak detection parameters
    private static final double PEAK_HEIGHT_THRESHOLD = 1.2; // higher means need larger movement
    private static final long PEAK_WIDTH_THRESHOLD = 250; // in milliseconds
    private static final long PEAK_INTERVAL_THRESHOLD = 450; // in milliseconds
    private long lastPeakTime = 0;
    private double lastLogMagnitude = -1;
    private long potentialPeakStartTime = -1;

    private LineChart mChart1, mChart2, mChart3, mChart4;
    private Thread thread;
    private boolean plotData = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_graph);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for(int i=0; i<sensors.size(); i++){
            Log.d(TAG, "onCreate: Sensor "+ i + ": " + sensors.get(i).toString());
        }

        if (mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        /**
         * Chart 1 - Raw data
         * */
        mChart1 = (LineChart) findViewById(R.id.chart1);

        // enable description text
        mChart1.getDescription().setEnabled(true);
        stepCount1 = 0;
        mChart1.getDescription().setText("Step Count: "+String.valueOf(stepCount1));
        mChart1.getDescription().setTextSize(22);

        // enable touch gestures
        mChart1.setTouchEnabled(true);

        // enable scaling and dragging
        mChart1.setDragEnabled(true);
        mChart1.setScaleEnabled(true);
        mChart1.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart1.setPinchZoom(true);

        // set an alternative background color
        mChart1.setBackgroundColor(Color.WHITE);

        LineData data1 = new LineData();
        data1.setValueTextColor(Color.WHITE);

        // add empty data
        mChart1.setData(data1);

        // get the legend (only possible after setting data)
        Legend l1 = mChart1.getLegend();

        // modify the legend ...
        l1.setForm(Legend.LegendForm.LINE);
        l1.setTextColor(Color.WHITE);

        XAxis xl1 = mChart1.getXAxis();
        xl1.setTextColor(Color.WHITE);
        xl1.setDrawGridLines(true);
        xl1.setAvoidFirstLastClipping(true);
        xl1.setEnabled(true);

        YAxis leftAxis1 = mChart1.getAxisLeft();
        leftAxis1.setTextColor(Color.WHITE);
        leftAxis1.setDrawGridLines(false);
        leftAxis1.setAxisMaximum(40f);
        leftAxis1.setAxisMinimum(-30f);
        leftAxis1.setDrawGridLines(true);

        YAxis rightAxis1 = mChart1.getAxisRight();
        rightAxis1.setEnabled(false);

        mChart1.getAxisLeft().setDrawGridLines(false);
        mChart1.getXAxis().setDrawGridLines(false);
        mChart1.setDrawBorders(false);

        feedMultiple();

        /**
         * Chart 2 - Apply low-pass filter
         * */
        mChart2 = (LineChart) findViewById(R.id.chart2);

        // enable description text
        mChart2.getDescription().setEnabled(true);
        stepCount2 = 0;
        mChart2.getDescription().setText("Step Count: "+String.valueOf(stepCount2));
        mChart2.getDescription().setTextSize(22);

        // enable touch gestures
        mChart2.setTouchEnabled(true);

        // enable scaling and dragging
        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);
        mChart2.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart2.setPinchZoom(true);

        // set an alternative background color
        mChart2.setBackgroundColor(Color.WHITE);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.WHITE);

        // add empty data
        mChart2.setData(data2);

        // get the legend (only possible after setting data)
        Legend l2 = mChart2.getLegend();

        // modify the legend ...
        l2.setForm(Legend.LegendForm.LINE);
        l2.setTextColor(Color.WHITE);

        XAxis xl2 = mChart2.getXAxis();
        xl2.setTextColor(Color.WHITE);
        xl2.setDrawGridLines(true);
        xl2.setAvoidFirstLastClipping(true);
        xl2.setEnabled(true);

        YAxis leftAxis2 = mChart2.getAxisLeft();
        leftAxis2.setTextColor(Color.WHITE);
        leftAxis2.setDrawGridLines(false);
        leftAxis2.setAxisMaximum(40f);
        leftAxis2.setAxisMinimum(-30f);
        leftAxis2.setDrawGridLines(true);

        YAxis rightAxis2 = mChart2.getAxisRight();
        rightAxis2.setEnabled(false);

        mChart2.getAxisLeft().setDrawGridLines(false);
        mChart2.getXAxis().setDrawGridLines(false);
        mChart2.setDrawBorders(false);

        feedMultiple();

        /**
         * Chart 3 - Apply low-pass filter + log
         * */
        mChart3 = (LineChart) findViewById(R.id.chart3);

        // enable description text
        mChart3.getDescription().setEnabled(true);
        stepCount3 = 0;
        mChart3.getDescription().setText("Step Count: "+String.valueOf(stepCount3));
        mChart3.getDescription().setTextSize(22);

        // enable touch gestures
        mChart3.setTouchEnabled(true);

        // enable scaling and dragging
        mChart3.setDragEnabled(true);
        mChart3.setScaleEnabled(true);
        mChart3.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart3.setPinchZoom(true);

        // set an alternative background color
        mChart3.setBackgroundColor(Color.WHITE);

        LineData data3 = new LineData();
        data3.setValueTextColor(Color.WHITE);

        // add empty data
        mChart3.setData(data3);

        // get the legend (only possible after setting data)
        Legend l3 = mChart3.getLegend();

        // modify the legend ...
        l3.setForm(Legend.LegendForm.LINE);
        l3.setTextColor(Color.WHITE);

        XAxis xl3 = mChart3.getXAxis();
        xl3.setTextColor(Color.WHITE);
        xl3.setDrawGridLines(true);
        xl3.setAvoidFirstLastClipping(true);
        xl3.setEnabled(true);

        YAxis leftAxis3 = mChart3.getAxisLeft();
        leftAxis3.setTextColor(Color.WHITE);
        leftAxis3.setDrawGridLines(false);
        leftAxis3.setAxisMaximum(40f);
        leftAxis3.setAxisMinimum(-30f);
        leftAxis3.setDrawGridLines(true);

        YAxis rightAxis3 = mChart3.getAxisRight();
        rightAxis3.setEnabled(false);

        mChart3.getAxisLeft().setDrawGridLines(false);
        mChart3.getXAxis().setDrawGridLines(false);
        mChart3.setDrawBorders(false);

        feedMultiple();


        /**
         * Chart 4 - Apply low-pass filter + log + peak detection
         * */
        mChart4 = (LineChart) findViewById(R.id.chart4);

        // enable description text
        mChart4.getDescription().setEnabled(true);
        stepCount4 = 0;
        mChart4.getDescription().setText("Step Count: "+String.valueOf(stepCount4));
        mChart4.getDescription().setTextSize(22);

        // enable touch gestures
        mChart4.setTouchEnabled(true);

        // enable scaling and dragging
        mChart4.setDragEnabled(true);
        mChart4.setScaleEnabled(true);
        mChart4.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart4.setPinchZoom(true);

        // set an alternative background color
        mChart4.setBackgroundColor(Color.WHITE);

        LineData data4 = new LineData();
        data4.setValueTextColor(Color.WHITE);

        // add empty data
        mChart4.setData(data4);

        // get the legend (only possible after setting data)
        Legend l4 = mChart4.getLegend();

        // modify the legend ...
        l4.setForm(Legend.LegendForm.LINE);
        l4.setTextColor(Color.WHITE);

        XAxis xl4 = mChart4.getXAxis();
        xl4.setTextColor(Color.WHITE);
        xl4.setDrawGridLines(true);
        xl4.setAvoidFirstLastClipping(true);
        xl4.setEnabled(true);

        YAxis leftAxis4 = mChart4.getAxisLeft();
        leftAxis4.setTextColor(Color.WHITE);
        leftAxis4.setDrawGridLines(false);
        leftAxis4.setAxisMaximum(40f);
        leftAxis4.setAxisMinimum(-40f);
        leftAxis4.setDrawGridLines(true);

        YAxis rightAxis4 = mChart4.getAxisRight();
        rightAxis4.setEnabled(false);

        mChart4.getAxisLeft().setDrawGridLines(false);
        mChart4.getXAxis().setDrawGridLines(false);
        mChart4.setDrawBorders(false);

        feedMultiple();



    }

    private void lowPassFilter(float[] input, float[] output) {
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
    }

    private void addEntry(SensorEvent event, int type) {

        if (type == 1) {

            LineData data1 = mChart1.getData();

            if (data1 != null) {

                ILineDataSet set1 = data1.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set1 == null) {
                    set1 = createSet(1);
                    data1.addDataSet(set1);
                }

                double magnitude1 = Math.sqrt(
                        event.values[0] * event.values[0] +
                                event.values[1] * event.values[1] +
                                event.values[2] * event.values[2]);

                long currentTime1 = System.currentTimeMillis();
                // Step Count
                if (magnitude1 > STEP_THRESHOLD && (currentTime1 - lastStepTime1) > STEP_TIME_THRESHOLD) {
                    stepCount1++;
                    mChart1.getDescription().setText("Step Count: "+String.valueOf(stepCount1));
                    lastStepTime1 = currentTime1;
                }

                data1.addEntry(new Entry(set1.getEntryCount(), event.values[0] + 5), 0);
                data1.notifyDataChanged();

                // let the chart know it's data has changed
                mChart1.notifyDataSetChanged();

                // limit the number of visible entries
                mChart1.setVisibleXRangeMaximum(150);
                // mChart1.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                mChart1.moveViewToX(data1.getEntryCount());
            }

        } else if (type == 2) {

            LineData data2 = mChart2.getData();

            if (data2 != null) {

                ILineDataSet set2 = data2.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set2 == null) {
                    set2 = createSet(2);
                    data2.addDataSet(set2);
                }

                lowPassFilter(event.values, lastAccelerometer2);
                double magnitude2 = Math.sqrt(
                        lastAccelerometer2[0] * lastAccelerometer2[0] +
                                lastAccelerometer2[1] * lastAccelerometer2[1] +
                                lastAccelerometer2[2] * lastAccelerometer2[2]);

                long currentTime2 = System.currentTimeMillis();
                // Step Count
                if (magnitude2 > STEP_THRESHOLD && (currentTime2 - lastStepTime2) > STEP_TIME_THRESHOLD) {
                    stepCount2++;
                    mChart2.getDescription().setText("Step Count: "+String.valueOf(stepCount2));
                    lastStepTime2 = currentTime2;
                }

                data2.addEntry(new Entry(set2.getEntryCount(), lastAccelerometer2[0] + 5), 0);
                data2.notifyDataChanged();

                // let the chart know it's data has changed
                mChart2.notifyDataSetChanged();

                // limit the number of visible entries
                mChart2.setVisibleXRangeMaximum(150);
                // mChart2.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                mChart2.moveViewToX(data2.getEntryCount());
            }
        } else if (type == 3) {

            LineData data3 = mChart3.getData();

            if (data3 != null) {

                ILineDataSet set3 = data3.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set3 == null) {
                    set3 = createSet(3);
                    data3.addDataSet(set3);
                }

                lowPassFilter(event.values, lastAccelerometer3);
                double magnitude3 = Math.sqrt(
                        lastAccelerometer3[0] * lastAccelerometer3[0] +
                                lastAccelerometer3[1] * lastAccelerometer3[1] +
                                lastAccelerometer3[2] * lastAccelerometer3[2]);
                double logMagnitude3 = Math.log10(magnitude3);

                long currentTime3 = System.currentTimeMillis();
                // Step Count
                if (logMagnitude3 > STEP_THRESHOLD && (currentTime3 - lastStepTime3) > STEP_TIME_THRESHOLD) {
                    stepCount3++;
                    mChart3.getDescription().setText("Step Count: "+String.valueOf(stepCount3));
                    lastStepTime3 = currentTime3;
                }

                data3.addEntry(new Entry(set3.getEntryCount(), lastAccelerometer3[0] + 5), 0);
                data3.notifyDataChanged();

                // let the chart know it's data has changed
                mChart3.notifyDataSetChanged();

                // limit the number of visible entries
                mChart3.setVisibleXRangeMaximum(150);
                // mChart3.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                mChart3.moveViewToX(data3.getEntryCount());
            }
        } else if (type == 4) {

            LineData data4 = mChart4.getData();

            if (data4 != null) {

                ILineDataSet set4 = data4.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set4 == null) {
                    set4 = createSet(4);
                    data4.addDataSet(set4);
                }

                lowPassFilter(event.values, lastAccelerometer4);
                double magnitude4 = Math.sqrt(
                        lastAccelerometer4[0] * lastAccelerometer4[0] +
                                lastAccelerometer4[1] * lastAccelerometer4[1] +
                                lastAccelerometer4[2] * lastAccelerometer4[2]);
                double logMagnitude4 = Math.log10(magnitude4);

                long currentTime4 = System.currentTimeMillis();
                // Peak detection
                boolean isPotentialPeak = false;
                if (logMagnitude4 > PEAK_HEIGHT_THRESHOLD) {
                    if (potentialPeakStartTime == -1) {
                        potentialPeakStartTime = currentTime4;
                    }
                    isPotentialPeak = true;
                } else {
                    if (potentialPeakStartTime != -1 && (currentTime4 - potentialPeakStartTime) > PEAK_WIDTH_THRESHOLD) {
                        isPotentialPeak = true;
                    }
                    potentialPeakStartTime = -1;
                }

                if (isPotentialPeak && (currentTime4 - lastPeakTime) > PEAK_INTERVAL_THRESHOLD) {
                    lastPeakTime = currentTime4;

                    if (lastLogMagnitude != -1 && logMagnitude4 > lastLogMagnitude) {
                        stepCount4++;
                        mChart4.getDescription().setText("Step Count: "+String.valueOf(stepCount4));
                    }
                }
                lastLogMagnitude = logMagnitude4;

                data4.addEntry(new Entry(set4.getEntryCount(), lastAccelerometer4[0] + 5), 0);
                data4.notifyDataChanged();

                // let the chart know it's data has changed
                mChart4.notifyDataSetChanged();

                // limit the number of visible entries
                mChart4.setVisibleXRangeMaximum(150);
                // mChart4.setVisibleYRange(40, AxisDependency.LEFT);

                // move to the latest entry
                mChart4.moveViewToX(data4.getEntryCount());
            }
        }
    }

    private LineDataSet createSet(int type) {
        String txt = "";
        if(type==1) {
            txt = "Raw data";
        }else if(type==2) {
            txt = "Low-pass filter";
        }else if(type==3) {
            txt = "Low-pass filter + Log";
        }else if(type==4) {
            txt = "Low-pass filter + Log + Peak detection";
        }
        LineDataSet set = new LineDataSet(null, txt);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.CYAN);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
        mSensorManager.unregisterListener(this);

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(plotData){
            addEntry(event, 1);
            addEntry(event, 2);
            addEntry(event, 3);
            addEntry(event, 4);
            plotData = false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(SensorGraph.this);
        thread.interrupt();
        super.onDestroy();
    }
}
