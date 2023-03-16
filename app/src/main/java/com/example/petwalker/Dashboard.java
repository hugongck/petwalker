package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Dashboard extends AppCompatActivity {

    private Button btn_ePet, btn_stepCount, btn_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        btn_ePet = findViewById(R.id.btn_1);
        btn_stepCount = findViewById(R.id.btn_2);
        btn_map = findViewById(R.id.btn_4);

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
    }
}