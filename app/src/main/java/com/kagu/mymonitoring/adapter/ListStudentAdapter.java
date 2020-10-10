package com.kagu.mymonitoring.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.admin.DetailUsersStudentActivity;
import com.kagu.mymonitoring.admin.RegisterActivity;
import com.kagu.mymonitoring.entity.User;

import java.util.List;

public class ListStudentAdapter extends RecyclerView.Adapter<ListStudentAdapter.ViewHolder> {
    private Context context;
    private List<User> users;

    public ListStudentAdapter(Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ListStudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_list_users, parent, false);
        return new ListStudentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListStudentAdapter.ViewHolder holder, int position) {
        String hisId = users.get(position).getId();
        String mUserType = users.get(position).getType();
        String userName = users.get(position).getFullname();
        String mProjectNameUsers = users.get(position).getProjectName();

        holder.username.setText(userName);
        holder.projectNameUsers.setText(mProjectNameUsers);
        holder.userType.setText(mUserType);

        holder.moreDetail.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailUsersStudentActivity.class);
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

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.uFullName);
            userType = itemView.findViewById(R.id.userType);
            moreDetail = itemView.findViewById(R.id.moreDetail);
            projectNameUsers = itemView.findViewById(R.id.projectName);
        }
    }
}