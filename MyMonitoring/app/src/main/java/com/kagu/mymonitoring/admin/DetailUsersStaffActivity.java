package com.kagu.mymonitoring.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.R;

public class DetailUsersStaffActivity extends AppCompatActivity {
    TextView uName, uEmail, uProject;
    ImageView mDp;
    CardView editProfile;
    ImageButton btnBack;
    FirebaseAuth firebaseAuth;

    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_users_staff);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        uName = findViewById(R.id.uName);
        uEmail = findViewById(R.id.uEmail);
        uProject = findViewById(R.id.uProject);
        editProfile = findViewById(R.id.editProfile);
        editProfile = findViewById(R.id.editProfile);

        mDp = findViewById(R.id.uDp);

        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String username = ""+ds.child("fullname").getValue();
                    String userMail = ""+ds.child("email").getValue();
                    String userProject = ""+ds.child("projectName").getValue();
                    String userDp = ""+ds.child("imageUrl").getValue();

                    uName.setText(username);
                    uEmail.setText(userMail);
                    uProject.setText(userProject);

                    //set dp
                    if (userDp.equals("default")){
                        Glide.with(getApplicationContext())
                                .load(R.drawable.ic_pic)
                                .into(mDp);
                    }else {
                        Glide.with(getApplicationContext())
                                .load(userDp)
                                .apply(new RequestOptions().override(75,75))
                                .into(mDp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        editProfile.setOnClickListener(view -> {
            Intent intentEdit = new Intent(getApplicationContext(), RegisterActivity.class);
            intentEdit.putExtra("key", "editUser");
            intentEdit.putExtra("editUserKey", id);
            intentEdit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentEdit);
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
    }

    

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}