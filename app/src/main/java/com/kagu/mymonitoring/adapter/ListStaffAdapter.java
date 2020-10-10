package com.kagu.mymonitoring.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.admin.DetailUsersStaffActivity;
import com.kagu.mymonitoring.admin.RegisterActivity;
import com.kagu.mymonitoring.entity.User;

import java.util.List;

public class ListStaffAdapter extends RecyclerView.Adapter<ListStaffAdapter.ViewHolder> {
    private Context context;
    private List<User> users;


    public ListStaffAdapter(Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ListStaffAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_list_users, parent, false);
        return new ListStaffAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListStaffAdapter.ViewHolder holder, int position) {
        String hisId = users.get(position).getId();
        String mUserType = users.get(position).getType();
        String userName = users.get(position).getFullname();
        String mProjectNameUsers = users.get(position).getProjectName();

        holder.username.setText(userName);
        holder.projectNameUsers.setText(mProjectNameUsers);
        holder.userType.setText(mUserType);

        holder.moreDetail.setOnClickListener(view -> {
                Intent intent = new Intent(context, DetailUsersStaffActivity.class);
                intent.putExtra("id", hisId);
                context.startActivity(intent);

        });

    }
    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, projectNameUsers, userType, moreDetail;
        public ImageButton editBtn;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.uFullName);
            userType = itemView.findViewById(R.id.userType);
            moreDetail = itemView.findViewById(R.id.moreDetail);
            projectNameUsers = itemView.findViewById(R.id.projectName);
        }
    }
}
