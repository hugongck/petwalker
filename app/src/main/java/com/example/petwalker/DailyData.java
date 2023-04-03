package com.example.petwalker;

public class DailyData {
    private double distanceWalked;
    private String finishTime;
    private int stepCount;
    private int taskDone;
    private String uid;

    public DailyData(User user){
        distanceWalked = 0.0;
        finishTime = "";
        stepCount = 0;
        taskDone = 0;
        uid = user.getUid();
    }

    public DailyData(double distanceWalked, String finishTime, int stepCount, int taskDone, String uid) {
        this.distanceWalked = distanceWalked;
        this.finishTime = finishTime;
        this.stepCount = stepCount;
        this.taskDone = taskDone;
        this.uid = uid;
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
}

