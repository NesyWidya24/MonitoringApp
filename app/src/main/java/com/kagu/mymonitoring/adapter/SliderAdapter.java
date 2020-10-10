package com.kagu.mymonitoring.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.kagu.mymonitoring.R;

public class SliderAdapter extends PagerAdapter {
    private Context context;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    private int[] slide_image = {
            R.drawable.data,
            R.drawable.kerjasama,
            R.drawable.qa
    };

    private String[] slide_headings = {
            "Pendataan",
            "Kerja Sama",
            "Pengujian"
    };

    private String[] slide_desc = {
            "Data peserta magang yang lengkap dan tersimpan secara digital",
            "Memudahkan antara peserta magang, Quality Assurance dan Person In Charge untuk me monitoring dan diskusi mengenai proyek aplikasi.",
            "Peserta magang dapat melakukan pengujian dengan teknik pengujian yang baik untuk mendeteksi kesalahan pada aplikasi"
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImg = view.findViewById(R.id.slide_image);
        TextView slideHeading = view.findViewById(R.id.slide_heading);
        TextView slideDesc = view.findViewById(R.id.slide_decs);

        slideImg.setImageResource(slide_image[position]);
        slideHeading.setText(slide_headings[position]);
        slideDesc.setText(slide_desc[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}
