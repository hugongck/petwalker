package com.example.petwalker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time {

    public static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
    }
}

