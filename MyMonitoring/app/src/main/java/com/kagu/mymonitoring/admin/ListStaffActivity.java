package com.kagu.mymonitoring.admin;

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
import com.kagu.mymonitoring.adapter.ListStaffAdapter;
import com.kagu.mymonitoring.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//delete auth user
public class ListStaffActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ListStaffAdapter listStaffAdapter;
    private List<User> users;
    EditText search_users;

    private FirebaseAuth firebaseAuth;
    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_staff);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.rv_staff);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        users = new ArrayList<>();

        FloatingActionButton fab_addUsers = findViewById(R.id.fab_addUsers);

        checkUserStat();
        readUsers();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view1 -> {
            onSupportNavigateUp();
        });
        fab_addUsers.setOnClickListener(view -> {
            startActivity(new Intent(ListStaffActivity.this, RegisterActivity.class));
        });
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
    }

    private void searchUsers(String s) {
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s + "\uf8ff"); //https://firebase.google.com/docs/database/admin/retrieve-data#kueri-rentang

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (Objects.equals(snapshot.child("type").getValue(String.class), "Quality Assurance") ||
                            Objects.equals(snapshot.child("type").getValue(String.class), "Person In Charge"))  {
                        users.add(user);
                    }
                }

                listStaffAdapter = new ListStaffAdapter(ListStaffActivity.this, users);
                recyclerView.setAdapter(listStaffAdapter);
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
            startActivity(new Intent(ListStaffActivity.this, ErrorActivity.class));
        }
    }


    private void readUsers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (Objects.equals(snapshot.child("type").getValue(String.class), "Quality Assurance") ||
                            Objects.equals(snapshot.child("type").getValue(String.class), "Person In Charge"))  {
                        users.add(user);
                    }
                }

                listStaffAdapter = new ListStaffAdapter(ListStaffActivity.this, users);
                recyclerView.setAdapter(listStaffAdapter);
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