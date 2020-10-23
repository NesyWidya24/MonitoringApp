package com.kagu.mymonitoring.student;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddDailyReportActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    //permission constants
    private static final int STORAGE_REQUEST_ID = 200;

    //img pick constants
    private static final int PICK_GALLERY_ID = 400;

    String[] storagePerms;

    EditText linkDocsPost,
            code_module,
            title_module,
            code_scenario,
            title_scenario,
            codeTestCase,
            descTest,
            expected,
            descResult,
            titleTestCase,
            reproduce,
            inputNote;

    TextView titleForm;

    String edit_linkDocsPost,
            edit_code_module,
            edit_title_module,
            edit_code_scenario,
            edit_title_scenario,
            edit_codeTestCase,
            edit_descTest,
            edit_expected,
            edit_descResult,
            edit_titleTestCase,
            edit_reproduce,
            edit_inputNote,
            edit_img,
            edit_resultTest;

    TextInputLayout inputReproduce, inputDescResult;

    ImageView imgPost;
    Button uploadPost;
    Spinner resultTest; //data lokal

    //var user info
    String name, email, id, projectName;

    Uri imgUri = null;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_report);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();


        storagePerms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStat();

        //get beberapa info dari node Users untuk dimasukkan ke node post
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("fullname").getValue();
                    email = "" + ds.child("email").getValue();
                    projectName = "" + ds.child("projectName").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        linkDocsPost = findViewById(R.id.linkDocsPost);
        code_module = findViewById(R.id.code_module);
        title_module = findViewById(R.id.title_module);
        code_scenario = findViewById(R.id.code_scenario);
        title_scenario = findViewById(R.id.title_scenario);
        codeTestCase = findViewById(R.id.codeTestCase);
        descTest = findViewById(R.id.descTest);
        expected = findViewById(R.id.expected);
        inputNote = findViewById(R.id.note);
        imgPost = findViewById(R.id.post_img);
        uploadPost = findViewById(R.id.btnPost);
        resultTest = findViewById(R.id.result);
        titleTestCase = findViewById(R.id.titleTestCase);
        descResult = findViewById(R.id.descResult);
        reproduce = findViewById(R.id.reproduce);
        inputDescResult = findViewById(R.id.inputDescResult);
        inputReproduce = findViewById(R.id.inputReproduce);
        titleForm = findViewById(R.id.titleForm);

        //get data
        Intent intent = getIntent();
        String isUpdate = "" + intent.getStringExtra("key");
        String editReport = "" + intent.getStringExtra("editReportKey");
        if (isUpdate.equals("editReport")) {
            titleForm.setText(R.string.update_report);
            uploadPost.setText(R.string.update_data);
            loadReport(editReport);
        } else {
            titleForm.setText(R.string.createReport);
            uploadPost.setText(R.string.addData);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.resultTest, android.R.layout.simple_spinner_dropdown_item);
        resultTest.setAdapter(adapter);

        resultTest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //setting spinner for show and hide tergantung value nya
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String result = resultTest.getSelectedItem().toString();
                ((TextView)adapterView.getChildAt(0)).setTextSize(15);

                if (result.equals("Success")) {
                    inputDescResult.setVisibility(View.VISIBLE);
                    descResult.setText("As expected");
                    descResult.setEnabled(false);
                    imgPost.setVisibility(View.INVISIBLE);
                    inputReproduce.setVisibility(View.INVISIBLE);
                }
                if (result.equals("Failed")) {
//                    descResult.setText(""); //klau di ilangin show data for upload, klau di pakai data for upload gaada tapi pas add dari success ganti ke failed As Expected masih ada
                    inputDescResult.setVisibility(View.VISIBLE);
                    descResult.setEnabled(true);
                    imgPost.setVisibility(View.VISIBLE);
                    inputReproduce.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imgPost.setOnClickListener(view -> showImgPick());

        uploadPost.setOnClickListener(view -> {
            String mLinkDocsPost = linkDocsPost.getText().toString().trim();
            String mCode_module = code_module.getText().toString().trim();
            String mTitle_module = title_module.getText().toString().trim();
            String mCode_scenario = code_scenario.getText().toString().trim();
            String mTitle_scenario = title_scenario.getText().toString().trim();
            String mCodeTestCase = codeTestCase.getText().toString().trim();
            String mDescTest = descTest.getText().toString().trim();
            String mExpected = expected.getText().toString().trim();
            String mTitleTestCase = titleTestCase.getText().toString().trim();
            String mInputNote = inputNote.getText().toString().trim();
            String mResult = resultTest.getSelectedItem().toString();
            String mDescResult = descResult.getText().toString();
            String mReproduce = reproduce.getText().toString().trim();

            if (isUpdate.equals("editReport")) {
                if (TextUtils.isEmpty(mLinkDocsPost)) {
                    linkDocsPost.setError("Fields are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Link Documents...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mCode_module)) {
                    title_module.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Code Module...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mTitle_module)) {
                    code_scenario.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Title Module...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mCode_scenario)) {
                    code_scenario.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Code Scenario...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mTitle_scenario)) {
                    title_scenario.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Title Scenario...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mCodeTestCase)) {
                    codeTestCase.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Code Test Case...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescTest)) {
                    descTest.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Description...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mExpected)) {
                    expected.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Expected...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (mResult.equals("-Choose Result-")) {
                    Toast.makeText(AddDailyReportActivity.this, "Please input Result", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescResult)) {
                    descResult.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Description Result...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mTitleTestCase)) {
                    titleTestCase.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Title Test Case...", Toast.LENGTH_SHORT).show();
                    return;

                }
                startUpdate(mLinkDocsPost,
                        mCode_module,
                        mTitle_module,
                        mCode_scenario,
                        mTitle_scenario,
                        mCodeTestCase,
                        mDescTest,
                        mExpected,
                        mTitleTestCase,
                        mInputNote,
                        mResult,
                        mDescResult,
                        mReproduce,
                        editReport);
            } else {
                if (TextUtils.isEmpty(mLinkDocsPost)) {
                    linkDocsPost.setError("Fields are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Link Documents...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mCode_module)) {
                    title_module.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Code Module...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mTitle_module)) {
                    code_scenario.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Title Module...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mCode_scenario)) {
                    code_scenario.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Code Scenario...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mTitle_scenario)) {
                    title_scenario.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Title Scenario...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mCodeTestCase)) {
                    codeTestCase.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Code Test Case...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescTest)) {
                    descTest.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Description...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mExpected)) {
                    expected.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Expected...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (mResult.equals("-Choose Result-")) {
                    Toast.makeText(AddDailyReportActivity.this, "Please input Result", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescResult)) {
                    descResult.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Description Result...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mTitleTestCase)) {
                    titleTestCase.setError("Field are required");
                    Toast.makeText(AddDailyReportActivity.this, "Enter Title Test Case...", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (mResult.equals("Failed")) {
                    if (TextUtils.isEmpty(mReproduce)) {
                        reproduce.setError("Fields are required");
                        Toast.makeText(AddDailyReportActivity.this, "Enter Reproduce please...", Toast.LENGTH_SHORT).show();
                    }
                }

                if (mResult.equals("Success")) {
                    if (imgUri == null) {
                        uploadData(mLinkDocsPost,
                                mCode_module,
                                mTitle_module,
                                mCode_scenario,
                                mTitle_scenario,
                                mCodeTestCase,
                                mDescTest,
                                mExpected,
                                mTitleTestCase,
                                mInputNote,
                                mResult,
                                mDescResult,
                                mReproduce,
                                "noImage");
                    }
                }
                if (mResult.equals("Failed")) {
                    if (imgUri != null) {
                        uploadData(mLinkDocsPost,
                                mCode_module,
                                mTitle_module,
                                mCode_scenario,
                                mTitle_scenario,
                                mCodeTestCase,
                                mDescTest,
                                mExpected,
                                mTitleTestCase,
                                mInputNote,
                                mResult,
                                mDescResult,
                                mReproduce,
                                String.valueOf(imgUri));
                    } else {
                        Toast.makeText(this, R.string.toast_needPict, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
    }

    private void startUpdate(String mLinkDocsPost,
                             String mCode_module,
                             String mTitle_module,
                             String mCode_scenario,
                             String mTitle_scenario,
                             String mCodeTestCase,
                             String mDescTest,
                             String mExpected,
                             String mTitleTestCase,
                             String mInputNote,
                             String mResult,
                             String mDescResult,
                             String mReproduce,
                             String editReport) {

        progressDialog.setMessage("Updating Data...");
        progressDialog.show();

        if (!edit_img.equals("noImg")) {
            //with img
            updateWasWithImg(mLinkDocsPost,
                    mCode_module,
                    mTitle_module,
                    mCode_scenario,
                    mTitle_scenario,
                    mCodeTestCase,
                    mDescTest,
                    mExpected,
                    mTitleTestCase,
                    mInputNote,
                    mResult,
                    mDescResult,
                    mReproduce,
                    editReport);
        } else if (imgPost.getDrawable() != null ||edit_img.equals("noImg")) {
            //with img
            updateWithNowImg(mLinkDocsPost,
                    mCode_module,
                    mTitle_module,
                    mCode_scenario,
                    mTitle_scenario,
                    mCodeTestCase,
                    mDescTest,
                    mExpected,
                    mTitleTestCase,
                    mInputNote,
                    mResult,
                    mDescResult,
                    mReproduce,
                    editReport);
        } else {
            //without img
            updateWithoutImg(mLinkDocsPost,
                    mCode_module,
                    mTitle_module,
                    mCode_scenario,
                    mTitle_scenario,
                    mCodeTestCase,
                    mDescTest,
                    mExpected,
                    mTitleTestCase,
                    mInputNote,
                    mResult,
                    mDescResult,
                    mReproduce,
                    editReport);
        }

    }

    private void updateWithoutImg(String mLinkDocsPost, String mCode_module, String mTitle_module, String mCode_scenario, String mTitle_scenario, String mCodeTestCase, String mDescTest, String mExpected, String mTitleTestCase, String mInputNote, String mResult, String mDescResult, String mReproduce, String editReport) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pLinkDocs", mLinkDocsPost);
        hashMap.put("pCodeModule", mCode_module);
        hashMap.put("pTitleModule", mTitle_module);
        hashMap.put("pCodeScenario", mCode_scenario);
        hashMap.put("pTitleScenario", mTitle_scenario);
        hashMap.put("pCodeTC", mCodeTestCase);
        hashMap.put("pDescTest", mDescTest);
        hashMap.put("pExpected", mExpected);
        hashMap.put("pTitleTC", mTitleTestCase);
        hashMap.put("pDescResult", mDescResult);
        hashMap.put("pReproduce", mReproduce);
        hashMap.put("pNote", mInputNote);
        hashMap.put("pResult", mResult);
        hashMap.put("postImg", "noImg");
        ;

        //path to store post data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
        reference.child(editReport).updateChildren(hashMap)
                .addOnSuccessListener(aVoid1 -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDailyReportActivity.this, "Daily Report Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateWithNowImg(String mLinkDocsPost, String mCode_module, String mTitle_module, String mCode_scenario, String mTitle_scenario, String mCodeTestCase, String mDescTest, String mExpected, String mTitleTestCase, String mInputNote, String mResult, String mDescResult, String mReproduce, String editReport) {
        //img deleted, upload new img
        //for post-img name, reportId, publish-time
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "DailyReport/" + "report_" + timestamp;

        //get img from iv
        Bitmap bitmap = ((BitmapDrawable) imgPost.getDrawable()).getBitmap();
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

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("pLinkDocs", mLinkDocsPost);
                        hashMap.put("pCodeModule", mCode_module);
                        hashMap.put("pTitleModule", mTitle_module);
                        hashMap.put("pCodeScenario", mCode_scenario);
                        hashMap.put("pTitleScenario", mTitle_scenario);
                        hashMap.put("pCodeTC", mCodeTestCase);
                        hashMap.put("pDescTest", mDescTest);
                        hashMap.put("pExpected", mExpected);
                        hashMap.put("pTitleTC", mTitleTestCase);
                        hashMap.put("pDescResult", mDescResult);
                        hashMap.put("pReproduce", mReproduce);
                        hashMap.put("pNote", mInputNote);
                        hashMap.put("pResult", mResult);
                        hashMap.put("postImg", downloadUri);
                        hashMap.put("postTime", timestamp);

                        //path to store post data
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
                        reference.child(editReport).updateChildren(hashMap)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddDailyReportActivity.this, "Daily Report Updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWasWithImg(String mLinkDocsPost, String mCode_module, String mTitle_module, String mCode_scenario, String mTitle_scenario, String mCodeTestCase, String mDescTest, String mExpected, String mTitleTestCase, String mInputNote, String mResult, String mDescResult, String mReproduce, String editReport) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(edit_img);
        sRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //img deleted, upload new img
                    //for post-img name
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String filePathAndName = "DailyReport/" + "report_" + timestamp;

                    //get img from iv
                    Bitmap bitmap = ((BitmapDrawable) imgPost.getDrawable()).getBitmap();
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

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("pLinkDocs", mLinkDocsPost);
                                    hashMap.put("pCodeModule", mCode_module);
                                    hashMap.put("pTitleModule", mTitle_module);
                                    hashMap.put("pCodeScenario", mCode_scenario);
                                    hashMap.put("pTitleScenario", mTitle_scenario);
                                    hashMap.put("pCodeTC", mCodeTestCase);
                                    hashMap.put("pDescTest", mDescTest);
                                    hashMap.put("pExpected", mExpected);
                                    hashMap.put("pTitleTC", mTitleTestCase);
                                    hashMap.put("pDescResult", mDescResult);
                                    hashMap.put("pReproduce", mReproduce);
                                    hashMap.put("pNote", mInputNote);
                                    hashMap.put("pResult", mResult);
                                    hashMap.put("postImg", downloadUri);

                                    //path to store post data
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport");
                                    reference.child(editReport).updateChildren(hashMap)
                                            .addOnSuccessListener(aVoid1 -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddDailyReportActivity.this, "Daily Report Updated", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }).addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReport(String editReport) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DailyReport");

        Query query = ref.orderByChild("reportId").equalTo(editReport);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    edit_linkDocsPost = "" + ds.child("pLinkDocs").getValue();
                    edit_code_module = "" + ds.child("pCodeModule").getValue();
                    edit_title_module = "" + ds.child("pTitleModule").getValue();
                    edit_code_scenario = "" + ds.child("pCodeScenario").getValue();
                    edit_title_scenario = "" + ds.child("pTitleScenario").getValue();
                    edit_codeTestCase = "" + ds.child("pCodeTC").getValue();
                    edit_descTest = "" + ds.child("pDescTest").getValue();
                    edit_expected = "" + ds.child("pExpected").getValue();
                    edit_descResult = "" + ds.child("pDescResult").getValue();
                    edit_titleTestCase = "" + ds.child("pTitleTC").getValue();
                    edit_reproduce = "" + ds.child("pReproduce").getValue();
                    edit_inputNote = "" + ds.child("pNote").getValue();
                    edit_resultTest = "" + ds.child("pResult").getValue();
                    edit_img = "" + ds.child("postImg").getValue();

                    linkDocsPost.setText(edit_linkDocsPost);
                    code_module.setText(edit_code_module);
                    title_module.setText(edit_title_module);
                    code_scenario.setText(edit_code_scenario);
                    title_scenario.setText(edit_title_scenario);
                    codeTestCase.setText(edit_codeTestCase);
                    descTest.setText(edit_descTest);
                    expected.setText(edit_expected);
                    titleTestCase.setText(edit_titleTestCase);
                    inputNote.setText(edit_inputNote);
                    descResult.setText(edit_descResult);
                    reproduce.setText(edit_reproduce);

                    //set post img
                    //if there is no image (p.Img.equals("noImg")) then hide ImageView
                    if (edit_img.equals("noImg")) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_no_img).into(imgPost);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(edit_img)
                                .into(imgPost);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void uploadData(String mLinkDocsPost,
                            String mCode_module,
                            String mTitle_module,
                            String mCode_scenario,
                            String mTitle_scenario,
                            String mCodeTestCase,
                            String mDescTest,
                            String mExpected,
                            String mTitleTestCase,
                            String mInputNote,
                            String mResult,
                            String mDescResult,
                            String mReproduce,
                            String uri) {
        progressDialog.setMessage("Wait, publishing post...");
        progressDialog.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "DailyReport/" + "report_" + timestamp;
        if (!uri.equals("noImage")) {
            //proses post with img
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            reference.putFile(Uri.parse(uri))
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("id", id);
                            hashMap.put("username", name);
                            hashMap.put("reportId", timestamp);
                            hashMap.put("pProjectName", projectName);
                            hashMap.put("pLinkDocs", mLinkDocsPost);
                            hashMap.put("pCodeModule", mCode_module);
                            hashMap.put("pTitleModule", mTitle_module);
                            hashMap.put("pCodeScenario", mCode_scenario);
                            hashMap.put("pTitleScenario", mTitle_scenario);
                            hashMap.put("pCodeTC", mCodeTestCase);
                            hashMap.put("pDescTest", mDescTest);
                            hashMap.put("pExpected", mExpected);
                            hashMap.put("pTitleTC", mTitleTestCase);
                            hashMap.put("pDescResult", mDescResult);
                            hashMap.put("pReproduce", mReproduce);
                            hashMap.put("pNote", mInputNote);
                            hashMap.put("pResult", mResult);
                            hashMap.put("postImg", downloadUri);
                            hashMap.put("postTime", timestamp);

                            //path to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DailyReport");
                            ref.child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddDailyReportActivity.this, R.string.post_publised, Toast.LENGTH_SHORT).show();
                                        //reset views make after post go to l

                                        linkDocsPost.setText("");
                                        code_module.setText("");
                                        title_module.setText("");
                                        code_scenario.setText("");
                                        title_scenario.setText("");
                                        codeTestCase.setText("");
                                        descTest.setText("");
                                        expected.setText("");
                                        titleTestCase.setText("");
                                        descResult.setText("");
                                        reproduce.setText("");
                                        inputNote.setText("");
                                        resultTest.setSelection(0);

                                        imgPost.setImageURI(null);
                                        imgUri = null;
                                    }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).addOnFailureListener(e -> {
                //failed upload img
                progressDialog.dismiss();
                Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            //post without img
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("id", id);
            hashMap.put("username", name);
            hashMap.put("reportId", timestamp);
            hashMap.put("pLinkDocs", mLinkDocsPost);
            hashMap.put("pProjectName", projectName);
            hashMap.put("pCodeModule", mCode_module);
            hashMap.put("pTitleModule", mTitle_module);
            hashMap.put("pCodeScenario", mCode_scenario);
            hashMap.put("pTitleScenario", mTitle_scenario);
            hashMap.put("pCodeTC", mCodeTestCase);
            hashMap.put("pTitleTC", mTitleTestCase);
            hashMap.put("pDescTest", mDescTest);
            hashMap.put("pExpected", mExpected);
            hashMap.put("pDescResult", mDescResult);
            hashMap.put("pReproduce", mReproduce);
            hashMap.put("pNote", mInputNote);
            hashMap.put("pResult", mResult);
            hashMap.put("postImg", "noImg");
            hashMap.put("postTime", timestamp);

            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DailyReport");
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddDailyReportActivity.this, R.string.post_publised, Toast.LENGTH_SHORT).show();
                        //reset views make after post go to list post
                        linkDocsPost.setText("");
                        code_module.setText("");
                        title_module.setText("");
                        code_scenario.setText("");
                        title_scenario.setText("");
                        codeTestCase.setText("");
                        descTest.setText("");
                        expected.setText("");
                        titleTestCase.setText("");
                        descResult.setText("");
                        reproduce.setText("");
                        inputNote.setText("");
                        resultTest.setSelection(0);
                        imgPost.setImageURI(null);
                        imgUri = null;
                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(AddDailyReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showImgPick() {
        //gallery
        if (!checkStoragePermission()) {
            requestStorage();
        } else {
            pickFromStorage();
        }
    }

    private void pickFromStorage() {
        //intent to pick img from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_GALLERY_ID);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
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

        if (requestCode == STORAGE_REQUEST_ID) {
            if (grantResults.length > 0) {
                boolean storageAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccept) {
                    pickFromStorage();
                } else {
                    //klau permission camera/storage/keduanya ditolak
                    Toast.makeText(this, "Storage both permission are neccessary ...", Toast.LENGTH_SHORT).show();
                }}
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //dipanggil setelah picking img dari camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_GALLERY_ID) {
                //img is picked from gallery, get uri of img
                imgUri = data.getData();

                //set to ImageView
                imgPost.setImageURI(imgUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.askCloseActivity)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    AddDailyReportActivity.this.finish();
                }).setNegativeButton(R.string.no, (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

