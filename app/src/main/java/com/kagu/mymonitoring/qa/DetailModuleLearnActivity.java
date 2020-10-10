package com.kagu.mymonitoring.qa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
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
import com.kagu.mymonitoring.student.DetailDailyReportActivity;
import com.kagu.mymonitoring.student.FullImgReportActivity;

import java.util.Calendar;
import java.util.Locale;


public class DetailModuleLearnActivity extends AppCompatActivity {

    TextView mName, pTitle, pDesc, mtimeReport,detail;

    ImageView mDp, pImgPosts;
    ImageButton btnEdit, btnBack;

    FirebaseAuth firebaseAuth;

    String moduleId, myId,type;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_module);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        mName = findViewById(R.id.uNameArticle);
        pTitle = findViewById(R.id.title_module);
        pDesc = findViewById(R.id.descModule);
        pImgPosts = findViewById(R.id.pImgModule);
        mtimeReport = findViewById(R.id.timeModule);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);
        detail = findViewById(R.id.detail);

        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        moduleId = intent.getStringExtra("moduleId");
        Query query = FirebaseDatabase.getInstance().getReference("ModuleLearn").orderByChild("moduleId").equalTo(moduleId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String id =""+ds.child("id").getValue();
                    String uName =""+ds.child("uName").getValue();
                    String pTitleArticle =""+ds.child("pTitleArticle").getValue();
                    String pDescArticle =""+ds.child("pDescArticle").getValue();
                    String postImg =""+ds.child("postImg").getValue();
                    String postTime =""+ds.child("postTime").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(postTime));
                    String pTime = DateFormat.format("dd MMM yyyy hh:mm aa", calendar).toString();

                    mName.setText(uName);
                    mtimeReport.setText(pTime);
                    pTitle.setText(pTitleArticle);
                    pDesc.setText(pDescArticle);

                    //set post img
                    //if there is no image (p.Img.equals("noImg")) then hide ImageView
                    if (postImg.equals("noImg")) {
                        pImgPosts.setVisibility(View.GONE);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(postImg)
                                .into(pImgPosts);
                        pImgPosts.setOnClickListener(view -> {
                            Intent intent = new Intent(DetailModuleLearnActivity.this, FullImgModuleActivity.class);
                            intent.putExtra("ImgModule", postImg);
                            startActivity(intent);
                        });
                    }
                    detail.setText(pTitleArticle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //get data
        Query query1 = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(currentUser.getUid());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    type = "" + ds.child("type").getValue();

                    if (type.equals("Student")||type.equals("Non Active Student"))
                        btnEdit.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnEdit.setOnClickListener(view -> {
            Intent intentEdit = new Intent(getApplicationContext(), AddModuleActivity.class);
            intentEdit.putExtra("key", "editModule");
            intentEdit.putExtra("editModuleKey", moduleId);
            intentEdit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentEdit);
        });
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
            startActivity(new Intent(DetailModuleLearnActivity.this, ErrorActivity.class));
            finish();
        }
    }
}