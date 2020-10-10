package com.kagu.mymonitoring.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.chat.FullImageActivity;
import com.kagu.mymonitoring.entity.Chat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<Chat> chats;
    private String imgUrl;

    FirebaseUser firebaseUser;

    public MsgAdapter(Context context, List<Chat> chats, String imgUrl) {
        this.context = context;
        this.chats = chats;
        this.imgUrl = imgUrl;
    }

    @NonNull
    @Override
    public MsgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MsgAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MsgAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MsgAdapter.ViewHolder holder, final int position) {
        String message = chats.get(position).getMessage();
        String timestamp = chats.get(position).getTimestamp();
        String type = chats.get(position).getType();
        String key = chats.get(position).getKey();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd MMM HH:mm", calendar).toString(); //17 Jul 19:02 (hh 12 hours, HH 24 hours)

        if (type.equals("text")){
            holder.show_msg.setVisibility(View.VISIBLE);
            holder.msgIv.setVisibility(View.GONE);
            holder.show_msg.setText(message);
        }else {
            holder.show_msg.setVisibility(View.GONE);
            holder.msgIv.setVisibility(View.VISIBLE);
            holder.msgIv.setOnClickListener(view -> {
                Intent intent = new Intent(context, FullImageActivity.class);
                intent.putExtra("fullImg", key);
                context.startActivity(intent);
            });

            holder.msgIv.setLongClickable(true);
            holder.msgIv.setOnLongClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.delete);
                builder.setMessage(R.string.askDelMsg);

                builder.setPositiveButton(R.string.delete, (dialogInterface, i) -> deleteMsg(position));
                builder.setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss());
                builder.create().show();
                return true;
            });

            Glide.with(context).load(message)
                    .placeholder(R.drawable.ic_image)
                    .into(holder.msgIv);
        }

        holder.txt_time.setText(dateTime);

            holder.msgLayout.setLongClickable(true);
            holder.msgLayout.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.delete);
            builder.setMessage(R.string.askDelMsg);

            builder.setPositiveButton(R.string.delete, (dialogInterface, i) -> deleteMsg(position));
            builder.setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
            return true;
        });

        if (position == chats.size() - 1) {
            if (chats.get(position).isSeen()) {
                holder.txt_seen.setText(R.string.seen);
            } else {
                holder.txt_seen.setText(R.string.delivered);
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }
    }

    private void deleteMsg(int position) {
        final String  myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimestamp = chats.get(position).getTimestamp();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = reference.orderByChild("timestamp").equalTo(msgTimestamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    if (ds.child("sender").getValue().equals(myId)){

                        ds.getRef().removeValue();

//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("message", "This message was delete...");
//                        ds.getRef().updateChildren(hashMap);

//                        Toast.makeText(context, "message deleted..", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, R.string.toastDelMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView show_msg, txt_seen, txt_time;
        ImageView msgIv;
        LinearLayout msgLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            show_msg = itemView.findViewById(R.id.show_msg);
//            profile = itemView.findViewById(R.id.profileIv);
            txt_time = itemView.findViewById(R.id.timeTv);
            txt_seen = itemView.findViewById(R.id.txt_see);
            msgLayout = itemView.findViewById(R.id.msgLayout);
            msgIv = itemView.findViewById(R.id.msgIv);
        }
    }
}