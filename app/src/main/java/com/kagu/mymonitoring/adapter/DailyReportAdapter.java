package com.kagu.mymonitoring.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.kagu.mymonitoring.entity.DailyReport;
import com.kagu.mymonitoring.student.DetailDailyReportActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DailyReportAdapter extends RecyclerView.Adapter<DailyReportAdapter.Holder> {

    private Context context;
    String myId;
    private List<DailyReport> dailyReports;

    public DailyReportAdapter(Context context, List<DailyReport> dailyReports) {
        this.context = context;
        this.dailyReports = dailyReports;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user !=null)
        myId = user.getUid();
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_dailyReport
        View view = LayoutInflater.from(context).inflate(R.layout.row_dailyreport, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        //get data
        String id = dailyReports.get(position).getId();
        String reportId = dailyReports.get(position).getReportId();
        String uName = dailyReports.get(position).getUsername();
        String mTimeReport = dailyReports.get(position).getPostTime();
        String mTitleScenario = dailyReports.get(position).getpTitleScenario();
        String mProjectName = dailyReports.get(position).getpProjectName();
        String imgReport = dailyReports.get(position).getPostImg();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(mTimeReport));
        String pTime = DateFormat.format("dd MMM yyyy \n hh:mm aa", calendar).toString();

        holder.mName.setText(uName);
        holder.mtimeReport.setText(pTime);
        holder.mTitleScenario.setText(mTitleScenario);
        holder.mProjectName.setText(mProjectName);

        holder.moreReport.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailDailyReportActivity.class);
            intent.putExtra("reportId", reportId);
            context.startActivity(intent);
        });
        holder.delOption.setOnClickListener(view -> {
            showMoreOption(holder.delOption, id, myId, reportId, imgReport);
        });

        if (!id.equals(myId)) {
            holder.delOption.setVisibility(View.GONE);
        }
    }

    private void showMoreOption(ImageButton delOption, String id, String myId, final String reportId, final String imgReport) {
        //creating popup menu currently having option Delete, we will add more option later
        PopupMenu popupMenu = new PopupMenu(context, delOption, Gravity.END);

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
                beginDelete(reportId,
                        imgReport);
            }
            return false;
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String reportId, String imgReport) {
        //post can be with or without img
        if (imgReport.equals("noImg")){
            //post is without img
            deleteWithoutImg(reportId);
        }else {
            deleteWithImg(reportId, imgReport);
        }
    }

    private void deleteWithImg(final String reportId, String imgReport) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.pDialogDel));

        //steps 1. Delete Img using uri 2.delete from database using post id
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imgReport);
        storageReference.delete().addOnSuccessListener(aVoid -> {
            //img deleted, now delete database
            Query query = FirebaseDatabase.getInstance().getReference("DailyReport").orderByChild("reportId").equalTo(reportId);
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

    private void deleteWithoutImg(String reportId) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.pDialogDel));
        Query query = FirebaseDatabase.getInstance().getReference("DailyReport").orderByChild("reportId").equalTo(reportId);
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
        return dailyReports.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        //var view from row_dailyreport
        TextView mName, mtimeReport, mTitleScenario, mProjectName;

        ImageButton delOption;
        TextView moreReport; //judul item

        Holder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.uFullname);
            mtimeReport = itemView.findViewById(R.id.timeReport);
            mTitleScenario = itemView.findViewById(R.id.titleScenario);
            mProjectName = itemView.findViewById(R.id.projectName);
            moreReport = itemView.findViewById(R.id.morePost);
            delOption = itemView.findViewById(R.id.delOption);
        }
    }
}
