package com.kagu.mymonitoring.qa;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kagu.mymonitoring.Log_inActivity;
import com.kagu.mymonitoring.R;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashMap;

public class MainActivityQa extends AppCompatActivity {
    String myId;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_qa);
        if (getSupportActionBar()!=null)
            getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();

        BottomNavigationView navView = findViewById(R.id.nav_view_qa);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_qa);
        NavigationUI.setupWithNavController(navView, navController);
        checkUserStat();
    }

    private void checkOnlineStat(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStat", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());

        checkOnlineStat(timestamp);
    }

    @Override
    protected void onResume() {
        checkUserStat();
        checkOnlineStat("online");
        super.onResume();
    }
    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        }
    }

    @Override
    protected void onStart() {
        checkUserStat();
        super.onStart();
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
