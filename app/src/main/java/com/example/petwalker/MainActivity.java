package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.text.Editable;
import android.widget.Button;
import android.text.TextWatcher;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private EditText userNameInput;
    private EditText userAgeInput;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameInput = findViewById(R.id.user_name_input);
        userAgeInput = findViewById(R.id.user_age_input);
        startButton = findViewById(R.id.start_button);

        // Adding onClickListener to start button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userNameInput.getText().toString();
                String age = userAgeInput.getText().toString();
                // Saving user input data in sharedpreference or database
                //saveUserData(name, age);

                // creating new Intent and starting next activity
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
            }
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
}
