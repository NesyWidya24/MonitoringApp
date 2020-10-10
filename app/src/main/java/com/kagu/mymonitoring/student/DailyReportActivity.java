package com.kagu.mymonitoring.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.adapter.DailyReportAdapter;
import com.kagu.mymonitoring.entity.DailyReport;
import com.kagu.mymonitoring.qa.ModuleLearnActivity;

import java.util.ArrayList;
import java.util.List;

public class DailyReportActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private RecyclerView rvPosts;
    private List<DailyReport> dailyReports;
    private DailyReportAdapter adapter;


    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        //rv and properties
        rvPosts = findViewById(R.id.rv_postsStudent);
        FloatingActionButton fabAddReport = findViewById(R.id.fab_addReport);
        firebaseAuth = FirebaseAuth.getInstance();
        dailyReports = new ArrayList<>();

        fabAddReport.setOnClickListener(view1 -> startActivity(new Intent(DailyReportActivity.this, AddDailyReportActivity.class)));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DailyReportActivity.this);
        //show newest post first
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvPosts.setLayoutManager(linearLayoutManager);


        EditText search_users = findViewById(R.id.search_report);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchPost(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        checkUserStat();
        loadPosts();
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(DailyReportActivity.this, ErrorActivity.class));
            finish();
        }
    }

    private void loadPosts() {
        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
        //query to load posts
        Query query = reference.orderByChild("id").equalTo(myId); //ga by project karena dalam 1 project biasanya terdiri beberapa student
        //get all data from node Posts
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    DailyReport dailyReport = ds.getValue(DailyReport.class);

                    dailyReports.add(dailyReport);
                }
                //adapter
                adapter = new DailyReportAdapter(DailyReportActivity.this, dailyReports);
                //set adapter to rv
                rvPosts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //if error
                Toast.makeText(DailyReportActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPost(final String querySearch) {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DailyReport");
        Query query = ref.orderByChild("id").equalTo(myId);
        //get all data from node Posts
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DailyReport dailyReport = ds.getValue(DailyReport.class);

                    if (dailyReport != null) {
                        if (dailyReport.getpProjectName().toLowerCase().contains(querySearch.toLowerCase()) ||
                                dailyReport.getpTitleScenario().toLowerCase().contains(querySearch.toLowerCase()) ||
                                dailyReport.getpResult().toLowerCase().contains(querySearch.toLowerCase())) {
                            dailyReports.add(dailyReport);
                        }
                    }
                    //adapter
                    adapter = new DailyReportAdapter(DailyReportActivity.this, dailyReports);
                    //set adapter to rv
                    rvPosts.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//if error
                Toast.makeText(DailyReportActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}