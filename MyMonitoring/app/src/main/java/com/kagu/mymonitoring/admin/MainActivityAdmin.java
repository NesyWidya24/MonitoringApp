package com.kagu.mymonitoring.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.Log_inActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.entity.User;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivityAdmin extends AppCompatActivity {
    TextView username, department;
    CircleImageView profile_img;
    Dialog popupListUsers;
    Button btnStudent, btnStaff, btnAddAccount;
    TextView titlePopup;
    ImageView closePopup;
    ViewGroup mainAdmin;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    ImageButton btnExit;

    FirebaseAuth mAuth1;
    Button btn_add_account, btn_add_project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();


        profile_img = findViewById(R.id.profileImgAdmin);
        mainAdmin = (ViewGroup) findViewById(R.id.mainAdmin);
        username = findViewById(R.id.usernameAdmin);
        department = findViewById(R.id.departmentAdmin);
        btn_add_account = findViewById(R.id.add_account_staff);
        btn_add_project = findViewById(R.id.add_data);
        btnExit = findViewById(R.id.btnExitAdmin);
        mAuth1 = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth1.getCurrentUser();
        if (user != null)
            user.getUid();
        btnExit.setOnClickListener(view -> {
            mAuth1.signOut();
            Intent i = new Intent(MainActivityAdmin.this, Log_inActivity.class);
            startActivity(i);
            finish();
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    username.setText(user.getFullname());
                    department.setText(user.getType());
                    if (user.getImageUrl().equals("default")) {
                        profile_img.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(MainActivityAdmin.this).load(user.getImageUrl()).into(profile_img);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        popupListUsers = new Dialog(this);
        Window window = popupListUsers.getWindow();
        assert window != null;
        WindowManager.LayoutParams layoutParams = window.getAttributes();

        layoutParams.gravity = Gravity.BOTTOM;//posisi dialog
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND; //all layar yg di backgroud di redupkan
        window.setAttributes(layoutParams);

        btnStudent = findViewById(R.id.btnStudent);
        btnStaff = findViewById(R.id.btnStaff);

        btn_add_project.setOnClickListener(view -> {
            Intent i = new Intent(MainActivityAdmin.this, ListProjectActivity.class);
            startActivity(i);
        });
        btn_add_account.setOnClickListener(view -> {
            showChooseTypeUsers();
        });
    }

    @Override
    protected void onResume() {//klau gaada pas openListStaffActivity trus balik lagi masih muncul dialog nya
        super.onResume();
        popupListUsers.dismiss();
    }

    @Override
    protected void onPause() { //klau gaada pas show activity nya di klik btn_add_account dialog muncul trus ilang
        super.onPause();
        popupListUsers.dismiss();
    }

    private void showChooseTypeUsers() {
        popupListUsers.setContentView(R.layout.popup_list_users);
        titlePopup = popupListUsers.findViewById(R.id.titlePopup);
        btnStudent = popupListUsers.findViewById(R.id.btnStudent);
        btnStaff = popupListUsers.findViewById(R.id.btnStaff);
        btnAddAccount = popupListUsers.findViewById(R.id.btnAddAccount);

        btnStudent.setOnClickListener(view -> {
            Intent i = new Intent(MainActivityAdmin.this, ListStudentActivity.class);
            startActivity(i);
        });
        btnAddAccount.setOnClickListener(view -> {
            Intent i = new Intent(MainActivityAdmin.this, RegisterActivity.class);
            startActivity(i);
        });
        btnStaff.setOnClickListener(view -> {
            Intent i = new Intent(MainActivityAdmin.this, ListStaffActivity.class);
            startActivity(i);
        });

        Objects.requireNonNull(popupListUsers.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupListUsers.show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakah anda benar-benar ingin keluar?")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialogInterface, i) -> {
                    onPause();
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }).setNegativeButton("Tidak", (dialogInterface, i) -> {

        }).show();
    }
}
