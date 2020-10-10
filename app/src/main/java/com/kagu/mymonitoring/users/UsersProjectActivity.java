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

public class UsersProjectActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    EditText search_users;
    TextView titlePage;

    private FirebaseAuth firebaseAuth;
    String myId, project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        recyclerView = findViewById(R.id.rv_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(UsersProjectActivity.this));

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

    private void searchUsers(String s) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query queryUser = ref.orderByChild("id").equalTo(myId);
        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    project = "" + dataSnapshot1.child("projectName").getValue();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                Query query = reference.orderByChild("projectName").equalTo(project); //filter user sesuai dengan projectName
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        users.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null)
                                if (!user.getId().equals(myId)) //show user selain current user
                                    if (user.getFullname().toLowerCase().contains(s.toLowerCase()) ||
                                            user.getType().toLowerCase().contains(s.toLowerCase())) {
                                        users.add(user);
                                    }
                        }

                        userAdapter = new UserAdapter(UsersProjectActivity.this, users);
                        recyclerView.setAdapter(userAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(UsersProjectActivity.this, ErrorActivity.class));
        }
    }


    private void readUsers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query queryUser = ref.orderByChild("id").equalTo(myId);
        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    project = "" + dataSnapshot1.child("projectName").getValue();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                Query query = reference.orderByChild("projectName").equalTo(project); //filter user sesuai dengan projectName
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        users.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                if (!user.getId().equals(myId)) //show user selain current user
                                    users.add(user);
                                String title = getResources().getString(R.string.users) + " " + user.getProjectName();
                                titlePage.setText(title);
                            }
                        }
                        userAdapter = new UserAdapter(UsersProjectActivity.this, users);
                        recyclerView.setAdapter(userAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}