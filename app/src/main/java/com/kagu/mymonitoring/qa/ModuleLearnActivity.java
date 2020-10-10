package com.kagu.mymonitoring.qa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.adapter.ModuleLearnAdapter;
import com.kagu.mymonitoring.entity.ModuleLearn;

import java.util.ArrayList;
import java.util.List;

public class ModuleLearnActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RecyclerView rvArticle;
    private List<ModuleLearn> moduleLearns;
    private ModuleLearnAdapter adapter;
    FloatingActionButton fabAddReport;

    String myId, type;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_learn);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        rvArticle = findViewById(R.id.rv_postsArticle);
        firebaseAuth = FirebaseAuth.getInstance();
        fabAddReport = findViewById(R.id.fab_addArticle);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView titleForm = findViewById(R.id.titleForm);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        moduleLearns = new ArrayList<>();

        fabAddReport.setOnClickListener(view1 -> startActivity(new Intent(ModuleLearnActivity.this, AddModuleActivity.class)));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ModuleLearnActivity.this);
        //show newest post first
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //set layout to rv
        rvArticle.setLayoutManager(linearLayoutManager);
        EditText search_article = findViewById(R.id.search_article);
        search_article.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchArticle(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //get data
        Query query1 = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(currentUser.getUid());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    type = "" + ds.child("type").getValue();

                    if (type.equals("Student") || type.equals("Non Active Student")) {
                        fabAddReport.setVisibility(View.GONE);
                        titleForm.setText(R.string.moduleLearn);
                    }
                    if (type.equals("Non Active Student")) {
                        btnBack.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnBack.setOnClickListener(view ->
                onSupportNavigateUp());
        checkUserStat();
        loadPosts();
    }

    private void checkUserStat() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        } else {
            startActivity(new Intent(ModuleLearnActivity.this, ErrorActivity.class));
            finish();
        }
    }

    private void loadPosts() {
        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ModuleLearn");
        //query to load posts
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moduleLearns.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModuleLearn information = ds.getValue(ModuleLearn.class);

                    moduleLearns.add(information);

                    //adapter
                    adapter = new ModuleLearnAdapter(ModuleLearnActivity.this, moduleLearns);
                    //set adapter to rv
                    rvArticle.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //if error
                Toast.makeText(ModuleLearnActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchArticle(final String querySearch) {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ModuleLearn");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moduleLearns.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModuleLearn moduleLearn = ds.getValue(ModuleLearn.class);

                    if (moduleLearn != null) {
                        if (moduleLearn.getpTitleArticle().toLowerCase().contains(querySearch.toLowerCase())) {
                            moduleLearns.add(moduleLearn);
                        }
                    }
                    //adapter
                    adapter = new ModuleLearnAdapter(ModuleLearnActivity.this, moduleLearns);
                    //set adapter to rv
                    rvArticle.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//if error
                Toast.makeText(ModuleLearnActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}