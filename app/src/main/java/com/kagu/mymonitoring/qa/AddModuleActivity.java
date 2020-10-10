package com.kagu.mymonitoring.qa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddModuleActivity extends AppCompatActivity {
    EditText titleModule, descModule;
    ImageView post_imgModule;
    Button btnPostModule;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    //permission constants
    private static final int STORAGE_REQUEST_ID = 200;

    //img pick constants
    private static final int PICK_GALLERY_ID = 400;
    String[] storagePerms;

    //var user info
    String name, id;
    TextView titleForm;
    String edit_titleModule,
            edit_descModule,
            edit_post_imgModule;

    Uri imgUri = null;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_module);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        storagePerms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStat();

        //get beberapa info dari node Users untuk dimasukkan ke node post
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("id").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("fullname").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        titleModule = findViewById(R.id.titleArticle);
        descModule = findViewById(R.id.descModule);
        titleForm = findViewById(R.id.titleForm);
        post_imgModule = findViewById(R.id.post_imgArticle);
        btnPostModule = findViewById(R.id.btnPostArticle);

        //get data
        Intent intent = getIntent();
        String isUpdate = "" + intent.getStringExtra("key");
        String editModule = "" + intent.getStringExtra("editModuleKey");
        if (isUpdate.equals("editModule")) {
            titleForm.setText(R.string.update_module);
            btnPostModule.setText(R.string.update_data);
            loadArticle(editModule);
        } else {
            titleForm.setText(R.string.add_module);
            btnPostModule.setText(R.string.addData);
        }

        post_imgModule.setOnClickListener(view -> showImgPick());

        btnPostModule.setOnClickListener(view -> {
            String mTitleInfo = titleModule.getText().toString();
            String mDescInformation = descModule.getText().toString().trim();

            if (isUpdate.equals("editModule")) {
                if (TextUtils.isEmpty(mTitleInfo)) {
                    titleModule.setError("Fields are required");
                    Toast.makeText(AddModuleActivity.this, "Enter Title...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescInformation)) {
                    descModule.setError("Fields are required");
                    Toast.makeText(AddModuleActivity.this, "Enter Description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                startUpdate(mTitleInfo,
                        mDescInformation,
                        editModule);
            } else {
                if (TextUtils.isEmpty(mTitleInfo)) {
                    titleModule.setError("Fields are required");
                    Toast.makeText(AddModuleActivity.this, "Enter Title...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescInformation)) {
                    descModule.setError("Fields are required");
                    Toast.makeText(AddModuleActivity.this, "Enter Description...", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (imgUri == null) {
                    uploadData(mTitleInfo,
                            mDescInformation,
                            "noImage");
                } else {
                    uploadData(mTitleInfo,
                            mDescInformation,
                            String.valueOf(imgUri));
                }
            }
        });
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });
    }

    private void startUpdate(String mTitleInfo, String mDescInformation, String editModule) {
        progressDialog.setMessage("Updating Data...");
        progressDialog.show();

        if (!edit_post_imgModule.equals("noImg")) {
            //with img
            updateWasWithImg(mTitleInfo,
                    mDescInformation,
                    editModule);
        } else if (post_imgModule.getDrawable() != null) {
            //with img
            updateWithNowImg(mTitleInfo,
                    mDescInformation,
                    editModule);
        } else {
            //without img
            updateWithoutImg(mTitleInfo,
                    mDescInformation,
                    editModule);
        }
    }

    private void updateWithoutImg(String mTitleInfo, String mDescInformation, String editModule) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pTitleArticle", mTitleInfo);
        hashMap.put("pDescArticle", mDescInformation);
        hashMap.put("postImg", "noImg");

        //path to store post data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ModuleLearn");
        ref.child(editModule).updateChildren(hashMap)
                .addOnSuccessListener(aVoid1 -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddModuleActivity.this, "Article Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateWithNowImg(String mTitleInfo, String mDescInformation, String editModule) {
        //img deleted, upload new img
        //for post-img name, moduleId, publish-time
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "ModuleLearn/" + "moduleLearn_" + timestamp;

        //get img from iv
        Bitmap bitmap = ((BitmapDrawable) post_imgModule.getDrawable()).getBitmap();
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
                        hashMap.put("pTitleArticle", mTitleInfo);
                        hashMap.put("pDescArticle", mDescInformation);
                        hashMap.put("postImg", downloadUri);

                        //path to store post data
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ModuleLearn");
                        reference.child(editModule).updateChildren(hashMap)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddModuleActivity.this, "Article Updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWasWithImg(String mTitleInfo, String mDescInformation, String editModule) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(edit_post_imgModule);
        sRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //img deleted, upload new img
                    //for post-img name
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String filePathAndName = "ModuleLearn/" + "moduleLearn_" + timestamp;

                    //get img from iv
                    Bitmap bitmap = ((BitmapDrawable) post_imgModule.getDrawable()).getBitmap();
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
                                    hashMap.put("pTitleArticle", mTitleInfo);
                                    hashMap.put("pDescArticle", mDescInformation);
                                    hashMap.put("postImg", downloadUri);

                                    //path to store post data
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ModuleLearn");
                                    reference.child(editModule).updateChildren(hashMap)
                                            .addOnSuccessListener(aVoid1 -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddModuleActivity.this, "Article Updated", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }).addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadArticle(String editModule) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ModuleLearn");

        Query query = ref.orderByChild("moduleId").equalTo(editModule);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    edit_titleModule = "" + ds.child("pTitleArticle").getValue();
                    edit_descModule = "" + ds.child("pDescArticle").getValue();
                    edit_post_imgModule = "" + ds.child("postImg").getValue();

                    titleModule.setText(edit_titleModule);
                    descModule.setText(edit_descModule);

                    //set post img
                    //if there is no image (p.Img.equals("noImg")) then hide ImageView
                    if (edit_post_imgModule.equals("noImg")) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_no_img).into(post_imgModule);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(edit_post_imgModule)
                                .into(post_imgModule);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(String mTitleInfo,
                            String mDescInformation,
                            String uri) {
        progressDialog.setMessage("Wait, publishing post...");
        progressDialog.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "ModuleLearn/" + "moduleLearn_" + timestamp;
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
                            hashMap.put("uName", name);
                            hashMap.put("moduleId", timestamp);
                            hashMap.put("pTitleArticle", mTitleInfo);
                            hashMap.put("pDescArticle", mDescInformation);
                            hashMap.put("postImg", downloadUri);
                            hashMap.put("postTime", timestamp);

                            //path to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ModuleLearn");
                            ref.child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddModuleActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        //reset views make after post go to list post
                                        titleModule.setText("");
                                        descModule.setText("");

                                        post_imgModule.setImageURI(null);
                                        imgUri = null;
                                    }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).addOnFailureListener(e -> {
                //failed upload img
                progressDialog.dismiss();
                Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            //post without img
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("id", id);
            hashMap.put("uName", name);
            hashMap.put("moduleId", timestamp);
            hashMap.put("pTitleArticle", mTitleInfo);
            hashMap.put("pDescArticle", mDescInformation);
            hashMap.put("postImg", "noImg");
            hashMap.put("postTime", timestamp);

            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ModuleLearn");
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddModuleActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                        //reset views make after post go to list post
                        titleModule.setText("");
                        descModule.setText("");

                        post_imgModule.setImageURI(null);
                        imgUri = null;
                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(AddModuleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                post_imgModule.setImageURI(imgUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}