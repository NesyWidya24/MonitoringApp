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
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
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
import com.kagu.mymonitoring.student.AddDailyReportActivity;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddArticleActivity extends AppCompatActivity {
    EditText titleArticle, descArticle;
    ImageView post_imgArticle;
    Button btnPostArticle;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    //permission constants
    private static final int STORAGE_REQUEST_ID = 200;

    //img pick constants
    private static final int PICK_GALLERY_ID = 400;

    String[] storagePerms;

    //var user info
    String name, id, dp;
    TextView titleForm;
    String edit_titleArticle,
            edit_descArticle,
            edit_post_imgArticle;

    Uri imgUri = null;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

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
                    dp = "" + ds.child("imageUrl").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        titleArticle = findViewById(R.id.titleArticle);
        descArticle = findViewById(R.id.descArticle);
        titleForm = findViewById(R.id.titleForm);
        post_imgArticle = findViewById(R.id.post_imgArticle);
        btnPostArticle = findViewById(R.id.btnPostArticle);

        //get data
        Intent intent = getIntent();
        String isUpdate = "" + intent.getStringExtra("key");
        String editArticle = "" + intent.getStringExtra("editArticleKey");
        if (isUpdate.equals("editArticle")) {
            titleForm.setText(R.string.update_article);
            btnPostArticle.setText(R.string.update_data);
            loadArticle(editArticle);
        } else {
            titleForm.setText(R.string.add_article);
            btnPostArticle.setText(R.string.addData);
        }

        post_imgArticle.setOnClickListener(view -> showImgPick());

        btnPostArticle.setOnClickListener(view -> {
            String mTitleInfo = titleArticle.getText().toString();
            String mDescInformation = descArticle.getText().toString().trim();

            if (isUpdate.equals("editArticle")) {
                if (TextUtils.isEmpty(mTitleInfo)) {
                    titleArticle.setError("Fields are required");
                    Toast.makeText(AddArticleActivity.this, "Enter Title...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescInformation)) {
                    descArticle.setError("Fields are required");
                    Toast.makeText(AddArticleActivity.this, "Enter Description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                startUpdate(mTitleInfo,
                        mDescInformation,
                        editArticle);
            } else {
                if (TextUtils.isEmpty(mTitleInfo)) {
                    titleArticle.setError("Fields are required");
                    Toast.makeText(AddArticleActivity.this, "Enter Title...", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(mDescInformation)) {
                    descArticle.setError("Fields are required");
                    Toast.makeText(AddArticleActivity.this, "Enter Description...", Toast.LENGTH_SHORT).show();
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
    }

    private void startUpdate(String mTitleInfo, String mDescInformation, String editArticle) {
        progressDialog.setMessage("Updating Data...");
        progressDialog.show();

        if (!edit_post_imgArticle.equals("noImg")) {
            //with img
            updateWasWithImg(mTitleInfo,
                    mDescInformation,
                    editArticle);
        } else if (post_imgArticle.getDrawable() != null) {
            //with img
            updateWithNowImg(mTitleInfo,
                    mDescInformation,
                    editArticle);
        } else {
            //without img
            updateWithoutImg(mTitleInfo,
                    mDescInformation,
                    editArticle);
        }

    }

    private void updateWithoutImg(String mTitleInfo, String mDescInformation, String editArticle) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pTitleArticle", mTitleInfo);
        hashMap.put("pDescArticle", mDescInformation);
        hashMap.put("postImg", "noImg");

        //path to store post data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Article");
        ref.child(editArticle).updateChildren(hashMap)
                .addOnSuccessListener(aVoid1 -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddArticleActivity.this, "Article Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateWithNowImg(String mTitleInfo, String mDescInformation, String editArticle) {
        //img deleted, upload new img
        //for post-img name, postId, publish-time
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Article/" + "article_" + timestamp;

        //get img from iv
        Bitmap bitmap = ((BitmapDrawable) post_imgArticle.getDrawable()).getBitmap();
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
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Article");
                        reference.child(editArticle).updateChildren(hashMap)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddArticleActivity.this, "Article Updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWasWithImg(String mTitleInfo, String mDescInformation, String editArticle) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(edit_post_imgArticle);
        sRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //img deleted, upload new img
                    //for post-img name
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String filePathAndName = "Article/" + "article_" + timestamp;

                    //get img from iv
                    Bitmap bitmap = ((BitmapDrawable) post_imgArticle.getDrawable()).getBitmap();
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
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Article");
                                    reference.child(editArticle).updateChildren(hashMap)
                                            .addOnSuccessListener(aVoid1 -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddArticleActivity.this, "Article Updated", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }).addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadArticle(String editArticle) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Article");

        Query query = ref.orderByChild("postId").equalTo(editArticle);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    edit_titleArticle = "" + ds.child("pTitleArticle").getValue();
                    edit_descArticle = "" + ds.child("pDescArticle").getValue();
                    edit_post_imgArticle = "" + ds.child("postImg").getValue();

                    titleArticle.setText(edit_titleArticle);
                    descArticle.setText(edit_descArticle);

                    //set post img
                    //if there is no image (p.Img.equals("noImg")) then hide ImageView
                    if (edit_post_imgArticle.equals("noImg")) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_no_img).into(post_imgArticle);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(edit_post_imgArticle)
                                .into(post_imgArticle);
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
        String filePathAndName = "Article/" + "article_" + timestamp;
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
                            hashMap.put("uDp", dp);
                            hashMap.put("postId", timestamp);
                            hashMap.put("pTitleArticle", mTitleInfo);
                            hashMap.put("pDescArticle", mDescInformation);
                            hashMap.put("postImg", downloadUri);
                            hashMap.put("postTime", timestamp);

                            //path to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Article");
                            ref.child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddArticleActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        //reset views make after post go to list post
                                        titleArticle.setText("");
                                        descArticle.setText("");

                                        post_imgArticle.setImageURI(null);
                                        imgUri = null;
                                    }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).addOnFailureListener(e -> {
                //failed upload img
                progressDialog.dismiss();
                Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            //post without img
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("id", id);
            hashMap.put("uName", name);
            hashMap.put("uDp", dp);
            hashMap.put("postId", timestamp);
            hashMap.put("pTitleArticle", mTitleInfo);
            hashMap.put("pDescArticle", mDescInformation);
            hashMap.put("postImg", "noImg");
            hashMap.put("postTime", timestamp);

            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Article");
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddArticleActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                        //reset views make after post go to list post
                        titleArticle.setText("");
                        descArticle.setText("");

                        post_imgArticle.setImageURI(null);
                        imgUri = null;
                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(AddArticleActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.add_post).setVisible(false);
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.logout).setVisible(false);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //dipanggil setelah picking img dari camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_GALLERY_ID) {
                //img is picked from gallery, get uri of img
                imgUri = data.getData();

                //set to ImageView
                post_imgArticle.setImageURI(imgUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}