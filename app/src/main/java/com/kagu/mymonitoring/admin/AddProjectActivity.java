package com.kagu.mymonitoring.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
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
import java.io.IOException;
import java.util.HashMap;

public class AddProjectActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

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
    ImageView imgProjects;

    EditText edProjectName, edClientName, edProjectDesc, edNamePic;
    Button submit;
    TextView titleForm;
    String editProjectName, editClientName, editProjectDesc, editNamePic, editImg;
    ProgressDialog progressDialog;
    String id;
    CardView warningAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        edProjectName = findViewById(R.id.EtProjectName);
        edClientName = findViewById(R.id.EtProjectClient);
        edProjectDesc = findViewById(R.id.EtProjectDesc);
        edNamePic = findViewById(R.id.EtPicProject);
        titleForm = findViewById(R.id.titleForm);
        imgProjects = findViewById(R.id.imgProject);
        submit = findViewById(R.id.submit);
        warningAdd = findViewById(R.id.warningAdd);

        cameraPerms = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePerms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //get data
        Intent intent = getIntent();
        String isUpdate = "" + intent.getStringExtra("key");
        String editProjectId = "" + intent.getStringExtra("editProjectKey");
        if (isUpdate.equals("editProject")) {
            titleForm.setText(R.string.updateDataProject);
            submit.setText(R.string.update_data);
            warningAdd.setVisibility(View.GONE);
            loadProjectData(editProjectId);
        } else {
            titleForm.setText(R.string.addDataProject);
            submit.setText(R.string.addData);
        }

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        imgProjects.setOnClickListener(view -> {
                    showImgPick();
                }
        );
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            onSupportNavigateUp();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.pdAddData));


        submit.setOnClickListener(view -> {
            String projectName = edProjectName.getText().toString().trim();
            String clientName = edClientName.getText().toString().trim();
            String projectDesc = edProjectDesc.getText().toString().trim();
            String namePic = edNamePic.getText().toString().trim();

            if (isUpdate.equals("editProject")) {
                if (TextUtils.isEmpty(projectName)) {
                    edProjectName.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastProjectName, Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(clientName)) {
                    edClientName.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastClienName, Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(projectDesc)) {
                    edProjectDesc.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastDescProject, Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(namePic)) {
                    edNamePic.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastNamePic, Toast.LENGTH_SHORT).show();
                    return;
                }
                startUpdate(projectName,
                        clientName,
                        projectDesc,
                        namePic,
                        editProjectId);
            } else {
                if (TextUtils.isEmpty(projectName)) {
                    edProjectName.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastProjectName, Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(clientName)) {
                    edClientName.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastClienName, Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(projectDesc)) {
                    edProjectDesc.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastDescProject, Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(namePic)) {
                    edNamePic.setError(getString(R.string.errorEmpty));
                    Toast.makeText(AddProjectActivity.this, R.string.toastNamePic, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imgUri == null) {
                    addProject(
                            projectName,
                            clientName,
                            projectDesc,
                            namePic,
                            "noImage");
                } else {
                    addProject(
                            projectName,
                            clientName,
                            projectDesc,
                            namePic,
                            String.valueOf(imgUri));
                }
            }
        });

        checkUserStat();
    }

    private void loadProjectData(String editProjectId) {
        edProjectName.setEnabled(false);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ListProjects");
        //get detail of data user
        Query query = ref.orderByChild("idProject").equalTo(editProjectId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    editProjectName = "" + ds.child("projectName").getValue();
                    editClientName = "" + ds.child("projectClient").getValue();
                    editProjectDesc = "" + ds.child("projectDesc").getValue();
                    editNamePic = "" + ds.child("namePic").getValue();
                    editImg = "" + ds.child("imgProject").getValue();

                    edProjectName.setText(editProjectName);
                    edClientName.setText(editClientName);
                    edProjectDesc.setText(editProjectDesc);
                    edNamePic.setText(editNamePic);

                    //set post img
                    //if there is no image (p.Img.equals("noImg")) then hide ImageView
                    if (editImg.equals("noImg")) {
                        imgProjects.setImageResource(R.drawable.ic_add_img);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(editImg)
                                .into(imgProjects);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startUpdate(String projectName, String clientName, String projectDesc, String
            namePic, String editProjectId) {
        progressDialog.setMessage(getString(R.string.pdUpdateData));
        progressDialog.show();

        if (!editImg.equals("noImg")) {
            //with img
            updateWasWithImg(projectName, //noImg > add new Img
                    clientName,
                    projectDesc,
                    namePic,
                    editProjectId);
        } else if (imgProjects.getDrawable() != null) {
            //with img
            updateWithNowImg(projectName, //old Img > New Img
                    clientName,
                    projectDesc,
                    namePic,
                    editProjectId);
        } else {
            //without img
            updateWithoutImg(projectName, //noImg
                    clientName,
                    projectDesc,
                    namePic,
                    editProjectId);
        }
    }

    private void updateWithNowImg(String projectName, String clientName, String projectDesc, String namePic, String editProjectId) {
        //img deleted, upload new img
        //for post-img name, postId, publish-time
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Projects/" + "project_" + timestamp;

        //get img from iv
        Bitmap bitmap = ((BitmapDrawable) imgProjects.getDrawable()).getBitmap();
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
                        hashMap.put("projectName", projectName);
                        hashMap.put("imgProject", downloadUri);
                        hashMap.put("projectClient", clientName);
                        hashMap.put("projectDesc", projectDesc);
                        hashMap.put("namePic", namePic);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ListProjects");
                        reference.child(editProjectId).updateChildren(hashMap)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddProjectActivity.this, R.string.toastSuccessUpdate, Toast.LENGTH_SHORT).show();
                                    finish();
                                }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWasWithImg(String projectName, String clientName, String
            projectDesc, String namePic, String editProjectId) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImg);
        sRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //img deleted, upload new img
                    //for post-img name
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String filePathAndName = "Projects/" + "project_" + timestamp;

                    //get img from iv
                    Bitmap bitmap = ((BitmapDrawable) imgProjects.getDrawable()).getBitmap();
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
                                    hashMap.put("projectName", projectName);
                                    hashMap.put("imgProject", downloadUri);
                                    hashMap.put("projectClient", clientName);
                                    hashMap.put("projectDesc", projectDesc);
                                    hashMap.put("namePic", namePic);

                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ListProjects");
                                    reference.child(editProjectId).updateChildren(hashMap)
                                            .addOnSuccessListener(aVoid1 -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProjectActivity.this, R.string.toastSuccessUpdate, Toast.LENGTH_SHORT).show();
                                                finish();
                                            }).addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWithoutImg(String projectName, //noImg
                                  String clientName,
                                  String projectDesc,
                                  String namePic,
                                  String editProjectId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("projectName", projectName);
        hashMap.put("imgProject", "noImg");
        hashMap.put("projectClient", clientName);
        hashMap.put("projectDesc", projectDesc);
        hashMap.put("namePic", namePic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ListProjects");
        ref.child(editProjectId).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProjectActivity.this, R.string.toastSuccessUpdate, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addProject(String projectName, String clientName, String
            projectDesc, String namePic, String uri) {
        progressDialog.setMessage("Wait, add the project");
        progressDialog.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Projects/" + "project_" + timestamp;
        if (!uri.equals("noImg")) {
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
                            hashMap.put("idProject", timestamp);
                            hashMap.put("projectName", projectName);
                            hashMap.put("imgProject", downloadUri);
                            hashMap.put("projectClient", clientName);
                            hashMap.put("projectDesc", projectDesc);
                            hashMap.put("namePic", namePic);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ListProjects");
                            ref.child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProjectActivity.this, R.string.toastSuccessAdd, Toast.LENGTH_SHORT).show();
                                        //reset views make after post go to l
                                        edProjectName.setText("");
                                        edClientName.setText("");
                                        edProjectDesc.setText("");
                                        edNamePic.setText("");

                                        imgProjects.setImageURI(null);
                                        imgUri = null;
                                    }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).addOnFailureListener(e -> {
                //failed upload img
                progressDialog.dismiss();
                Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            //post without img
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("id", id);
            hashMap.put("idProject", timestamp);
            hashMap.put("projectName", projectName);
            hashMap.put("imgProject", "noImg");
            hashMap.put("projectClient", clientName);
            hashMap.put("projectDesc", projectDesc);
            hashMap.put("namePic", namePic);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ListProjects");
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddProjectActivity.this, R.string.toastSuccessAdd, Toast.LENGTH_SHORT).show();
                        //reset views make after post go to l
                        edProjectName.setText("");
                        edClientName.setText("");
                        edProjectDesc.setText("");
                        edNamePic.setText("");

                        imgProjects.setImageURI(null);
                        imgUri = null;
                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(AddProjectActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
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


                imgProjects.setImageURI(imgUri);
            } else if (requestCode == PICK_CAMERA_ID) {

                imgProjects.setImageURI(imgUri);
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