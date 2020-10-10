package com.kagu.mymonitoring.users;

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
import android.widget.TextView;

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
import com.kagu.mymonitoring.adapter.UserAdapter;
import com.kagu.mymonitoring.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UsersQaActivity extends AppCompatActivity { //all user show karena ada lead qa yg ngatur all staff qa
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    EditText search_users;
    TextView titlePage;

    private FirebaseAuth firebaseAuth;
    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_users);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        recyclerView = findViewById(R.id.rv_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(UsersQaActivity.this));

        users = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();

        checkUserStat();
        readUsers();

        titlePage = findViewById(R.id.titlePage);
        search_users = findViewById(R.id.search_users);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
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

    @Override
    protected void onStart() {
        checkUserStat();
        super.onStart();
    }

    private void searchUsers(String querySearch) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null)
                        if (!user.getId().equals(myId))
                            if (Objects.equals(snapshot.child("type").getValue(String.class), "Student") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Person In Charge") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Quality Assurance") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Non Active Staff") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Non Active Student")) {
                                if (user.getFullname().toLowerCase().contains(querySearch.toLowerCase()) ||
                                        user.getType().toLowerCase().contains(querySearch.toLowerCase()) ||
                                        user.getProjectName().toLowerCase().contains(querySearch.toLowerCase())) {
                                    users.add(user);
                                }
                            }


                }

                userAdapter = new UserAdapter(UsersQaActivity.this, users);
                recyclerView.setAdapter(userAdapter);
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

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(UsersQaActivity.this, ErrorActivity.class));
        }
    }


    private void readUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (user != null)
                        if (!user.getId().equals(myId))
                            if (Objects.equals(snapshot.child("type").getValue(String.class), "Student") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Person In Charge") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Quality Assurance") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Non Active Staff") ||
                                    Objects.equals(snapshot.child("type").getValue(String.class), "Non Active Student")) {
                                //not show current user and admin
                                users.add(user);
                            }
                }
                userAdapter = new UserAdapter(UsersQaActivity.this, users);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}