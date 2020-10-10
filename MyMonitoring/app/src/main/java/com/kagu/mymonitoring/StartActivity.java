package com.kagu.mymonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.admin.MainActivityAdmin;
import com.kagu.mymonitoring.onBoarding.OnBoardingActivity;
import com.kagu.mymonitoring.pic.MainActivityPic;
import com.kagu.mymonitoring.qa.MainActivityQa;
import com.kagu.mymonitoring.student.MainActivityStudent;

import java.util.Objects;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "Message";

    FirebaseAuth mAuth;
    FirebaseUser user;
    Handler handler;
    String myId;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = (FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference uidRef = reference.child("Users").child(uid);
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Quality Assurance")) {
                        startActivity(new Intent(StartActivity.this, MainActivityQa.class));
                        finish();
                    } else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Person In Charge")) {
                        startActivity(new Intent(StartActivity.this, MainActivityPic.class));
                        finish();
                    } else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Student")) {
                        startActivity(new Intent(StartActivity.this, MainActivityStudent.class));
                        finish();
                    }else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Admin")) {
                        startActivity(new Intent(StartActivity.this, MainActivityAdmin.class));
                        finish();
                    }else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Non Active Student")) {
                        startActivity(new Intent(StartActivity.this, ErrorActivity.class));
                        finish();
                    }else if (Objects.equals(dataSnapshot.child("type").getValue(String.class), "Non Active Staff")) {
                        startActivity(new Intent(StartActivity.this, ErrorActivity.class));
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
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (getSupportActionBar()!=null)
            getSupportActionBar().hide();

        Window w = getWindow();
        //mengatur tampilan awal menjadi fullscreen
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(StartActivity.this, OnBoardingActivity.class));
                StartActivity.this.finish();
            }
        }, 4000L);

    }
}
