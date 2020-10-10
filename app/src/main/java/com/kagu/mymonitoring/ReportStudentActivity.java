package com.kagu.mymonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import com.kagu.mymonitoring.adapter.DailyReportAdapter;
import com.kagu.mymonitoring.entity.DailyReport;
import com.kagu.mymonitoring.student.AddDailyReportActivity;

import java.util.ArrayList;
import java.util.List;

public class ReportStudentActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private RecyclerView rvPostsStudent;
    private List<DailyReport> dailyReports;
    private DailyReportAdapter adapter;

    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_student);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Report");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        //rv and properties
        rvPostsStudent = findViewById(R.id.rvReportStudent);
        Intent intent=getIntent();
        myId = intent.getStringExtra("id");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //show newest post first
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set layout to rv
        rvPostsStudent.setLayoutManager(linearLayoutManager);

        dailyReports = new ArrayList<>();

        loadPosts();
    }

    private void loadPosts() {

        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("id").equalTo(myId);
        //get all data from node Posts
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DailyReport dailyReport = ds.getValue(DailyReport.class);

                    dailyReports.add(dailyReport);

                    //adapter
                    adapter = new DailyReportAdapter(ReportStudentActivity.this, dailyReports);
                    //set adapter to rv
                    rvPostsStudent.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //if error
                Toast.makeText(ReportStudentActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPost(final String querySearch) {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
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
                                dailyReport.getUsername().toLowerCase().contains(querySearch.toLowerCase())) {
                            dailyReports.add(dailyReport);
                        }
                    }
                    //adapter
                    adapter = new DailyReportAdapter(ReportStudentActivity.this, dailyReports);
                    //set adapter to rv
                    rvPostsStudent.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//if error
                Toast.makeText(ReportStudentActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //searchView
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.add_post).setVisible(false);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    searchPost(s);
                }
                if (TextUtils.isEmpty(s)) {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchPost(s);
                }
                if (TextUtils.isEmpty(s)) {
                    loadPosts();
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            firebaseAuth.signOut();
            Intent i = new Intent(ReportStudentActivity.this, Log_inActivity.class);
            startActivity(i);
        }
        if (id == R.id.add_post) {
            startActivity(new Intent(ReportStudentActivity.this, AddDailyReportActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
