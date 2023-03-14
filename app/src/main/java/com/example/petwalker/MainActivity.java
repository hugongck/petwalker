package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.EditText;
import android.text.Editable;
import android.widget.Button;
import android.text.TextWatcher;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    String msg = "FYP: ";

    private FirebaseAuth mAuth;
    private EditText userNameInput;
    private EditText userPwInput;
    private Button createButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(msg, "onCreate() event");

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        userNameInput = findViewById(R.id.user_name_input);
        userPwInput = findViewById(R.id.user_age_input);
        createButton = findViewById(R.id.start_button);
        loginButton = findViewById(R.id.login_button);

        // Adding onClickListener to start button
        createButton.setOnClickListener(view -> {
            String email = userNameInput.getText().toString().trim();
            String password = userPwInput.getText().toString().trim();

            // Call createUserWithEmailAndPassword() with the user's email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Do something with the signed-up user
                            Toast.makeText(MainActivity.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Sign up failed
                            Toast.makeText(MainActivity.this, "Account creation failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Set click listener for the login button
        loginButton.setOnClickListener(view -> {
            String email = userNameInput.getText().toString().trim();
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
                            Intent intent = new Intent(MainActivity.this, Dashboard.class);
                            startActivity(intent);
                        } else {
                            // Sign in failed
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        //private void saveUserData(String name, String age) {
            // code to save data in shared preference or database

        //}


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

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(msg, "onStart() event");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(msg, "onResume() event");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(msg, "onPause() event");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(msg, "onStop() event");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(msg, "onDestroy() event");
    }
}
