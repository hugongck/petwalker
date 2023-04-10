package com.example.petwalker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDBManager {
    private static FirebaseDBManager instance;
    private DatabaseReference dBRef;

    FirebaseDBManager() {
        dBRef = FirebaseDatabase.getInstance("https://fyp-2023-fad2a-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    public static synchronized FirebaseDBManager getInstance() {
        if (instance == null) {
            instance = new FirebaseDBManager();
        }
        return instance;
    }

    public DatabaseReference getDatabaseRef() {
        return dBRef;
    }

    public void writeDailyData(String uid, DailyData dailyData) {
        DatabaseReference dailyDataRef = dBRef.child("daily_data").child(Time.getCurrentDate()).child(uid);
        dailyDataRef.setValue(dailyData);
    }

    public void readDailyData(String uid, ValueEventListener listener) {
        DatabaseReference dailyDataRef = dBRef.child("daily_data").child(Time.getCurrentDate()).child(uid);
        dailyDataRef.addValueEventListener(listener);
    }

    public void writeUserData(String uid, User userData){
        DatabaseReference userDataRef = dBRef.child("users").child(uid);
        userDataRef.setValue(userData);
    }

    public void readUserData(String uid, ValueEventListener listener){
        DatabaseReference userDataRef = dBRef.child("users").child(uid);
        userDataRef.addValueEventListener(listener);
    }
}
