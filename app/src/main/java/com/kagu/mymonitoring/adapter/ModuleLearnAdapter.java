package com.kagu.mymonitoring.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.entity.ModuleLearn;
import com.kagu.mymonitoring.qa.DetailModuleLearnActivity;

import java.util.List;
import java.util.Objects;

public class ModuleLearnAdapter extends RecyclerView.Adapter<ModuleLearnAdapter.Holder> {

    private Context context;
    private List<ModuleLearn> moduleLearns;
    String myUid;

    public ModuleLearnAdapter(Context context, List<ModuleLearn> moduleLearns) {
        this.context = context;
        this.moduleLearns = moduleLearns;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null)
            myUid = user.getUid();
    }

    @NonNull
    @Override
    public ModuleLearnAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_dailyReport
        View view = LayoutInflater.from(context).inflate(R.layout.row_article, parent, false);
        return new ModuleLearnAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleLearnAdapter.Holder holder, int position) {
        //get data
        String uid = moduleLearns.get(position).getId();
        String moduleId = moduleLearns.get(position).getModuleId();
        String pTitleArticle = moduleLearns.get(position).getpTitleArticle();
        String pDescArticle = moduleLearns.get(position).getpDescArticle();
        String pImgArticle = moduleLearns.get(position).getPostImg();

        holder.pTitle.setText(pTitleArticle);
        holder.pDesc.setText(pDescArticle);

        holder.moreReport.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailModuleLearnActivity.class);
            intent.putExtra("moduleId", moduleId);
            context.startActivity(intent);
        });
        holder.delModule.setOnClickListener(view -> {
            showMoreOption(holder.delModule, uid, myUid, moduleId, pImgArticle);
        });
        if (!uid.equals(myUid)) {
            holder.delModule.setVisibility(View.GONE);
        }
    }

    private void showMoreOption(ImageButton moreBtn, String uid, String myUid, final String moduleId, final String pImgArticle) {
        //creating popup menu currently having option Delete, we will add more option later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)) {

            moreBtn.setVisibility(View.VISIBLE);
            //add menu in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.delete);
        }

        //item cick listener
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == 0) {
                //delete is clicked
                beginDelete(moduleId,
                        pImgArticle);
            }
            return false;
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String moduleId, String pImgArticle) {
        //post can be with or without img
        if (pImgArticle.equals("noImg")) {
            //post is without img
            deleteWithoutImg(moduleId, pImgArticle);
        } else {
            deleteWithImg(moduleId, pImgArticle);
        }
    }

    private void deleteWithImg(final String moduleId, String pImgArticle) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.pDialogDel));

        //steps 1. Delete Img using uri 2.delete from database using post id
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImgArticle);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //img deleted, now delete database
                Query query = FirebaseDatabase.getInstance().getReference("ModuleLearn").orderByChild("moduleId").equalTo(moduleId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();//remove values from firebase where pId matches
                        }
                        //delted
                        Toast.makeText(context, R.string.toastDelSuccess, Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void deleteWithoutImg(String moduleId, String pImgArticle) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.pDialogDel));
        Query query = FirebaseDatabase.getInstance().getReference("ModuleLearn").orderByChild("moduleId").equalTo(moduleId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();//remove values from firebase where pId matches
                }
                //delted
                Toast.makeText(context, R.string.toastDelSuccess, Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return moduleLearns.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        //var view from row_dailyreport
        TextView pTitle, pDesc;

        ImageButton delModule;
        TextView moreReport; //judul item


        Holder(@NonNull View itemView) {
            super(itemView);
            pTitle = itemView.findViewById(R.id.title_module);
            pDesc = itemView.findViewById(R.id.descModule);
            moreReport = itemView.findViewById(R.id.morePost);
            delModule = itemView.findViewById(R.id.delModule);
        }
    }
}
