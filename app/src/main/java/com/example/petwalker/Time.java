package com.example.petwalker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Time {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";

    public static String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return currentDate.format(formatter);
    }

    public static String getCurrentTime() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
        return currentTime.format(formatter);
    }

    public static String getCurrentDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT + " " + TIME_FORMAT);
        return currentDateTime.format(formatter);
    }

    public static boolean isNextDayMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime timeNow = now.toLocalTime();
        LocalDate dateTomorrow = now.toLocalDate().plusDays(1);
        LocalTime midnight = LocalTime.MIDNIGHT;

        return timeNow.compareTo(midnight) >= 0 && now.isBefore(LocalDateTime.of(dateTomorrow, midnight));
    }
}