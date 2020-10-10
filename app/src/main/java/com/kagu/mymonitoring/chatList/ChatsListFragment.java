package com.kagu.mymonitoring.chatList;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.adapter.AdapterChatlist;
import com.kagu.mymonitoring.entity.Chat;
import com.kagu.mymonitoring.entity.Chatlist;
import com.kagu.mymonitoring.entity.User;
import com.kagu.mymonitoring.users.UsersQaActivity;
import com.kagu.mymonitoring.users.UsersProjectActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsListFragment extends Fragment {

    private RecyclerView rv_chatsList;

    List<Chatlist> chatlistList;
    List<User> usersList;
    DatabaseReference ref;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;
    FloatingActionButton fabNewChat;
    String project;
    TextView unread;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats_list, container, false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        rv_chatsList = view.findViewById(R.id.rv_chats);
        unread = view.findViewById(R.id.unread);

        fabNewChat = view.findViewById(R.id.fabNewChat);
        //get data
        Query query1 = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(currentUser.getUid());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    project = "" + ds.child("projectName").getValue();

                    if (project.equals("All Project")) //all users for lead qa and all staff qa
                        fabNewChat.setOnClickListener(view1 -> startActivity(new Intent(getContext(), UsersQaActivity.class)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unreadMsg = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null)
                        if (chat.getReceiver().equals(currentUser.getUid()) && !chat.isSeen()) {
                            unreadMsg++;
                        }
                }
                if (unreadMsg == 0) {
                    unread.setText(" ");
                } else {
                    unread.setText("(" + unreadMsg + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        fabNewChat.setOnClickListener(view1 -> startActivity(new Intent(getContext(), UsersProjectActivity.class)));
        chatlistList = new ArrayList<>();
        ref = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chatlist chatlist = ds.getValue(Chatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }

    private void loadChats() {
        usersList = new ArrayList<>();
        ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User users = ds.getValue(User.class);
                    for (Chatlist chatlist : chatlistList) {
                        if (users != null)
                            if (users.getId() != null && users.getId().equals(chatlist.getId())) {
                                usersList.add(users);
                                break;
                            }
                    }
                    adapterChatlist = new AdapterChatlist(getContext(), usersList);
                    //setAdapter
                    rv_chatsList.setAdapter(adapterChatlist);
                    //set last msg
                    for (int i = 0; i < usersList.size(); i++) {
                        lastMsg(usersList.get(i).getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMsg(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMsg = "default";
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())) {
                        if (chat.getType().equals("image")) {
                            theLastMsg = "Sent a photo";
                        } else {
                            theLastMsg = chat.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMsgMap(userId, theLastMsg);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}