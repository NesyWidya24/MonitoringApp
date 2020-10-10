package com.kagu.mymonitoring.qa.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.admin.ListStudentActivity;
import com.kagu.mymonitoring.entity.User;
import com.kagu.mymonitoring.qa.AddArticleActivity;
import com.kagu.mymonitoring.qa.AllDailyReportActivity;
import com.kagu.mymonitoring.qa.ModuleLearnActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragmentQa extends Fragment {

    private TextView username, department;
    private CircleImageView profile_img;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_qa, container, false);

        profile_img = view.findViewById(R.id.profileImgQa);
        username = view.findViewById(R.id.usernameQa);
        department = view.findViewById(R.id.departmentQa);
        Button report = view.findViewById(R.id.reportStudent);
        Button moduleLearn = view.findViewById(R.id.moduleLearn);
        Button dataStudent = view.findViewById(R.id.dataStudent);

        report.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), AllDailyReportActivity.class)));
        moduleLearn.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), ModuleLearnActivity.class)));
        dataStudent.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), ListStudentActivity.class)));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getActivity() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getFullname());
                department.setText(user.getType());
                if (user.getImageUrl().equals("default")) {
                    profile_img.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getActivity()).load(user.getImageUrl()).into(profile_img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    public HomeFragmentQa() {
    }
}
