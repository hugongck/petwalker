package com.example.petwalker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText userNameInput, userPwInput;
    private Button createButton, loginButton;

    private FirebaseDBManager fypDB = FirebaseDBManager.getInstance();
    private DatabaseReference databaseRef = fypDB.getDatabaseRef();
    private DatabaseReference userRef = databaseRef.child("users");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Step Detector Graph
        ImageView logo_img = findViewById(R.id.logo_img);
        logo_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SensorGraph.class);
                startActivity(intent);
            }
        });

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Assign ViewIds to var
        userNameInput = findViewById(R.id.user_name_input);
        userPwInput = findViewById(R.id.user_age_input);
        createButton = findViewById(R.id.start_button);
        loginButton = findViewById(R.id.login_button);

        // Bundle for the next activity
        Bundle userData = new Bundle();

        // Create New User
        // Adding onClickListener to start button
        createButton.setOnClickListener(view -> {
            String username = userNameInput.getText().toString().trim();
            String email = username + "@petwalker.fyp";
            String password = userPwInput.getText().toString().trim();

            // Call createUserWithEmailAndPassword() with the user's email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser userAuth = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Account created successfully.", Toast.LENGTH_SHORT).show();

                            // Use the current value of "nextUserID" as the ID for the new user
                            User newUser = new User(userAuth.getUid(), username, "none", 0, 0.0);
                            // Save the new user to the database
                            userRef.child(userAuth.getUid()).setValue(newUser);
                            // Save current user's id to bundle
                            userData.putString("uid", userAuth.getUid());

                            //jump to collect new user's info
                            Intent intent = new Intent(MainActivity.this, newUserEnterInfo.class);
                            intent.putExtras(userData);
                            startActivity(intent);
                        } else {
                            // Sign up failed
                            Toast.makeText(MainActivity.this, "Account creation failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Login Current User
        // Set click listener for the login button
        loginButton.setOnClickListener(view -> {
            String email = userNameInput.getText().toString().trim() + "@petwalker.fyp";
            String password = userPwInput.getText().toString().trim();

            // Call signInWithEmailAndPassword() with the user's email and password
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Do something with the signed-in user
                            Toast.makeText(MainActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();

                            // creating new Intent and starting next activity
                            Intent toDashboard = new Intent(MainActivity.this, Dashboard.class);
                            startActivity(toDashboard);
                        } else {
                            // Sign in failed
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        userNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                checkInputs();
            }
        });

        userPwInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                checkInputs();
            }
        });
    }

    private void checkInputs() {
        String userNameInputText = userNameInput.getText().toString().trim();
        String userAgeInputText = userPwInput.getText().toString().trim();
        createButton.setEnabled(!userNameInputText.isEmpty() && !userAgeInputText.isEmpty());
        loginButton.setEnabled(!userNameInputText.isEmpty() && !userAgeInputText.isEmpty());
    }
}
