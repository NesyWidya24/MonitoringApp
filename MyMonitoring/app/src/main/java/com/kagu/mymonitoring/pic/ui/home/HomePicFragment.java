package com.kagu.mymonitoring.pic.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.entity.User;
import com.kagu.mymonitoring.pic.ReportPicActivity;
import com.kagu.mymonitoring.qa.AllDailyReportActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePicFragment extends Fragment {
    TextView username, department;
    Button btnReport;
    CircleImageView profile_img;

    private List<User> users;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_pic, container, false);

        profile_img = view.findViewById(R.id.profileImgPic);
        username = view.findViewById(R.id.usernamePic);
        department = view.findViewById(R.id.departmentPic);
        btnReport = view.findViewById(R.id.btn_dailyReport);

        users = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        btnReport.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), ReportPicActivity.class));
        });

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

    public HomePicFragment() {
    }
}