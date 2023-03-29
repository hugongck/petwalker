package com.example.petwalker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class User {
    public String uid, name, email, gender;
    public int age;
    public double weight;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.petwalker.User.class)
    }

    public User(String userid, String name, String gender, int age, double weight) {
        this.uid = userid;
        this.name = name;
        this.email = name + "@petwalker.fyp";
        this.gender = gender;
        this.age = age;
        this.weight = weight;
    }
}
