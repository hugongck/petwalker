package com.example.petwalker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class User {
    public String userid, name, email, birthday, gender;
    public int age;
    public double weight;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.petwalker.User.class)
    }

    public User(String userid, String name, String gender, String birthday, double weight) {
        this.userid = userid;
        this.name = name;
        this.email = name + "@petwalker.fyp";
        this.gender = gender;
        this.birthday = birthday;
        this.age = 0;
        this.weight = weight;
    }

    /*public int getAge() {
        // Parse the birthday string into a Date object
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date birthdayDate = null;
        try {
            birthdayDate = format.parse(birthday);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Calculate the age based on the current date and the user's birthday
        Calendar birthdayCalendar = Calendar.getInstance();
        birthdayCalendar.setTime(birthdayDate);
        Calendar currentCalendar = Calendar.getInstance();
        int age = currentCalendar.get(Calendar.YEAR) - birthdayCalendar.get(Calendar.YEAR);
        if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthdayCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }*/
}
