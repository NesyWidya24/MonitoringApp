package com.kagu.mymonitoring.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;
import com.kagu.mymonitoring.Log_inActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.StartActivity;
import com.kagu.mymonitoring.adapter.MsgAdapter;
import com.kagu.mymonitoring.entity.Chat;
import com.kagu.mymonitoring.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView profile;
    TextView username, userStat;
    EditText msgEt;
    ImageButton sendBtn, attachBtn;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefSeen;

    //permission constants
    private static final int CAMERA_REQUEST_ID = 100;
    private static final int STORAGE_REQUEST_ID = 200;

    //img pick constants
    private static final int PICK_CAMERA_ID = 300;
    private static final int PICK_GALLERY_ID = 400;

    //permission array
    String[] cameraPerms;
    String[] storagePerms;

    //    image picked will be saved in this uri
    Uri imgUri = null;

    List<Chat> chats;
    MsgAdapter msgAdapter;

    String hisId;
    String myId;
    String hisImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //
        cameraPerms = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePerms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        recyclerView = findViewById(R.id.chat_rv);
        profile = findViewById(R.id.profileChat);
        username = findViewById(R.id.usernameChat);
        userStat = findViewById(R.id.userStat);
        msgEt = findViewById(R.id.msgEt);
        sendBtn = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisId = intent.getStringExtra("userid");

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");

        Query query = userDbRef.orderByChild("id").equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("fullname").getValue();
                    hisImg = "" + ds.child("imageUrl").getValue();
                    String typingStat = "" + ds.child("typingTo").getValue();

                    if (typingStat.equals(myId)) {
                        userStat.setText("typing...");
                    } else {
                        String status = "" + ds.child("onlineStat").getValue();
                        if (status.equals("online")) {
                            userStat.setText(status);
                        } else if (status.equals("offline")) {
                            userStat.setText(status);
                        } else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(status));
                            String dateTime = DateFormat.format("dd MMM yyyy hh:mm aa", calendar).toString(); //hh:mm am/pm
                            userStat.setText("Last seen at " + dateTime);
                        }
                    }

                    username.setText(name);
                    try {
                        Glide.with(getApplicationContext()).load(hisImg).placeholder(R.mipmap.ic_launcher).into(profile);
                    } catch (Exception e) {
                        Glide.with(getApplicationContext()).load(R.mipmap.ic_launcher).into(profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendBtn.setOnClickListener(view -> {
            String msg = msgEt.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(MessageActivity.this, "Cannot send empty message..", Toast.LENGTH_SHORT).show();
            } else {
                sendMsg(msg);
            }
            msgEt.setText("");
        });

//        click btn to import image
        attachBtn.setOnClickListener(view ->
                showImgPick()
        );

        msgEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0) {
                    checkTypingStat("noOne");
                } else {
                    checkTypingStat(hisId);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readMsg();

        seenMsg();

    }

    private void seenMsg() {
        userRefSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && (chat.getSender().equals(hisId))) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMsg() {
        chats = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(hisId) ||
                            (chat.getReceiver().equals(hisId) && chat.getSender().equals(myId))) {
                        chats.add(chat);
                    }
                    msgAdapter = new MsgAdapter(MessageActivity.this, chats, hisImg);
                    msgAdapter.notifyDataSetChanged();

                    recyclerView.setAdapter(msgAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMsg(final String msg) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String key = reference.child("Chats").push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myId);
        hashMap.put("receiver", hisId);
        hashMap.put("message", msg);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        hashMap.put("key", key);

        if (key != null)
        reference.child("Chats").child(key).setValue(hashMap);

        final DatabaseReference refChat1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myId)
                .child(hisId);
        refChat1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    refChat1.child("id").setValue(hisId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference refChat2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisId)
                .child(myId);
        refChat2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    refChat2.child("id").setValue(myId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sendImgMsg(Uri imgUri) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Image..");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatsImg/" + "post_" + timestamp;
        //chats node will be created that will contain all image send via chat
//        get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray(); //convert img to bytes
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        reference.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    //img upload
                    progressDialog.dismiss();
                    //get url of uploaded image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String downloadUri = uriTask.getResult().toString();
                    if (uriTask.isSuccessful()) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                        String key = ref.child("Chats").push().getKey();
                        //setup required data

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", myId);
                        hashMap.put("receiver", hisId);
                        hashMap.put("message", downloadUri);
                        hashMap.put("timestamp", timestamp);
                        hashMap.put("isSeen", false);
                        hashMap.put("type", "image");
                        hashMap.put("key", key);

                        if (key != null)
                            ref.child("Chats").child(key).setValue(hashMap);

                        final DatabaseReference refChat1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                .child(myId)
                                .child(hisId);
                        refChat1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    refChat1.child("id").setValue(hisId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        final DatabaseReference refChat2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                .child(hisId)
                                .child(myId);
                        refChat2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    refChat2.child("id").setValue(myId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

    private void checkUserStatus() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            myId = firebaseUser.getUid();
        } else {
            startActivity(new Intent(this, Log_inActivity.class));
            finish();
        }
    }

    private void checkOnlineStat(String stat) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStat", stat);

        reference.updateChildren(hashMap);
    }

    private void checkTypingStat(String typing) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStat("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timestamp = String.valueOf(System.currentTimeMillis());
        checkTypingStat("noOne");
        checkOnlineStat(timestamp);
        userRefSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStat("online");
        super.onResume();
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

                //code for image uri to upload to firebase
                try {
                    sendImgMsg(imgUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == PICK_CAMERA_ID) {
                try {
                    sendImgMsg(imgUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

}