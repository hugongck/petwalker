package com.example.petwalker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {
    private static FirebaseManager instance = null;
    private DatabaseReference dBRef;

    private FirebaseManager() {
        dBRef = FirebaseDatabase.getInstance("https://fyp-2023-fad2a-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public DatabaseReference getDatabaseRef() {
        return dBRef;
    }
}
