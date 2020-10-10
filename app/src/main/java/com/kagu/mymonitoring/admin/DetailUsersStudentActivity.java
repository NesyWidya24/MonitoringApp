package com.kagu.mymonitoring.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.R;

public class DetailUsersStudentActivity extends AppCompatActivity {
    TextView uName,
            uEmail,
            uProject,
            startDate,
            endDate,
            uStudentPhone,
            studentAddress,
            schoolName,
            nimNis,
            schoolAddress,
            majors,
            phone;
    ImageView mDp;
    ImageButton btnBack, btnEdit;

    private FirebaseUser user;
    FirebaseAuth firebaseAuth;

    String id, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_users_student);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        mDp = findViewById(R.id.mDp);
        uName = findViewById(R.id.uName);
        uEmail = findViewById(R.id.uEmail);
        uProject = findViewById(R.id.uProject);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);
        uStudentPhone = findViewById(R.id.uStudentPhone);
        studentAddress = findViewById(R.id.studentAddress);
        schoolName = findViewById(R.id.schoolName);
        nimNis = findViewById(R.id.nimNis);
        schoolAddress = findViewById(R.id.schoolAddress);
        majors = findViewById(R.id.majors);
        phone = findViewById(R.id.phone);

        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        //get data
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String username = "" + ds.child("fullname").getValue();
                    String userMail = "" + ds.child("email").getValue();
                    String userStartDate = "" + ds.child("startDate").getValue();
                    String userEndDate = "" + ds.child("endDate").getValue();
                    String userProject = "" + ds.child("projectName").getValue();
                    String userDp = "" + ds.child("imageUrl").getValue();
                    String type = "" + ds.child("type").getValue();

                    uName.setText(username);
                    uEmail.setText(userMail);
                    uProject.setText(userProject);
                    startDate.setText(userStartDate);
                    endDate.setText(userEndDate);

                    //set dp
                    if (userDp.equals("default")) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.ic_pic)
                                .into(mDp);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(userDp)
                                .apply(new RequestOptions().override(75, 75))
                                .into(mDp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnEdit.setOnClickListener(view -> {
            Intent intentEdit = new Intent(getApplicationContext(), RegisterActivity.class);
            intentEdit.putExtra("key", "editUser");
            intentEdit.putExtra("editUserKey", id);
            intentEdit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentEdit);
        });
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
        //get data
        Query query1 = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(user.getUid());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    type = "" + ds.child("type").getValue();

                    if (type.equals("Quality Assurance"))
                        btnEdit.setVisibility(View.INVISIBLE); //hilangin btn Edit buat tim QA, edit only student and admin
                }
            }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
        loadDataStudent();
    }

    private void loadDataStudent() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DataStudent");
        Query query = ref.orderByChild("id").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userStudentPhone = "" + ds.child("studentPhone").getValue();
                    String userStudentAddress = "" + ds.child("studentAddress").getValue();
                    String userSchoolName = "" + ds.child("schoolName").getValue();
                    String userNimNis = "" + ds.child("nimNis").getValue();
                    String userSchoolAddress = "" + ds.child("schoolAddress").getValue();
                    String userMajors = "" + ds.child("majors").getValue();
                    String userPhone = "" + ds.child("schoolPhone").getValue();

                    uStudentPhone.setText(userStudentPhone);
                    studentAddress.setText(userStudentAddress);
                    schoolName.setText(userSchoolName);
                    nimNis.setText(userNimNis);
                    schoolAddress.setText(userSchoolAddress);
                    majors.setText(userMajors);
                    phone.setText(userPhone);
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