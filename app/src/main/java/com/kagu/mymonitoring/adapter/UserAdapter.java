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
import com.kagu.mymonitoring.entity.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> users;

//    private String sLastMsg;

    public UserAdapter(Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        final String hisId = users.get(position).getId();
        String userImg = users.get(position).getImageUrl();
        String userName = users.get(position).getFullname();
        String mProjectNameUsers = users.get(position).getProjectName();
        String userType = users.get(position).getType();

        holder.username.setText(userName);
        holder.userType.setText(userType);
        holder.projectNameUsers.setText(mProjectNameUsers);
        if (userImg.equals("default")) {
            Glide.with(context).load(R.drawable.ic_pic).into(holder.profile);
        } else {
            Glide.with(context).load(userImg).into(holder.profile);
        }

        holder.itemView.setOnClickListener(view -> {

            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userid", hisId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, projectNameUsers, userType;
        public ImageView profile;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile = itemView.findViewById(R.id.profile);
            projectNameUsers = itemView.findViewById(R.id.projectNameUsers);
            userType = itemView.findViewById(R.id.userType);
        }
    }
}