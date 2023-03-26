package com.example.petwalker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.petwalker.User;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    String msg = "FYP: ";

    private FirebaseAuth mAuth;
    private EditText userNameInput, userPwInput;
    private Button createButton, loginButton;

    private DatabaseReference mDatabase, userRef, nextUserIdRef;

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
        mDatabase = FirebaseDatabase.getInstance("https://fyp-2023-fad2a-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        userRef = mDatabase.child("users");
        nextUserIdRef = mDatabase.child("nextUserId");

        userNameInput = findViewById(R.id.user_name_input);
        userPwInput = findViewById(R.id.user_age_input);
        createButton = findViewById(R.id.start_button);
        loginButton = findViewById(R.id.login_button);


        //Create New User
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
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Do something with the signed-up user
                            Toast.makeText(MainActivity.this, "Account created successfully.", Toast.LENGTH_SHORT).show();

                            // Retrieve the current value of "nextUserID" from the database
                            nextUserIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long nextUserID = dataSnapshot.getValue(Long.class);
                                    // Use the current value of "nextUserID" as the ID for the new user
                                    User newUser = new User(Long.toString(nextUserID), username, "n/a", "n/a", 0.0);
                                    // Save the new user to the database
                                    userRef.child(Long.toString(nextUserID)).setValue(newUser);

                                    // Increment the value of "nextUserID" in the database by 1
                                    nextUserIdRef.setValue(nextUserID + 1);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle any errors
                                }
                            });

                            //jump to collect new user's info
                            Intent intent = new Intent(MainActivity.this, newUserEnterInfo.class);
                            startActivity(intent);
                        } else {
                            // Sign up failed
                            Toast.makeText(MainActivity.this, "Account creation failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        //Login Current User
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
