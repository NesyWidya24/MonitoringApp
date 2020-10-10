package com.kagu.mymonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.adapter.ListProjectAdapter;
import com.kagu.mymonitoring.entity.Project;

import java.util.ArrayList;
import java.util.List;

public class MyProjectActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ListProjectAdapter listProjectAdapter;
    private List<Project> projects;
    EditText search_project;
    FloatingActionButton fab_addUsers;
    TextView textInfo, titleForm;

    private FirebaseAuth firebaseAuth;
    String myId,type;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_project);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.rv_projects);
        fab_addUsers = findViewById(R.id.fab_addUsers);

        ImageButton btnBack = findViewById(R.id.btnBack);
        search_project = findViewById(R.id.search_project);
        textInfo = findViewById(R.id.textInfo);
        titleForm = findViewById(R.id.titleForm);
        titleForm.setText(R.string.detailProject);
        textInfo.setVisibility(View.VISIBLE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //get data
        Query query1 = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(currentUser.getUid());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    type = "" + ds.child("type").getValue();

                    if (type.equals("Quality Assurance")||type.equals("Person In Charge"))
                        textInfo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        textInfo.setText(R.string.textInfo);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MyProjectActivity.this));

        projects = new ArrayList<>();

        btnBack.setOnClickListener(view1 -> {
            onSupportNavigateUp();
        });
        fab_addUsers.setVisibility(View.GONE);
        search_project.setVisibility(View.GONE);
        checkUserStat();
        readProject();
        search_project.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchProject(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void searchProject(String querySearch) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ListProjects");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                projects.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Project project = snapshot.getValue(Project.class);

                    if (project != null) {
                        if (project.getProjectName().toLowerCase().contains(querySearch.toLowerCase())) {
                            projects.add(project);
                        }
                    }
                }

                listProjectAdapter = new ListProjectAdapter(MyProjectActivity.this, projects);
                recyclerView.setAdapter(listProjectAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(MyProjectActivity.this, ErrorActivity.class));
        }
    }


    private void readProject() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query query = ref.orderByChild("id").equalTo(myId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String myProject = "" + snapshot.child("projectName").getValue();
                    search_project.setText(myProject);
                }
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