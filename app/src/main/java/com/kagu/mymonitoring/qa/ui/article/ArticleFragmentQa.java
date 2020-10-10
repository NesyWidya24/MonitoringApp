package com.kagu.mymonitoring.qa.ui.article;

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
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.kagu.mymonitoring.adapter.ArticleAdapter;
import com.kagu.mymonitoring.entity.ModuleLearn;
import com.kagu.mymonitoring.qa.AddArticleActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArticleFragmentQa extends Fragment {
    private FirebaseAuth firebaseAuth;
    private RecyclerView rvArticle;
    private List<ModuleLearn> informationList;
    private ArticleAdapter adapter;

    String myId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_qa, container, false);

        if (getActivity() != null)
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).hide();

        firebaseAuth = FirebaseAuth.getInstance();

        //rv and properties
        rvArticle = view.findViewById(R.id.rv_postsArticle);
        FloatingActionButton fabAddReport = view.findViewById(R.id.fab_addArticle);
        fabAddReport.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), AddArticleActivity.class)));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        //show newest post first
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set layout to rv
        rvArticle.setLayoutManager(linearLayoutManager);

        informationList = new ArrayList<>();

        checkUserStat();
        loadPosts();
        EditText search_article = view.findViewById(R.id.search_article);
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
        //ll for rv
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
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
                informationList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModuleLearn information = ds.getValue(ModuleLearn.class);

                    informationList.add(information);

                    //adapter
                    adapter = new ArticleAdapter(getActivity(), informationList);
                    //set adapter to rv
                    rvArticle.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //if error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchArticle(final String querySearch) {
        //ll for rv
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
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
                informationList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModuleLearn information = ds.getValue(ModuleLearn.class);

                    if (information.getpTitleArticle().toLowerCase().contains(querySearch.toLowerCase()) ||
                            information.getpDescArticle().toLowerCase().contains(querySearch.toLowerCase())) {
                        informationList.add(information);
                    }
                    //adapter
                    adapter = new ArticleAdapter(getActivity(), informationList);
                    //set adapter to rv
                    rvArticle.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//if error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        //searchView
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    searchArticle(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchArticle(s);
                }
                if (TextUtils.isEmpty(s)) {
                    rvArticle.setVisibility(View.GONE);
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            firebaseAuth.signOut();
            Intent i = new Intent(getActivity(), Log_inActivity.class);
            startActivity(i);
        }
        if (id == R.id.add_post) {
            startActivity(new Intent(getActivity(), AddArticleActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}