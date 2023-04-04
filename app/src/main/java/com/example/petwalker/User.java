package com.example.petwalker;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class User {
    private String uid, name, email, gender;
    private int age;
    private double height;

    public static void getUser(String uid, UserLoadedCallback userLoadedCallback) {
    }

    public interface UserLoadedCallback {
        void onUserLoaded(User user);
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String name, String gender, int age, double height) {
        this.uid = uid;
        this.name = name;
        this.email = name + "@petwalker.fyp";
        this.gender = gender;
        this.age = age;
        this.height = height;
    }

    public User(String userid, User.UserLoadedCallback callback) {
        FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
        DatabaseReference userDataRef = fypDB.getDatabaseRef().child("users").child(userid);
        userDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    uid = dataSnapshot.child("uid").getValue(String.class);
                    name = dataSnapshot.child("name").getValue(String.class);
                    email = dataSnapshot.child("email").getValue(String.class);
                    gender = dataSnapshot.child("gender").getValue(String.class);
                    age = dataSnapshot.child("age").getValue(Integer.class);
                    height = dataSnapshot.child("height").getValue(Double.class);

                    if (callback != null) {
                        callback.onUserLoaded(User.this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.email = name + "@petwalker.fyp";
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", height=" + height +
                '}';
    }
}
