package com.example.petwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ProgressBar;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.os.Bundle;

public class StepCount extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Back button
        Button btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Help button
        Button btn_help = findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StepCount.this, Help.class);
                startActivity(intent);
            }
        });

        //progress bar
        ProgressBar progressBar = findViewById(R.id.circular_progress_bar);
        int progressValue = (int) ((float)1696/2000*100); // Set the progress value here
        progressBar.setProgress(progressValue);
    }
}