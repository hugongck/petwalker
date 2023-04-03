package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Dashboard extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseManager fypDB = FirebaseManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference currentUserRef;

    private User currentUserData = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Retrieve user data from database
        String uid = mAuth.getCurrentUser().getUid();
        currentUserRef = databaseRef.child("users").child(uid);
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the user data from the snapshot
                User currentUserData = dataSnapshot.getValue(User.class);

                // Use the retrieved values as needed
                //Greeting message
                TextView epetTalk = findViewById(R.id.epet_talk);
                String currentDate = Time.getCurrentDate();
                String currentTime = Time.getCurrentTime();
                String currentDateTime = Time.getCurrentDateTime();
                String currentTime_H = currentTime.substring(0, 2);
                if(Integer.parseInt(currentTime_H) >= 0 && Integer.parseInt(currentTime_H) < 12){
                    epetTalk.setText("Good Morning, "+currentUserData.getName());
                }else if(Integer.parseInt(currentTime_H) >= 12 && Integer.parseInt(currentTime_H) < 16){
                    epetTalk.setText("Good Afternoon, "+currentUserData.getName());
                }else if(Integer.parseInt(currentTime_H) >= 16 && Integer.parseInt(currentTime_H) < 24){
                    epetTalk.setText("Good Evening, "+currentUserData.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                String TAG = "TAG: ";
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(getApplicationContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Help button
        Button btn_help = findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Help.class);
                startActivity(intent);
            }
        });

        // Main buttons
        Button btn_ePet = findViewById(R.id.btn_1);
        Button btn_stepCount = findViewById(R.id.btn_2);
        Button btn_map = findViewById(R.id.btn_3);
        Button btn_rewards = findViewById(R.id.btn_4);
        Button btn_healthreport = findViewById(R.id.btn_5);
        Button btn_setting = findViewById(R.id.btn_6);

        btn_stepCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, StepCount.class);
                startActivity(intent);
            }
        });
        btn_ePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, epet.class);
                startActivity(intent);
            }
        });
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Map.class);
                startActivity(intent);
            }
        });
        btn_rewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Rewards.class);
                startActivity(intent);
            }
        });
        btn_healthreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, HealthReport.class);
                startActivity(intent);
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Setting.class);
                startActivity(intent);
            }
        });



    }

    @Override
    public void onBackPressed() {
        // do nothing or show an error message
    }

}