package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Dashboard extends AppCompatActivity {

    private Button btn_ePet, btn_stepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btn_ePet = findViewById(R.id.btn_1);
        btn_stepCount = findViewById(R.id.btn_2);

        btn_stepCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, StepCount.class);
                startActivity(intent);
            }
        });
    }
}