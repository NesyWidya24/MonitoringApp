package com.kagu.mymonitoring.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.chat.MessageActivity;
import com.kagu.mymonitoring.entity.Chatlist;
import com.kagu.mymonitoring.entity.User;

import java.util.HashMap;
import java.util.List;


public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.Holder>{
    Context context;
    List<User> userList;//get user info
    private HashMap<String, String> lastMsgMap;

    //constructor
    public AdapterChatlist(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMsgMap = new HashMap<>();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_chatlist
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String hisUid = userList.get(position).getId();
        String userImg = userList.get(position).getImageUrl();
        String userName = userList.get(position).getFullname();
        String lastMsg = lastMsgMap.get(hisUid);

        //set data
        holder.nameTv.setText(userName);
        if (lastMsg==null || lastMsg.equals("default")){
            holder.lastMsgTv.setVisibility(View.GONE);
        }else {
            holder.lastMsgTv.setVisibility(View.VISIBLE);
            holder.lastMsgTv.setText(lastMsg);
        }
        try {
            Glide.with(context).load(userImg)
                    .placeholder(R.drawable.ic_pic)
                    .into(holder.profileIv);
        }catch (Exception e){
            Glide.with(context).load(R.drawable.ic_pic)
                    .into(holder.profileIv);
        }
        if (userList.get(position).getOnlineStat().equals("online")){
            holder.onlineStatIv.setImageResource(R.drawable.ic_online);
        }else {
            holder.onlineStatIv.setImageResource(R.drawable.ic_offline);
        }

        holder.itemView.setOnClickListener(view -> {
            //start chat activity with that user
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userid", hisUid);
            context.startActivity(intent);
        });
    }

    public void setLastMsgMap(String userId, String lastMsg){
        lastMsgMap.put(userId, lastMsg);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        //views of row_chatlist
        ImageView profileIv, onlineStatIv;
        TextView nameTv, lastMsgTv;

        public Holder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatIv = itemView.findViewById(R.id.onlineStatIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMsgTv = itemView.findViewById(R.id.lastMsgTv);
        }
    }
}
