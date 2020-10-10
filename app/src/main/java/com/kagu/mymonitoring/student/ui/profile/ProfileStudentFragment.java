package com.kagu.mymonitoring.student.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.ErrorActivity;
import com.kagu.mymonitoring.Log_inActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.student.AddDataStudentActivity;

import java.util.Objects;

public class ProfileStudentFragment extends Fragment {
    TextView uName,
            uEmail,
            uProject,
            startDate,
            endDate,
            uStudentPhone,
            studentAddress,
            schoolName,
            nimNis,
            schoolAddress,
            majors,
            schoolPhone;
    ImageView mDp;
    ImageButton btnBack, btnEdit, settingLanguage;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    String id;

    //get email and put in editProfile
    //beresin update DataStudent

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity() != null) {
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).hide();
        }
        View view = inflater.inflate(R.layout.fragment_profile_student, container, false);
        TextView logout = view.findViewById(R.id.logoutProfile);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        mDp = view.findViewById(R.id.mDp);
        uName = view.findViewById(R.id.uName);
        uEmail = view.findViewById(R.id.uEmail);
        uProject = view.findViewById(R.id.uProject);
        startDate = view.findViewById(R.id.startDate);
        endDate = view.findViewById(R.id.endDate);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnBack = view.findViewById(R.id.btnBack);
        uStudentPhone = view.findViewById(R.id.uStudentPhone);
        studentAddress = view.findViewById(R.id.studentAddress);
        schoolName = view.findViewById(R.id.schoolName);
        nimNis = view.findViewById(R.id.nimNis);
        schoolAddress = view.findViewById(R.id.schoolAddress);
        majors = view.findViewById(R.id.majors);
        schoolPhone = view.findViewById(R.id.phone);
        settingLanguage = view.findViewById(R.id.settingLanguage);

        //get data
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String username = "" + ds.child("fullname").getValue();
                    String userMail = "" + ds.child("email").getValue();
                    String userStartDate = "" + ds.child("startDate").getValue();
                    String userEndDate = "" + ds.child("endDate").getValue();
                    String userProject = "" + ds.child("projectName").getValue();
                    String userDp = "" + ds.child("imageUrl").getValue();

                    uName.setText(username);
                    uEmail.setText(userMail);
                    uProject.setText(userProject);
                    startDate.setText(userStartDate);
                    endDate.setText(userEndDate);

                    //set dp
                    if (userDp.equals("default")) {
                        if (getActivity()!=null)
                        Glide.with(getActivity())
                                .load(R.drawable.ic_pic)
                                .into(mDp);
                    } else {
                        if (getActivity()!=null)
                        Glide.with(getActivity())
                                .load(userDp)
                                .apply(new RequestOptions().override(75, 75))
                                .into(mDp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //get data
        Query query2 = FirebaseDatabase.getInstance().getReference("DataStudent").orderByChild("id").equalTo(user.getUid());
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userStudentPhone = "" + ds.child("studentPhone").getValue();
                    String userStudentAddress = "" + ds.child("studentAddress").getValue();
                    String userSchoolName = "" + ds.child("schoolName").getValue();
                    String userNimNis = "" + ds.child("nimNis").getValue();
                    String userSchoolAddress = "" + ds.child("schoolAddress").getValue();
                    String userMajors = "" + ds.child("majors").getValue();
                    String userSchoolPhone = "" + ds.child("schoolPhone").getValue();

                    uStudentPhone.setText(userStudentPhone);
                    studentAddress.setText(userStudentAddress);
                    schoolName.setText(userSchoolName);
                    nimNis.setText(userNimNis);
                    schoolAddress.setText(userSchoolAddress);
                    majors.setText(userMajors);
                    schoolPhone.setText(userSchoolPhone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnEdit.setOnClickListener(view1 -> {
            Intent intentEdit = new Intent(getActivity(), AddDataStudentActivity.class);
            intentEdit.putExtra("key", "editStudent");
            intentEdit.putExtra("editStudentKey", id);
            intentEdit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intentEdit);
        });

        logout.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(getActivity(), Log_inActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            getActivity().finish();
        });

        settingLanguage.setOnClickListener(view1 -> {
            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(intent);
        });

        checkUserStatus();

        return view;
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            id = user.getUid();

        } else {
            startActivity(new Intent(getActivity(), ErrorActivity.class));
            getActivity().finish();
        }
    }

}