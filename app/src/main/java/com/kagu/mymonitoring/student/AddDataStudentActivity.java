package com.kagu.mymonitoring.student;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.admin.AddProjectActivity;
import com.kagu.mymonitoring.entity.User;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddDataStudentActivity extends AppCompatActivity {
    EditText uStudentName,
            uEtEmail,
            uStartDate,
            uEndDate,
            uStudentAddress,
            uStudentPhone,
            uSchoolName,
            uNimNis,
            uSchoolAddress,
            uMajors,
            uSchoolPhone;
    Button addDataStudent;
    TextView titleForm;
    DatabaseReference dbRefUser;
    String id,
            editStudentName,
            editEtEmail,
            editStartDate,
            editEndDate,
            editStudentAddress,
            editStudentPhone,
            editSchoolName,
            editNimNis,
            editSchoolAddress,
            editMajors,
            editSchoolPhone,
            editImg;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    private CircleImageView profile_img;

    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    //permission constants
    private static final int CAMERA_REQUEST_ID = 100;
    private static final int STORAGE_REQUEST_ID = 200;

    //img pick constants
    private static final int PICK_CAMERA_ID = 300;
    private static final int PICK_GALLERY_ID = 400;

    //permission array
    String[] cameraPerms;
    String[] storagePerms;

    //    image picked will be samed in this uri
    Uri imgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_student);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        //init
        uStudentName = findViewById(R.id.uStudentName);
        uEtEmail = findViewById(R.id.uEtEmail);
        uStartDate = findViewById(R.id.uStartDate);
        uEndDate = findViewById(R.id.uEndDate);
        uStudentAddress = findViewById(R.id.uStudentAddress);
        uStudentPhone = findViewById(R.id.uStudentPhone);
        uSchoolName = findViewById(R.id.uSchoolName);
        uNimNis = findViewById(R.id.uNimNis);
        uSchoolAddress = findViewById(R.id.uSchoolAddress);
        uMajors = findViewById(R.id.uMajors);
        uSchoolPhone = findViewById(R.id.uSchoolPhone);
        addDataStudent = findViewById(R.id.addDataStudent);
        titleForm = findViewById(R.id.titleForm);
        profile_img = findViewById(R.id.profileImg);
        cameraPerms = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePerms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profile_img.setOnClickListener(view12 -> showImgPick());

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });

        auth = FirebaseAuth.getInstance();

        //get data for edit
        Intent intent = getIntent();
        String isUpdate = "" + intent.getStringExtra("key");
        String editStudentId = "" + intent.getStringExtra("editStudentKey");
        if (isUpdate.equals("editStudent")) {
            //for update
            titleForm.setText("Update Data Student");
            addDataStudent.setText("Update Data");
            loadUserData(editStudentId);
        }

        dbRefUser = FirebaseDatabase.getInstance().getReference("Users"); //get data from node Users
        Query query = dbRefUser.orderByChild("id").equalTo(editStudentId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    id = "" + ds.child("id").getValue();
                    editEtEmail = "" + ds.child("email").getValue();
                    editStartDate = "" + ds.child("startDate").getValue();
                    editEndDate = "" + ds.child("endDate").getValue();
                    editStudentName = "" + ds.child("fullname").getValue();
                    editImg = "" + ds.child("imageUrl").getValue();

                    uStudentName.setText(editStudentName);
                    uEtEmail.setText(editEtEmail);
                    uStartDate.setText(editStartDate);
                    uEndDate.setText(editEndDate);

                    //set post img
                    if (editImg.equals("default")) {
                        profile_img.setImageResource(R.drawable.ic_add_img);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(editImg)
                                .into(profile_img);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Update Data Student...");

        addDataStudent.setOnClickListener(view -> {
            String mStudentName = uStudentName.getText().toString().trim();
            String mEtEmail = uEtEmail.getText().toString().trim();
            String mStartDate = uStartDate.getText().toString().trim();
            String mEndDate = uEndDate.getText().toString().trim();
            String mStudentAddress = uStudentAddress.getText().toString().trim();
            String mStudentPhone = uStudentPhone.getText().toString().trim();
            String mSchoolName = uSchoolName.getText().toString().trim();
            String mNimNis = uNimNis.getText().toString().trim();
            String mSchoolAddress = uSchoolAddress.getText().toString().trim();
            String mMajors = uMajors.getText().toString().trim();
            String mSchoolPhone = uSchoolPhone.getText().toString().trim();

            if (isUpdate.equals("editStudent")) {
                if (TextUtils.isEmpty(mStudentName)) {
                    uStudentName.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter Student Name...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mEtEmail)) {
                    uEtEmail.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter Email...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mStartDate)) {
                    uStartDate.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter Start Date...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mEndDate)) {
                    uEndDate.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter End Date...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mStudentAddress)) {
                    uStudentAddress.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter Student Address...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mStudentPhone)) {
                    uStudentPhone.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter Student Phone...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mSchoolName)) {
                    uSchoolName.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter School Name...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mNimNis)) {
                    uNimNis.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter Nim or Nis...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mSchoolAddress)) {
                    uSchoolAddress.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter School Address...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mMajors)) {
                    uMajors.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter Majors...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mSchoolPhone)) {
                    uSchoolPhone.setError("Fields are required");
                    Toast.makeText(AddDataStudentActivity.this, "Enter School Phone...", Toast.LENGTH_SHORT).show();
                    return;
                }
                startUpdate(mStudentName,
                        mStudentAddress,
                        mStudentPhone,
                        mSchoolName,
                        mNimNis,
                        mSchoolAddress,
                        mMajors,
                        mSchoolPhone,
                        editStudentId);
            }
        });
    }

    private void loadUserData(String editStudentId) {
        uEtEmail.setEnabled(false);
        uStartDate.setEnabled(false);
        uEndDate.setEnabled(false);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DataStudent");
        //get detail of data user
        Query query = ref.orderByChild("id").equalTo(editStudentId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    editStudentAddress = "" + ds.child("studentAddress").getValue();
                    editStudentPhone = "" + ds.child("studentPhone").getValue();
                    editSchoolName = "" + ds.child("schoolName").getValue();
                    editNimNis = "" + ds.child("nimNis").getValue();
                    editSchoolAddress = "" + ds.child("schoolAddress").getValue();
                    editMajors = "" + ds.child("majors").getValue();
                    editSchoolPhone = "" + ds.child("schoolPhone").getValue();


                    uStudentAddress.setText(editStudentAddress);
                    uStudentPhone.setText(editStudentPhone);
                    uSchoolName.setText(editSchoolName);
                    uNimNis.setText(editNimNis);
                    uSchoolAddress.setText(editSchoolAddress);
                    uMajors.setText(editMajors);
                    uSchoolPhone.setText(editSchoolPhone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startUpdate(String mStudentName, String mStudentAddress,
                             String mStudentPhone,
                             String mSchoolName,
                             String mNimNis,
                             String mSchoolAddress,
                             String mMajors,
                             String mSchoolPhone,
                             String editStudentId) {
        progressDialog.setMessage("Updating Data...");
        progressDialog.show();

        if (!editImg.equals("default")) {
            //with img
            updateWasWithImg(mStudentName,
                    mStudentAddress,
                    mStudentPhone,
                    mSchoolName,
                    mNimNis,
                    mSchoolAddress,
                    mMajors,
                    mSchoolPhone,
                    editStudentId);
        } else if (profile_img.getDrawable() != null) {
            //with img
            updateWithNowImg(mStudentName,
                    mStudentAddress,
                    mStudentPhone,
                    mSchoolName,
                    mNimNis,
                    mSchoolAddress,
                    mMajors,
                    mSchoolPhone,
                    editStudentId);
        } else {
            //without img
            updateDataStudent(mStudentName,
                    mStudentAddress,
                    mStudentPhone,
                    mSchoolName,
                    mNimNis,
                    mSchoolAddress,
                    mMajors,
                    mSchoolPhone,
                    editStudentId);
        }
    }

    private void updateWithNowImg(String mStudentName, String mStudentAddress,
                                  String mStudentPhone,
                                  String mSchoolName,
                                  String mNimNis,
                                  String mSchoolAddress,
                                  String mMajors,
                                  String mSchoolPhone,
                                  String editStudentId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "uploads/" + timestamp;

        //get img from iv
        Bitmap bitmap = ((BitmapDrawable) profile_img.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //img compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray(); //metranskode gambar ke Byte array

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    //img uploaded get its url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;

                    String downloadUri = uriTask.getResult().toString();
                    if (uriTask.isSuccessful()) {
                        //url is received, upload to firebase database

                        HashMap<String, Object> hashMapUsers = new HashMap<>();
                        hashMapUsers.put("email", editEtEmail);
                        hashMapUsers.put("startDate", editStartDate);
                        hashMapUsers.put("endDate", editEndDate);
                        hashMapUsers.put("fullname", mStudentName);
                        hashMapUsers.put("imageUrl", downloadUri);


                        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
                        refUsers.child(editStudentId)
                                .updateChildren(hashMapUsers)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddDataStudentActivity.this, "Update Success..", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        //change name on DailyReport
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
                        Query query = reference.orderByChild("id").equalTo(editStudentId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (child != null)
                                        dataSnapshot.getRef().child(child).child("username").setValue(mStudentName);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        HashMap<String, Object> hashMapDataStudent = new HashMap<>();
                        //put data user

                        hashMapDataStudent.put("id", editStudentId);
                        hashMapDataStudent.put("studentAddress", mStudentAddress);
                        hashMapDataStudent.put("studentPhone", mStudentPhone);
                        hashMapDataStudent.put("schoolName", mSchoolName);
                        hashMapDataStudent.put("nimNis", mNimNis);
                        hashMapDataStudent.put("schoolAddress", mSchoolAddress);
                        hashMapDataStudent.put("majors", mMajors);
                        hashMapDataStudent.put("schoolPhone", mSchoolPhone);

                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("DataStudent");
                        ref1.child(editStudentId)
                                .updateChildren(hashMapDataStudent)
                                .addOnSuccessListener(aVoid2 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddDataStudentActivity.this, "Update Success..", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWasWithImg(String mStudentName, String mStudentAddress,
                                  String mStudentPhone,
                                  String mSchoolName,
                                  String mNimNis,
                                  String mSchoolAddress,
                                  String mMajors,
                                  String mSchoolPhone,
                                  String editStudentId) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImg);
        sRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //img deleted, upload new img
                    //for post-img name
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String filePathAndName = "uploads/" + timestamp;

                    //get img from iv
                    Bitmap bitmap = ((BitmapDrawable) profile_img.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //img compress
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] data = baos.toByteArray();

                    StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                    ref.putBytes(data)
                            .addOnSuccessListener(taskSnapshot -> {
                                //img uploaded get its url
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful()) ;

                                String downloadUri = uriTask.getResult().toString();
                                if (uriTask.isSuccessful()) {
                                    //url is received, upload to firebase database

                                    HashMap<String, Object> hashMapUsers = new HashMap<>();
                                    hashMapUsers.put("email", editEtEmail);
                                    hashMapUsers.put("startDate", editStartDate);
                                    hashMapUsers.put("endDate", editEndDate);
                                    hashMapUsers.put("fullname", mStudentName);
                                    hashMapUsers.put("imageUrl", downloadUri);


                                    DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
                                    refUsers.child(editStudentId)
                                            .updateChildren(hashMapUsers)
                                            .addOnSuccessListener(aVoid1 -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddDataStudentActivity.this, "Update Success..", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });

                                    //change name on DailyReport
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
                                    Query query = reference.orderByChild("id").equalTo(editStudentId);
                                    query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                String child = ds.getKey();
                                                if (child != null)
                                                    dataSnapshot.getRef().child(child).child("username").setValue(mStudentName);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    HashMap<String, Object> hashMapDataStudent = new HashMap<>();
                                    //put data user

                                    hashMapDataStudent.put("id", editStudentId);
                                    hashMapDataStudent.put("studentAddress", mStudentAddress);
                                    hashMapDataStudent.put("studentPhone", mStudentPhone);
                                    hashMapDataStudent.put("schoolName", mSchoolName);
                                    hashMapDataStudent.put("nimNis", mNimNis);
                                    hashMapDataStudent.put("schoolAddress", mSchoolAddress);
                                    hashMapDataStudent.put("majors", mMajors);
                                    hashMapDataStudent.put("schoolPhone", mSchoolPhone);

                                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("DataStudent");
                                    ref1.child(editStudentId)
                                            .updateChildren(hashMapDataStudent)
                                            .addOnSuccessListener(aVoid2 -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddDataStudentActivity.this, "Update Success..", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateDataStudent(String mStudentName, String mStudentAddress,
                                   String mStudentPhone,
                                   String mSchoolName,
                                   String mNimNis,
                                   String mSchoolAddress,
                                   String mMajors,
                                   String mSchoolPhone,
                                   String editStudentId) {
        HashMap<String, Object> hashMapUsers = new HashMap<>();
        hashMapUsers.put("email", editEtEmail);
        hashMapUsers.put("startDate", editStartDate);
        hashMapUsers.put("endDate", editEndDate);
        hashMapUsers.put("fullname", mStudentName);
        hashMapUsers.put("imageUrl", "default");

//  Change data on Users
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refUsers.child(editStudentId)
                .updateChildren(hashMapUsers)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDataStudentActivity.this, "Update Success..", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        //change name on DailyReport
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
        Query query = reference.orderByChild("id").equalTo(editStudentId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    if (child != null)
                        dataSnapshot.getRef().child(child).child("username").setValue(mStudentName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//Change data on DataStudent
        HashMap<String, Object> hashMapDataStudent = new HashMap<>();
        //put data user

        hashMapDataStudent.put("id", editStudentId);
        hashMapDataStudent.put("studentAddress", mStudentAddress);
        hashMapDataStudent.put("studentPhone", mStudentPhone);
        hashMapDataStudent.put("schoolName", mSchoolName);
        hashMapDataStudent.put("nimNis", mNimNis);
        hashMapDataStudent.put("schoolAddress", mSchoolAddress);
        hashMapDataStudent.put("majors", mMajors);
        hashMapDataStudent.put("schoolPhone", mSchoolPhone);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DataStudent");
        ref.child(editStudentId)
                .updateChildren(hashMapDataStudent)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDataStudentActivity.this, "Update Success..", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDataStudentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showImgPick() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");
        //set option to dialog
        builder.setItems(options, (dialogInterface, i) -> {
            if (i == 0) {
                //camera
                if (!checkCameraPermission()) {
                    requestCamera();
                } else {
                    pickFromCamera();
                }
            }
            if (i == 1) {
                //gallery
                if (!checkStoragePermission()) {
                    requestStorage();
                } else {
                    pickFromStorage();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromStorage() {
        //intent to pick img from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_GALLERY_ID);
    }

    private void pickFromCamera() {
        //intent to pick img from camera
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Desc");
        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, PICK_CAMERA_ID);
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enable or not
        //enabled = return true, not enable = return false
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStorage() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePerms, STORAGE_REQUEST_ID);
    }

    private boolean checkCameraPermission() {
        //check if camera permission is enable or not
        //enabled = return true, not enable = return false
        boolean resultCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean resultStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return resultCamera && resultStorage;
    }

    private void requestCamera() {
        //request runtime camera permission
        ActivityCompat.requestPermissions(this, cameraPerms, CAMERA_REQUEST_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStat();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStat();
    }

    private void checkUserStat() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            id = user.getUid();
        } else {
            startActivity(new Intent(this, ErrorActivity.class));
            finish();
        }
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_ID: {
                if (grantResults.length > 0) {
                    boolean cameraAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccept = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccept && storageAccept) {
                        pickFromCamera();
                    } else {
                        //klau permission camera/storage/keduanya ditolak
                        Toast.makeText(this, "Camera & Storage both permission are neccessary ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_ID: {
                if (grantResults.length > 0) {
                    boolean storageAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccept) {
                        pickFromCamera();
                    } else {
                        //klau permission camera/storage/keduanya ditolak
                        Toast.makeText(this, "Storage both permission are neccessary ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //dipanggil setelah picking img dari camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_GALLERY_ID) {
                //img is picked from gallery, get uri of img
                imgUri = data.getData();


                profile_img.setImageURI(imgUri);
            } else if (requestCode == PICK_CAMERA_ID) {

                profile_img.setImageURI(imgUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}