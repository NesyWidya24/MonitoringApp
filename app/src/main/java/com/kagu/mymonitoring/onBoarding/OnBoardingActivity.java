package com.kagu.mymonitoring.onBoarding;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kagu.mymonitoring.Log_inActivity;
import com.kagu.mymonitoring.R;
import com.kagu.mymonitoring.adapter.SliderAdapter;

public class OnBoardingActivity extends AppCompatActivity {
    private ViewPager slideVPager;
    private LinearLayout dotLayout;
    private TextView[] mDots;
    private Button btn_next;
    private int currentPage;

    FirebaseAuth mAuth;
    private static final String TAG = "Message";

    //need this, for fix issue "seudah splash masuk main balik lagi ke onBoarding (kondisi sudah login)"
    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = (FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference uidRef = reference.child("Users").child(uid);
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, databaseError.getMessage());
                }
            };
            uidRef.addListenerForSingleValueEvent(valueEventListener);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        if (getSupportActionBar()!=null)
            getSupportActionBar().hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        slideVPager = findViewById(R.id.slideViewPager);
        dotLayout = findViewById(R.id.dotsLayout);

        btn_next = findViewById(R.id.nextBtn);

        SliderAdapter sliderAdapter = new SliderAdapter(this);

        slideVPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        slideVPager.addOnPageChangeListener(viewListener);

        btn_next.setOnClickListener(view -> {
            slideVPager.setCurrentItem(currentPage + 1);

            Intent intent = new Intent(OnBoardingActivity.this, Log_inActivity.class);
            startActivity(intent);

        });
    }

    private void addDotsIndicator(int position) {
        mDots = new TextView[3];
        dotLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.transparent));

            dotLayout.addView(mDots[i]);
        }

        if (mDots.length > 0) {
            mDots[position].setTextSize(37);
            mDots[position].setTextColor(getResources().getColor(R.color.textIcons));
        }
    }

    private final ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);

            currentPage = position;

            if (position == 0) {
                btn_next.setEnabled(false);
                btn_next.setText("");
            } else if (position == mDots.length - 1) {
                btn_next.setEnabled(true);
                btn_next.setText(R.string.btnSelesai);
            } else {
                btn_next.setEnabled(false);
                btn_next.setText("");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
