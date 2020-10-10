package com.kagu.mymonitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.kagu.mymonitoring.qa.MainActivityQa;

public class ErrorActivity extends AppCompatActivity {
    Button restart_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        restart_btn = findViewById(R.id.restart_btn);
        restart_btn.setOnClickListener(view -> {
            startActivity(new Intent(ErrorActivity.this, StartActivity.class));
            finish();
        });
    }
}
