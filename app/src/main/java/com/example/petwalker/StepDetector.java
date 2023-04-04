package com.example.petwalker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepDetector implements SensorEventListener {

    private long lastStepTime;
    private float[] lastAccelerometer = new float[3];
    private float[] lastGyroscope = new float[3];
    private int stepCount;

    // Step detection parameters
    private static final float STEP_THRESHOLD = 0.9f;
    private static final long STEP_TIME_THRESHOLD = 400;
    private static final float ALPHA = 0.2f; // Low-pass filter smoothing factor

    // Peak detection parameters
    private static final double PEAK_HEIGHT_THRESHOLD = 1.05;
    private static final long PEAK_WIDTH_THRESHOLD = 500; // in milliseconds
    private static final long PEAK_INTERVAL_THRESHOLD = 800; // in milliseconds
    private long lastPeakTime = 0;
    private double lastLogMagnitude = -1;
    private long potentialPeakStartTime = -1;

    private OnStepListener onStepListener;

    public interface OnStepListener {
        void onStep(int stepCount);
    }

    public void setOnStepListener(OnStepListener listener) {
        this.onStepListener = listener;
    }

    private void lowPassFilter(float[] input, float[] output) {
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lowPassFilter(event.values, lastAccelerometer);
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            lowPassFilter(event.values, lastGyroscope);
        }

        // Calculate the magnitude of the acceleration vector
        double magnitude = Math.sqrt(
                lastAccelerometer[0] * lastAccelerometer[0] +
                        lastAccelerometer[1] * lastAccelerometer[1] +
                        lastAccelerometer[2] * lastAccelerometer[2]);

        // Calculate the log magnitude
        double logMagnitude = Math.log10(magnitude);

        long currentTime = System.currentTimeMillis();

/*
        // Step Count
        if (logMagnitude > STEP_THRESHOLD && (currentTime - lastStepTime) > STEP_TIME_THRESHOLD) {
            stepCount++;
            lastStepTime = currentTime;

            if (onStepListener != null) {
                onStepListener.onStep(stepCount);
            }
        }
*/

        // Peak detection
        boolean isPotentialPeak = false;
        if (logMagnitude > PEAK_HEIGHT_THRESHOLD) {
            if (potentialPeakStartTime == -1) {
                potentialPeakStartTime = currentTime;
            }
            isPotentialPeak = true;
        } else {
            if (potentialPeakStartTime != -1 && (currentTime - potentialPeakStartTime) > PEAK_WIDTH_THRESHOLD) {
                isPotentialPeak = true;
            }
            potentialPeakStartTime = -1;
        }

        if (isPotentialPeak && (currentTime - lastPeakTime) > PEAK_INTERVAL_THRESHOLD) {
            lastPeakTime = currentTime;

            if (lastLogMagnitude != -1 && logMagnitude > lastLogMagnitude) {
                stepCount++;
                if (onStepListener != null) {
                    onStepListener.onStep(stepCount);
                }
            }
        }

        lastLogMagnitude = logMagnitude;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
