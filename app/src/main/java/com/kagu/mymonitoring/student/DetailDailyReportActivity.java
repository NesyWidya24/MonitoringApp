package com.kagu.mymonitoring.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.chat.FullImageActivity;

public class DetailDailyReportActivity extends AppCompatActivity {
    //var view from row_dailyreport
    TextView mUsernameStudent, mResultReport,mProjectNameReport, mLinkDocs, mCodeModule,
            mTitleModule, mCodeScenario, mTitleScenario, mCodeTestcase, mDescReport,mTitleTestcase,
            mExpectedReport, mDescResult,mReproduce,mNoteReport;
    ImageView pImgPosts;
    ImageButton btnEdit, btnBack;

    CardView cv_note, cv_reproduce;

    FirebaseAuth firebaseAuth;

    String reportId, myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_daily_report);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        mUsernameStudent = findViewById(R.id.usernameStudent);
        mProjectNameReport = findViewById(R.id.projectNameReport);
        mLinkDocs = findViewById(R.id.linkDocs);
        mCodeModule = findViewById(R.id.codeModule);
        mTitleModule = findViewById(R.id.titleModule);
        mCodeScenario = findViewById(R.id.codeScenario);
        mTitleScenario = findViewById(R.id.titleScenario);
        mCodeTestcase = findViewById(R.id.codeTestcase);
        mTitleTestcase = findViewById(R.id.titleTestcase);
        mResultReport = findViewById(R.id.resultReport);
        mDescReport = findViewById(R.id.descReport);
        mExpectedReport = findViewById(R.id.expectedReport);
        mDescResult = findViewById(R.id.descResult);
        mReproduce = findViewById(R.id.reproduce);
        mNoteReport = findViewById(R.id.noteReport);
        pImgPosts = findViewById(R.id.pImgPost);

        cv_note = findViewById(R.id.cv_note);
        cv_reproduce = findViewById(R.id.cv_reproduce);

        btnEdit = findViewById(R.id.btnEdit);

        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        reportId = intent.getStringExtra("reportId");
        Query query = FirebaseDatabase.getInstance().getReference("DailyReport").orderByChild("reportId").equalTo(reportId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String id =""+ds.child("id").getValue();
                    String username =""+ds.child("username").getValue();
                    String pProjectName =""+ds.child("pProjectName").getValue();
                    String pLinkDocs =""+ds.child("pLinkDocs").getValue();
                    String pCodeModule =""+ds.child("pCodeModule").getValue();
                    String pTitleModule =""+ds.child("pTitleModule").getValue();
                    String pCodeScenario =""+ds.child("pCodeScenario").getValue();
                    String pTitleScenario =""+ds.child("pTitleScenario").getValue();
                    String pCodeTS =""+ds.child("pCodeTC").getValue();
                    String pTitleTS =""+ds.child("pTitleTC").getValue();
                    String pResult =""+ds.child("pResult").getValue();
                    String pDescTest =""+ds.child("pDescTest").getValue();
                    String pExpected =""+ds.child("pExpected").getValue();
                    String pDescResult =""+ds.child("pDescResult").getValue();
                    String pReproduce =""+ds.child("pReproduce").getValue();
                    String pNote =""+ds.child("pNote").getValue();
                    String postImg =""+ds.child("postImg").getValue();

                    mUsernameStudent.setText(username);
                    mProjectNameReport.setText(pProjectName);
                    mLinkDocs.setText(pLinkDocs);
                    mCodeModule.setText(pCodeModule);
                    mTitleModule.setText(pTitleModule);
                    mCodeScenario.setText(pCodeScenario);
                    mTitleScenario.setText(pTitleScenario);
                    mCodeTestcase.setText(pCodeTS);
                    mTitleTestcase.setText(pTitleTS);
                    mResultReport.setText(pResult);
                    mDescReport.setText(pDescTest);
                    mExpectedReport.setText(pExpected);
                    mDescResult.setText(pDescResult);
                    mReproduce.setText(pReproduce);
                    mNoteReport.setText(pNote);

                    //set Note
                    if (pNote.equals("")) {
                        cv_note.setVisibility(View.GONE);
                    } else {
                        cv_note.setVisibility(View.VISIBLE);
                        mNoteReport.setText(pNote);
                    }
                    //set Reproduce
                    if (pReproduce.equals("")) {
                        cv_reproduce.setVisibility(View.GONE);
                    } else {
                        cv_reproduce.setVisibility(View.VISIBLE);
                        mReproduce.setText(pReproduce);
                    }

                    //set post img
                    //if there is no image (p.Img.equals("noImg")) then hide ImageView
                    if (postImg.equals("noImg")) {
                        pImgPosts.setVisibility(View.GONE);
                    } else {
                        Glide.with(DetailDailyReportActivity.this)
                                .load(postImg)
                                .into(pImgPosts);
                        pImgPosts.setOnClickListener(view -> {
                            Intent intent = new Intent(DetailDailyReportActivity.this, FullImgReportActivity.class);
                            intent.putExtra("ImgReport", postImg);
                            startActivity(intent);
                        });
                    }

                    //set result
                    //klau scenario nya success warna ijo, klau ada bug (failed) warna merah
                    if (pResult.equals("Success")) {
                        mResultReport.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
                        pImgPosts.setVisibility(View.GONE);
                        cv_reproduce.setVisibility(View.GONE);
                    } else {
                        cv_reproduce.setVisibility(View.VISIBLE);
                        pImgPosts.setVisibility(View.VISIBLE);
                        mResultReport.setTextColor(getApplicationContext().getResources().getColor(R.color.red));
                    }
                    if (!id.equals(myId))
                        btnEdit.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnEdit.setOnClickListener(view -> {
            Intent intentEdit = new Intent(getApplicationContext(), AddDailyReportActivity.class);
            intentEdit.putExtra("key", "editReport");
            intentEdit.putExtra("editReportKey", reportId);
            intentEdit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentEdit);
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();

        } else {
            startActivity(new Intent(DetailDailyReportActivity.this, ErrorActivity.class));
            finish();
        }
    }
}