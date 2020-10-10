package com.kagu.mymonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.admin.MainActivityAdmin;
import com.kagu.mymonitoring.pic.MainActivityPic;
import com.kagu.mymonitoring.qa.MainActivityQa;
import com.kagu.mymonitoring.student.LearnStudentActivity;
import com.kagu.mymonitoring.student.MainActivityStudent;

import java.util.Objects;

public class Log_inActivity extends AppCompatActivity {
    private static final String TAG = "Message";
    EditText mEmail, mPassword;
    TextView mForgot;
    Button mLogin;

    private FirebaseAuth mAuth;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        //init
        mEmail = findViewById(R.id.EtEmail);
        mPassword = findViewById(R.id.EtPass);
        mLogin = findViewById(R.id.login_btn);
        mForgot = findViewById(R.id.forgotPass);

        mAuth = FirebaseAuth.getInstance();
        mLogin.setOnClickListener(view -> {
            String email = mEmail.getText().toString().trim();
            String pass = mPassword.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmail.setError("Invalid Email");
                mEmail.setFocusable(true);
            } else if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(Log_inActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, pass);
            }
        });

        mForgot.setOnClickListener(view -> showForgotPassDialog());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
    }

    private void showForgotPassDialog() {
        startActivity(new Intent(Log_inActivity.this, ForgotPassActivity.class));
        finish();
    }

    private void loginUser(String email, String pass) {
        progressDialog.setMessage("Logging In...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        String uid = (FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference uidRef = reference.child("Users").child(uid);
                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Quality Assurance")) {
                                    startActivity(new Intent(Log_inActivity.this, MainActivityQa.class));
                                    finish();
                                } else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Person In Charge")) {
                                    startActivity(new Intent(Log_inActivity.this, MainActivityPic.class));
                                    finish();
                                } else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Student")) {
                                    startActivity(new Intent(Log_inActivity.this, MainActivityStudent.class));
                                    finish();
                                } else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Admin")) {
                                    startActivity(new Intent(Log_inActivity.this, MainActivityAdmin.class));
                                    finish();
                                } else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Non Active Student")) {
                                    startActivity(new Intent(Log_inActivity.this, LearnStudentActivity.class));
                                    finish();
                                } else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Non Active Staff")) {
                                    Toast.makeText(Log_inActivity.this, "Mohon maaf akun anda sudah di non aktifkan.", Toast.LENGTH_SHORT).show();
                                    mAuth = FirebaseAuth.getInstance();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null)
                                        user.getUid();
                                    mAuth.signOut();
                                    Intent i = new Intent(Log_inActivity.this, Log_inActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, databaseError.getMessage());
                            }
                        };
                        uidRef.addListenerForSingleValueEvent(valueEventListener);
                    } else {
                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user.
                        Toast.makeText(Log_inActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(Log_inActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakah anda benar-benar ingin keluar?")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialogInterface, i) -> {
                    onPause();
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }).setNegativeButton("Tidak", (dialogInterface, i) -> {
        }).show();
    }
}
