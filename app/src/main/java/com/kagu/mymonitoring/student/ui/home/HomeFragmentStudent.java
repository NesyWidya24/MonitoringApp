package com.kagu.mymonitoring.student.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.entity.User;
import com.kagu.mymonitoring.qa.ModuleLearnActivity;
import com.kagu.mymonitoring.student.DailyReportActivity;
import com.kagu.mymonitoring.MyProjectActivity;
import com.kagu.mymonitoring.student.LearnStudentActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragmentStudent extends Fragment {

    private FirebaseAuth firebaseAuth;

    String myId;

    private TextView username;
    private CircleImageView profile_img;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_student, container, false);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
        firebaseAuth = FirebaseAuth.getInstance();

        profile_img = view.findViewById(R.id.userImg);
        username = view.findViewById(R.id.usernameStudent);

        Button moduleLearn = view.findViewById(R.id.moduleLearn);
        Button myProject = view.findViewById(R.id.myProject);
        Button btn_dailyReport = view.findViewById(R.id.btn_dailyReport);

        moduleLearn.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), ModuleLearnActivity.class)));
        myProject.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), MyProjectActivity.class)));
        btn_dailyReport.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), DailyReportActivity.class)));

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

        checkUserStat();
        return view;
    }

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(getActivity(), ErrorActivity.class));
            requireActivity().finish();
        }
    }

}
