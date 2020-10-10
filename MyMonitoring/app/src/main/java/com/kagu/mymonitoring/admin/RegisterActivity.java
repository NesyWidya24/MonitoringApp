package com.kagu.mymonitoring.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmail, mPassword, mFullname, uStartDate, uEndDate;
    Button mRegist;
    ImageView img_icon;
    TextView titleForm;
    TextInputLayout inputPass;
    Spinner mType, mProjectName;
    ValueEventListener listenerSpinner;
    DatabaseReference referenceSpinner;
    ArrayAdapter<String> adapterSpinner;
    ArrayList<String> listSpinner;


    DatabaseReference dbRefUser;
    String email, editFullname, editType, editProjectName, editStartDate, editEndDate, editEmail;

    FirebaseAuth mAuth1;
    FirebaseAuth mAuth2;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        //init
        mEmail = findViewById(R.id.EtEmail);
        mPassword = findViewById(R.id.EtPass);
        uStartDate = findViewById(R.id.uStartDate);
        uEndDate = findViewById(R.id.uEndDate);
        mFullname = findViewById(R.id.EtFullname);
        mRegist = findViewById(R.id.register_btn);
        inputPass = findViewById(R.id.inputPass);
        titleForm = findViewById(R.id.titleForm);
        img_icon = findViewById(R.id.img_icon);
        mType = findViewById(R.id.type);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });

        mProjectName = findViewById(R.id.projectNameRegister);
        listSpinner = new ArrayList<>();
        adapterSpinner = new ArrayAdapter<>(RegisterActivity.this,
                android.R.layout.simple_spinner_dropdown_item, listSpinner);
        mProjectName.setAdapter(adapterSpinner);
        adapterSpinner.add("-Choose Project Name-");
        referenceSpinner = FirebaseDatabase.getInstance().getReference("ListProjects");

        mAuth1 = FirebaseAuth.getInstance();

        //get data for edit
        Intent intent = getIntent();
        String isUpdate = "" + intent.getStringExtra("key");
        String editUserId = "" + intent.getStringExtra("editUserKey");
        if (isUpdate.equals("editUser")) {
            //for update
            img_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_update_user));
            titleForm.setText("Update Account");
            mRegist.setText("Update");
            loadUserData(editUserId);
        } else {
            //for add
            img_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_regist_user));
            titleForm.setText("Add Account");
            mRegist.setText("Register");
        }

        dbRefUser = FirebaseDatabase.getInstance().getReference("Users");
        Query query = dbRefUser.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    email = "" + ds.child("email").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fix regist akun yg d regist logout dan tetep stay d akun admin
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://mymonitoring-85732.firebaseio.com/")
                .setApiKey("AIzaSyA1emLe8bSgmBOk3glgaTEHnH7CJuTiB_k")
                .setApplicationId("1:514602650999:android:a9f431b7683bbebd8f2572").build();

        try {
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "Users");
            mAuth2 = FirebaseAuth.getInstance(firebaseApp);
        } catch (IllegalStateException e) {
            mAuth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("Users"));
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.usertypeStaff, R.layout.support_simple_spinner_dropdown_item);
        mType.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        //handle register
        mRegist.setOnClickListener(view -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            String startDate = uStartDate.getText().toString().trim();
            String endDate = uEndDate.getText().toString().trim();
            String projectName = mProjectName.getSelectedItem().toString();
            String fullname = mFullname.getText().toString().trim();
            String type = mType.getSelectedItem().toString();

            if (isUpdate.equals("editUser")) {
                if (TextUtils.isEmpty(fullname)) {
                    mFullname.setError("Fields are required");
                    return;
                } else if (TextUtils.isEmpty(startDate)) {
                    uStartDate.setError("Fields are required");
                    return;
                } else if (TextUtils.isEmpty(endDate)) {
                    uEndDate.setError("Fields are required");
                    return;
                } else if (projectName.equals("-Choose Project Name-")) {
                    Toast.makeText(RegisterActivity.this, "Please Input Project Name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (type.equals("-Choose User Type-")) {
                    Toast.makeText(RegisterActivity.this, "Please Input Type User", Toast.LENGTH_SHORT).show();
                    return;
                }
                startUpdate(fullname, startDate, endDate, type, projectName, editUserId);
            } else {
                if (TextUtils.isEmpty(fullname)) {
                    mFullname.setError("Fields are required");
                    return;
                } else if (TextUtils.isEmpty(startDate)) {
                    uStartDate.setError("Fields are required");
                    return;
                } else if (TextUtils.isEmpty(endDate)) {
                    uEndDate.setError("Fields are required");
                    return;
                } else if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Field are required");
                    return;
                } else if (projectName.equals("-Choose Project Name-")) {
                    Toast.makeText(RegisterActivity.this, "Please Input Project Name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (type.equals("-Choose User Type-")) {
                    Toast.makeText(RegisterActivity.this, "Please Input Type User", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmail.setError("Invalid Email");
                    mEmail.setFocusable(true);
                    return;
                } else if (password.length() < 6) {
                    mPassword.setError("Password length at least 6 characters");
                    mPassword.setFocusable(true);
                    return;
                }
                registerUser(email, password, projectName, endDate, type, startDate, fullname);
            }

        });
        retrievedData();
    }

    private void startUpdate(String fullname, String type, String projectName, String startDate, String endDate, String editUserId) {
        progressDialog.setMessage("Updating Data...");
        progressDialog.show();

        updateDataUser(fullname, type, projectName, startDate, endDate, editUserId);
    }

    private void updateDataUser(String fullname, String startDate, String endDate, String type, String projectName, String editUserId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        //put data user
        hashMap.put("fullname", fullname);
        hashMap.put("type", type);
        hashMap.put("startDate", startDate);
        hashMap.put("endDate", endDate);
        hashMap.put("projectName", projectName);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(editUserId)
                .updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Update Success..", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport").child("Traceable Matrix");
            Query query = reference.orderByChild("id").equalTo(editUserId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        if (child != null)
                            dataSnapshot.getRef().child(child).child("username").setValue(fullname);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        DatabaseReference refName = FirebaseDatabase.getInstance().getReference("Article");
        Query queryName = refName.orderByChild("id").equalTo(editUserId);
        queryName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    if (child != null)
                        dataSnapshot.getRef().child(child).child("uName").setValue(fullname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserData(String editUserId) {
        inputPass.setVisibility(View.GONE);
        mEmail.setEnabled(false);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get detail of data user
        Query query = ref.orderByChild("id").equalTo(editUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    editFullname = "" + ds.child("fullname").getValue();
                    editType = ds.child("type").getValue(String.class);
                    editStartDate = ds.child("startDate").getValue(String.class);
                    editEndDate = ds.child("endDate").getValue(String.class);
                    editProjectName = ds.child("projectName").getValue(String.class);
                    editEmail = "" + ds.child("email").getValue(String.class);

                    mFullname.setText(editFullname);
                    mEmail.setText(editEmail);
                    uStartDate.setText(editStartDate);
                    uEndDate.setText(editEndDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void retrievedData() {
        listenerSpinner = referenceSpinner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String project = item.child("projectName").getValue().toString();
                    listSpinner.add(project);
                }
                adapterSpinner.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void registerUser(String email, String password, final String projectName, final String endDate, final String type, String startDate, String fullname) {
        progressDialog.show();

        mAuth2.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        FirebaseUser user = mAuth2.getCurrentUser();

                        //ambil user email dan uid dari auth
                        String email1 = user.getEmail();
                        String uid = user.getUid();

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", uid);
                        hashMap.put("email", email1);
                        hashMap.put("fullname", fullname);
                        hashMap.put("startDate", startDate);
                        hashMap.put("endDate", endDate);
                        hashMap.put("imageUrl", "default");
                        hashMap.put("onlineStat", "offline");
                        hashMap.put("typingTo", "noOne");
                        hashMap.put("type", type);
                        hashMap.put("projectName", projectName);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("Users");
                        reference.child(uid).setValue(hashMap);

                        Toast.makeText(RegisterActivity.this, "Registered....\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                        mAuth2.signOut();
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Authentications failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
