package com.kagu.mymonitoring.qa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.Log_inActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.adapter.DailyReportAdapter;
import com.kagu.mymonitoring.entity.DailyReport;
import com.kagu.mymonitoring.pic.ReportProjectActivity;
import com.kagu.mymonitoring.student.AddDailyReportActivity;

import java.util.ArrayList;
import java.util.List;

public class AllDailyReportActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private RecyclerView rvPosts;
    private List<DailyReport> dailyReports;
    private DailyReportAdapter adapter;
    EditText search_report;

    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_daily_report);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();


        firebaseAuth = FirebaseAuth.getInstance();

        //rv and properties
        rvPosts = findViewById(R.id.rvAllDailyReport);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //show newest post first
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set layout to rv
        rvPosts.setLayoutManager(linearLayoutManager);
        search_report = findViewById(R.id.search_report);
        search_report.addTextChangedListener(new TextWatcher() {
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

        dailyReports = new ArrayList<>();

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
            startActivity(new Intent(AllDailyReportActivity.this, ErrorActivity.class));
            finish();
        }
    }

    private void loadPosts() {

        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DailyReport dailyReport = ds.getValue(DailyReport.class);

                    dailyReports.add(dailyReport);

                    //adapter
                    adapter = new DailyReportAdapter(AllDailyReportActivity.this, dailyReports);
                    //set adapter to rv
                    rvPosts.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //if error
                Toast.makeText(AllDailyReportActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPost(final String querySearch) {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DailyReport");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DailyReport dailyReport = ds.getValue(DailyReport.class);
                    if (dailyReport != null) {
                        if (dailyReport.getpProjectName().toLowerCase().contains(querySearch.toLowerCase()) ||
                                dailyReport.getpTitleScenario().toLowerCase().contains(querySearch.toLowerCase()) ||
                                dailyReport.getUsername().toLowerCase().contains(querySearch.toLowerCase())) {
                            dailyReports.add(dailyReport);
                        }
                    }
                    //adapter
                    adapter = new DailyReportAdapter(AllDailyReportActivity.this, dailyReports);
                    //set adapter to rv
                    rvPosts.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//if error
                Toast.makeText(AllDailyReportActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}