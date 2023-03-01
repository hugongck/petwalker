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
    private EditText userAgeInput;
    private Button startButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(msg, "onCreate() event");

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        userNameInput = findViewById(R.id.user_name_input);
        userAgeInput = findViewById(R.id.user_age_input);
        startButton = findViewById(R.id.start_button);
        loginButton = findViewById(R.id.login_button);

        // Adding onClickListener to start button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userNameInput.getText().toString();
                String age = userAgeInput.getText().toString();



                // Saving user input data in shared preference or database
                //saveUserData(name, age);

                // creating new Intent and starting next activity
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
            }
        });

        // Set click listener for the login button
        loginButton.setOnClickListener(view -> {
            String email = userNameInput.getText().toString().trim();
            String password = userAgeInput.getText().toString().trim();

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

        userAgeInput.addTextChangedListener(new TextWatcher() {
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
        String userAgeInputText = userAgeInput.getText().toString().trim();
        startButton.setEnabled(!userNameInputText.isEmpty() && !userAgeInputText.isEmpty());
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
