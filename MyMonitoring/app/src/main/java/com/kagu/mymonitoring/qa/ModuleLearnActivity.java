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
import com.kagu.mymonitoring.adapter.ArticleAdapter;
import com.kagu.mymonitoring.entity.ModuleLearn;

import java.util.ArrayList;
import java.util.List;

public class ModuleLearnActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RecyclerView rvArticle;
    private List<ModuleLearn> moduleLearns;
    private ArticleAdapter adapter;
    FloatingActionButton fabAddReport;

    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_learn);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        firebaseAuth = FirebaseAuth.getInstance();

        //rv and properties
        rvArticle = findViewById(R.id.rv_postsArticle);
        fabAddReport = findViewById(R.id.fab_addArticle);
        fabAddReport.setOnClickListener(view1 -> startActivity(new Intent(ModuleLearnActivity.this, AddArticleActivity.class)));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ModuleLearnActivity.this);
        //show newest post first
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set layout to rv
        rvArticle.setLayoutManager(linearLayoutManager);

        moduleLearns = new ArrayList<>();

        checkUserStat();
        loadPosts();
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
        //ll for rv
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ModuleLearnActivity.this);
        //show newest post
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //set this layout to rv
        rvArticle.setLayoutManager(linearLayoutManager);

        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Article");
        //query to load posts
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moduleLearns.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String type =""+ds.child("type").getValue();
                    ModuleLearn information = ds.getValue(ModuleLearn.class);

                    moduleLearns.add(information);

                    //adapter
                    adapter = new ArticleAdapter(ModuleLearnActivity.this, moduleLearns);
                    //set adapter to rv
                    rvArticle.setAdapter(adapter);

                    if (type.equals("Quality Assurance"))
                        fabAddReport.setVisibility(View.GONE);
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
        //ll for rv
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ModuleLearnActivity.this);
        //show newest post
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //set this layout to rv
        rvArticle.setLayoutManager(linearLayoutManager);

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Article");
        Query query = ref.orderByChild("id").equalTo(myId);
        //get all data from node Posts
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moduleLearns.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModuleLearn information = ds.getValue(ModuleLearn.class);

                    if (information.getpTitleArticle().toLowerCase().contains(querySearch.toLowerCase()) ||
                            information.getpDescArticle().toLowerCase().contains(querySearch.toLowerCase())) {
                        moduleLearns.add(information);
                    }
                    //adapter
                    adapter = new ArticleAdapter(ModuleLearnActivity.this, moduleLearns);
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
}