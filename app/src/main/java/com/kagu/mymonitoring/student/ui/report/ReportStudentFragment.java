package com.kagu.mymonitoring.student.ui.report;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.kagu.mymonitoring.adapter.DailyReportAdapter;
import com.kagu.mymonitoring.entity.DailyReport;
import com.kagu.mymonitoring.qa.AllDailyReportActivity;
import com.kagu.mymonitoring.student.AddDailyReportActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportStudentFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private RecyclerView rvPosts;
    private List<DailyReport> dailyReports;
    private DailyReportAdapter adapter;


    String myId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_student, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        //rv and properties
        rvPosts = view.findViewById(R.id.rv_postsStudent);
        FloatingActionButton fabAddReport = view.findViewById(R.id.fab_addReport);

        rvPosts.setHasFixedSize(true);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyReports = new ArrayList<>();

        fabAddReport.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), AddDailyReportActivity.class)));

        checkUserStat();
        loadPosts();

        EditText search_users = view.findViewById(R.id.search_report);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    searchPost(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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

    private void loadPosts() {
        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DailyReport").child("Traceable Matrix");
        //query to load posts
        Query query = reference.orderByChild("id").equalTo(myId);
        //get all data from node Posts
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DailyReport dailyReport = ds.getValue(DailyReport.class);

                    dailyReports.add(dailyReport);
                }
                //adapter
                adapter = new DailyReportAdapter(getContext(), dailyReports);
                //set adapter to rv
                rvPosts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //if error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPost(final String querySearch) {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DailyReport").child("Traceable Matrix");
        Query query = ref.orderByChild("id").equalTo(myId);
        //get all data from node Posts
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyReports.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DailyReport dailyReport = ds.getValue(DailyReport.class);

                    if (dailyReport.getpProjectName().toLowerCase().contains(querySearch.toLowerCase()) ||
                            dailyReport.getpTitleScenario().toLowerCase().contains(querySearch.toLowerCase()) ||
                            dailyReport.getpResult().toLowerCase().contains(querySearch.toLowerCase())) {
                        dailyReports.add(dailyReport);
                    }
                    //adapter
                    adapter = new DailyReportAdapter(getActivity(), dailyReports);
                    //set adapter to rv
                    rvPosts.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//if error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}