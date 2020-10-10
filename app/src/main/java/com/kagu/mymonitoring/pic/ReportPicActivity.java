package com.kagu.mymonitoring.pic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.kagu.mymonitoring.pic.ReportPicActivity;
import com.kagu.mymonitoring.student.AddDailyReportActivity;

import java.util.ArrayList;
import java.util.List;

public class ReportPicActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private RecyclerView rvPosts;
    private List<DailyReport> dailyReports;
    private DailyReportAdapter adapter;

    String myId, project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_pic);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Report");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        //rv and properties
        rvPosts = findViewById(R.id.rvAllDailyReport);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //show newest post first
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set layout to rv
        rvPosts.setLayoutManager(linearLayoutManager);

        dailyReports = new ArrayList<>();

        EditText search_users = findViewById(R.id.search_report);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchPost(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        checkUserStat();
        loadPosts();
    }

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(ReportPicActivity.this, ErrorActivity.class));
            finish();
        }
    }

    private void loadPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query queryUser = ref.orderByChild("id").equalTo(myId);
        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    project = ""+dataSnapshot1.child("projectName").getValue();

                //path of all posts
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport").child("Traceable Matrix");
                Query query = reference.orderByChild("pProjectName").equalTo(project);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dailyReports.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            DailyReport dailyReport = ds.getValue(DailyReport.class);

                            dailyReports.add(dailyReport);

                            //adapter
                            adapter = new DailyReportAdapter(ReportPicActivity.this, dailyReports);
                            //set adapter to rv
                            rvPosts.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //if error
                        Toast.makeText(ReportPicActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchPost(final String querySearch) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query queryUser = ref.orderByChild("id").equalTo(myId);
        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    project = ""+dataSnapshot1.child("projectName").getValue();

                //path of all posts
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport").child("Traceable Matrix");
                Query query = reference.orderByChild("pProjectName").equalTo(project);
                query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DailyReport dailyReport = ds.getValue(DailyReport.class);
                    if (dailyReport != null) {
                        if (dailyReport.getpTitleScenario().toLowerCase().contains(querySearch.toLowerCase()) ||
                                dailyReport.getUsername().toLowerCase().contains(querySearch.toLowerCase())) {
                            dailyReports.add(dailyReport);
                        }
                    }
                    //adapter
                    adapter = new DailyReportAdapter(ReportPicActivity.this, dailyReports);
                    //set adapter to rv
                    rvPosts.setAdapter(adapter);
                }
            }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //if error
                        Toast.makeText(ReportPicActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}