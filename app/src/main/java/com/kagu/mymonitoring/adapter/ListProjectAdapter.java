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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.kagu.mymonitoring.admin.DetailProjectActivity;
import com.kagu.mymonitoring.entity.Project;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListProjectAdapter extends RecyclerView.Adapter<ListProjectAdapter.Holder> {

    private Context context;
    private List<Project> listProjects;
    String myId;

    public ListProjectAdapter(Context context, List<Project> listProjects) {
        this.context = context;
        this.listProjects = listProjects;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user !=null)
            myId = user.getUid();
    }

    @NonNull
    @Override
    public ListProjectAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_dailyReport
        View view = LayoutInflater.from(context).inflate(R.layout.row_list_projects, parent, false);
        return new ListProjectAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListProjectAdapter.Holder holder, int position) { //add entity by child ListProject
        //get data
        String id = listProjects.get(position).getId();
        String imageUrl = listProjects.get(position).getImgProject();
        String projectName = listProjects.get(position).getProjectName();
        String projectClient = listProjects.get(position).getProjectClient();
        String idProject = listProjects.get(position).getIdProject();

        holder.mProjectName.setText(projectName);
        holder.projectClient.setText(projectClient);

        //set user dp
        if (imageUrl.equals("noImg")) {
            Glide.with(context).load(R.drawable.ic_no_img).into(holder.imageProject);
        } else {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions().override(70, 70))
                    .into(holder.imageProject);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailProjectActivity.class);
            intent.putExtra("idProject", idProject); //add id every project for THIS
            context.startActivity(intent);
        });
        holder.delProject.setOnClickListener(view -> {
            showMoreOption(holder.delProject, id, myId, idProject, imageUrl);
        });
        if (!id.equals(myId)) {
            holder.delProject.setVisibility(View.GONE);
        }
    }

    private void showMoreOption(ImageButton moreBtn, String id, String myId, final String idProject, final String imageUrl) {
        //creating popup menu currently having option Delete, we will add more option later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (id.equals(myId)){
            //add menu in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,R.string.delete);
        }

        //item cick listener
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int idItem  = menuItem.getItemId();
            if (idItem==0) {
                //delete is clicked
                beginDelete(idProject,
                        imageUrl);
            }
            return false;
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String idProject, String imageUrl) {
        //post can be with or without img
        if (imageUrl.equals("noImg")){
            //post is without img
            deleteWithoutImg(idProject,imageUrl);
        }else {
            deleteWithImg(idProject, imageUrl);
        }
    }

    private void deleteWithImg(final String idProject, String imageUrl) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.pDialogDel));

        //steps 1. Delete Img using uri 2.delete from database using post id
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageReference.delete().addOnSuccessListener(aVoid -> {
            //img deleted, now delete database
            Query query = FirebaseDatabase.getInstance().getReference("ListProjects").orderByChild("idProject").equalTo(idProject);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
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
        });
    }

    private void deleteWithoutImg(String idProject, String imageUrl) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.pDialogDel));
        Query query = FirebaseDatabase.getInstance().getReference("ListProjects").orderByChild("idProject").equalTo(idProject);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
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
        return listProjects.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        //var view from row_dailyreport
        TextView mProjectName, projectClient;

        ImageButton delProject;
        CircleImageView imageProject;
        CardView listProjects;

        Holder(@NonNull View itemView) {
            super(itemView);
//            mCopyLink = itemView.findViewById(R.id.copyLink); MAKE LINK TO CHAT PIC, FOR MORE DETAIL PROJECT
//            klik project > show dialog from decs  and button chat > arah btnChat to messageChat

            imageProject = itemView.findViewById(R.id.imageProject);
            mProjectName = itemView.findViewById(R.id.projectName);
            listProjects = itemView.findViewById(R.id.listProjects);
            delProject = itemView.findViewById(R.id.delProject);
            projectClient = itemView.findViewById(R.id.projectClient);
        }
    }
}