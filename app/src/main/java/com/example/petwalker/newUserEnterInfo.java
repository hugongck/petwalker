package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;

public class newUserEnterInfo extends AppCompatActivity {

    private Button btn_letsStart;
    private EditText userAgeInput, userWeightInput;
    private Spinner userGenderInput;

    private FirebaseManager fypDB = FirebaseManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference userRef = databaseRef.child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_enter_info);

        // Back button
        Button btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        userAgeInput = findViewById(R.id.editTextNumber_age);
        userGenderInput = findViewById(R.id.spinner_gender);
        userWeightInput = findViewById(R.id.editTextNumberDecimal_weight);

        Bundle userDataBundle = getIntent().getExtras();
        String currentUid = userDataBundle.getString("uid");

        //"LET'S START button listener
        btn_letsStart = findViewById(R.id.enter_btn);
        btn_letsStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //upload user's info to database
                int userAge = Integer.parseInt(userAgeInput.getText().toString());
                String userGender = userGenderInput.getSelectedItem().toString();
                double userWeight = Double.parseDouble(userWeightInput.getText().toString());

                //userRef.child(currentUid).child("age").setValue(userAge);
                //userRef.child(currentUid).child("gender").setValue(userGender);
                //userRef.child(currentUid).child("weight").setValue(userWeight);

                Intent intent = new Intent(newUserEnterInfo.this, Dashboard.class);
                startActivity(intent);
            }
        });
    }
}