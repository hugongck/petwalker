package com.example.petwalker;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DailyData {
    private double distanceWalked;
    private double taskLatitude;
    private double taskLongitude;
    private boolean taskRefreshed;
    private String finishTime;
    private int stepCount;
    private int taskDone;
    private String uid;
    private FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
    private DatabaseReference dailyDataRef = fypDB.getDatabaseRef().child("daily_data");

    public static void getDailyData(String uid, String today, DataLoadedCallback dataLoadedCallback) {
    }

    public interface DataLoadedCallback {
        void onDataLoaded(DailyData data);
    }
    public DailyData(){
        distanceWalked = 0.0;
        finishTime = "";
        stepCount = 0;
        taskDone = 0;
        taskLatitude = 0.0;
        taskLongitude = 0.0;
        uid = "na";
    }

    public DailyData(double distanceWalked, String finishTime, int stepCount, int taskDone, double taskLatitude, double taskLongitude, String uid) {
        this.distanceWalked = distanceWalked;
        this.finishTime = finishTime;
        this.stepCount = stepCount;
        this.taskDone = taskDone;
        this.taskLatitude = taskLatitude;
        this.taskLongitude = taskLongitude;
        this.uid = uid;
    }


    public DailyData(String userid, String today, DataLoadedCallback callback) {
        DatabaseReference todayData = dailyDataRef.child(today).child(userid);
        todayData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    distanceWalked = dataSnapshot.child("distanceWalked").getValue(Double.class);
                    finishTime = dataSnapshot.child("finishTime").getValue(String.class);
                    stepCount = dataSnapshot.child("stepCount").getValue(Integer.class);
                    taskDone = dataSnapshot.child("taskDone").getValue(Integer.class);
                    uid = dataSnapshot.child("uid").getValue(String.class);
                    taskRefreshed = dataSnapshot.child("taskRefreshed").getValue(Boolean.class);
                    taskLatitude = dataSnapshot.child("taskLatitude").getValue(Double.class);
                    taskLongitude = dataSnapshot.child("taskLongitude").getValue(Double.class);

                    if (callback != null) {
                        callback.onDataLoaded(DailyData.this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public double getDistanceWalked() {
        return distanceWalked;
    }

    public void setDistanceWalked(double distanceWalked) {
        this.distanceWalked = distanceWalked;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getTaskDone() {
        return taskDone;
    }

    public void setTaskDone(int taskDone) {
        this.taskDone = taskDone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getTaskLatitude() {
        return taskLatitude;
    }

    public void setTaskLatitude(double taskLatitude) {
        this.taskLatitude = taskLatitude;
    }

    public double getTaskLongitude() {
        return taskLongitude;
    }

    public void setTaskLongitude(double taskLongitude) {
        this.taskLongitude = taskLongitude;
    }

    public boolean isTaskRefreshed() {
        return taskRefreshed;
    }

    public void setTaskRefreshed(boolean taskRefreshed) {
        this.taskRefreshed = taskRefreshed;
    }
}

