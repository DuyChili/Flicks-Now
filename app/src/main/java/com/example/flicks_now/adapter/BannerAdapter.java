package com.example.flicks_now.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.flicks_now.activity.ChiTietPhimActivity;
import com.example.flicks_now.databinding.ItemBannerBinding;
import com.example.flicks_now.model.Phim;

import java.util.List;

public class BannerAdapter extends PagerAdapter {

    private Activity context;
    private List<Phim> movies;

    public BannerAdapter(Activity context, List<Phim> movies) {
        this.context = context;
        this.movies = movies;
    }


    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // Inflate banner layout for each item
        ItemBannerBinding binding = ItemBannerBinding.inflate(LayoutInflater.from(context), container, false);
        Phim movie = movies.get(position);

        // Set movie title
        binding.tvTenPhim.setText(movie.getName());

        // Load movie banner image
        Glide.with(context)
                .load(movie.getThumb_url())  // Assuming you are using thumb_url for the banner
                .into(binding.imageViewBanner);

        // Handle click event to pass slug
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChiTietPhimActivity.class);
                intent.putExtra("slug", movie.getSlug());  // Pass the movie slug
                context.startActivity(intent);
            }
        });

        container.addView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
