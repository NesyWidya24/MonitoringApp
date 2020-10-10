package com.kagu.mymonitoring.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class DetailProjectActivity extends AppCompatActivity {
    TextView projectName, clientName, descProject,namePic;

    ImageView pImgProject;
    ImageButton btnEdit, btnBack;

    FirebaseAuth firebaseAuth;

    String idProject, myId,type;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_project);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        projectName = findViewById(R.id.projectName);
        clientName = findViewById(R.id.clientName);
        descProject = findViewById(R.id.descProject);
        namePic = findViewById(R.id.namePic);
        pImgProject = findViewById(R.id.pImgProject);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);
        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        idProject = intent.getStringExtra("idProject");
        Query query = FirebaseDatabase.getInstance().getReference("ListProjects").orderByChild("idProject").equalTo(idProject);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String mProjectName = ""+ds.child("projectName").getValue();
                    String imgProject = ""+ds.child("imgProject").getValue();
                    String pProjectClient = ""+ds.child("projectClient").getValue();
                    String pProjectDesc = ""+ds.child("projectDesc").getValue();
                    String pNamePic = ""+ds.child("namePic").getValue();

                    projectName.setText(mProjectName);
                    clientName.setText(pProjectClient);
                    descProject.setText(pProjectDesc);
                    namePic.setText(pNamePic);

                    //set post img
                    //if there is no image (p.Img.equals("noImg")) then hide ImageView
                    if (imgProject.equals("noImg")) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_no_img).into(pImgProject);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(imgProject)
                                .into(pImgProject);
                    }
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(getString(R.string.detailTitle) + projectName);
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

                    if (type.equals("Student"))
                        btnEdit.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnEdit.setOnClickListener(view -> {
            Intent intentEdit = new Intent(getApplicationContext(), AddProjectActivity.class);
            intentEdit.putExtra("key", "editProject");
            intentEdit.putExtra("editProjectKey", idProject);
            intentEdit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentEdit);
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
        checkUserStatus();
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();

        } else {
            startActivity(new Intent(DetailProjectActivity.this, ErrorActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}