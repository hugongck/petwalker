package com.example.petwalker;

import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class User {
    private String uid, name, email, gender;
    private int age;
    private double height;

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

    public User(DataSnapshot dataSnapshot) {
        this.uid = dataSnapshot.child("uid").getValue(String.class);
        this.name = dataSnapshot.child("name").getValue(String.class);
        this.email = dataSnapshot.child("email").getValue(String.class);
        this.gender = dataSnapshot.child("gender").getValue(String.class);
        this.age = dataSnapshot.child("age").getValue(Integer.class);
        this.height = dataSnapshot.child("height").getValue(Double.class);
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

    public double getWeight() {
        return height;
    }

    public void setWeight(double height) {
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
