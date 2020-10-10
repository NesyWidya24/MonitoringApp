package com.kagu.mymonitoring.qa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
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

import java.util.Calendar;
import java.util.Locale;


public class DetailArticleActivity extends AppCompatActivity {

    TextView mName, pTitle, pDesc, mtimeReport,detail;

    ImageView mDp, pImgPosts;
    ImageButton btnEdit, btnBack;

    FirebaseAuth firebaseAuth;

    String articleId, myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_article);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        mName = findViewById(R.id.uNameArticle);
        pTitle = findViewById(R.id.title_article);
        pDesc = findViewById(R.id.descArticle);
        pImgPosts = findViewById(R.id.pImgArticle);
        mtimeReport = findViewById(R.id.timeArticle);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);
        detail = findViewById(R.id.detail);

        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        articleId = intent.getStringExtra("articleId");
        Query query = FirebaseDatabase.getInstance().getReference("Article").orderByChild("postId").equalTo(articleId);
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
                    }
                    detail.setText(pTitleArticle);
                    if (!id.equals(myId))
                        btnEdit.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnEdit.setOnClickListener(view -> {
            Intent intentEdit = new Intent(getApplicationContext(), AddArticleActivity.class);
            intentEdit.putExtra("key", "editArticle");
            intentEdit.putExtra("editArticleKey", articleId);
            intentEdit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentEdit);
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
            startActivity(new Intent(DetailArticleActivity.this, ErrorActivity.class));
            finish();
        }
    }
}