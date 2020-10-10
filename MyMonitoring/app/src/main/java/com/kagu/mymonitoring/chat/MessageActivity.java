package com.kagu.mymonitoring.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kagu.mymonitoring.Log_inActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.StartActivity;
import com.kagu.mymonitoring.adapter.MsgAdapter;
import com.kagu.mymonitoring.entity.Chat;
import com.kagu.mymonitoring.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

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
    ImageButton sendBtn;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefSeen;

    List<Chat> chats;
    MsgAdapter msgAdapter;

    String hisId;
    String myId;
    String hisImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.chat_rv);
        profile = findViewById(R.id.profileChat);
        username = findViewById(R.id.usernameChat);
        userStat = findViewById(R.id.userStat);
        msgEt = findViewById(R.id.msgEt);
        sendBtn = findViewById(R.id.sendBtn);


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
                        }else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(status));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString(); //hh:mm am/pm
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
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = msgEt.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(MessageActivity.this, "Cannot send empty message..", Toast.LENGTH_SHORT).show();
                } else {
                    sendMsg(msg);
                }
                msgEt.setText("");
            }
        });

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

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myId);
        hashMap.put("receiver", hisId);
        hashMap.put("message", msg);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference refChat1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myId)
                .child(hisId);
        refChat1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
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
                if (!snapshot.exists()){
                    refChat2.child("id").setValue(myId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}