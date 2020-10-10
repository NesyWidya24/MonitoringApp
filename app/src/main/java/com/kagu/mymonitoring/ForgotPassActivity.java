package com.kagu.mymonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {

    private EditText resetEmail;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();

        resetEmail = findViewById(R.id.emailReset);
        Button resetPass = findViewById(R.id.btnSend);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(ForgotPassActivity.this, Log_inActivity.class));
        });

        resetPass.setOnClickListener(view -> {
                String userMail = resetEmail.getText().toString();

            if (TextUtils.isEmpty(userMail)) {
                Toast.makeText(ForgotPassActivity.this, R.string.notifForgotPass, Toast.LENGTH_SHORT).show();
            } else {
                auth.sendPasswordResetEmail(userMail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassActivity.this, "Please check your email account", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(ForgotPassActivity.this, Log_inActivity.class));
                        } else {
                            Toast.makeText(ForgotPassActivity.this, "Email not found, please try again.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ForgotPassActivity.this, Log_inActivity.class));
    }
}

