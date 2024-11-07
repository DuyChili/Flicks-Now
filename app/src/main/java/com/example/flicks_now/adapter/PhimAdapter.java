package com.example.flicks_now.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.flicks_now.model.QLPhim;
import com.example.flicks_now.databinding.ItemPhimBinding;

import java.util.List;

public class PhimAdapter extends RecyclerView.Adapter<PhimAdapter.PhimViewHolder> {

    private Context context;
    private List<QLPhim> movieList;

    public PhimAdapter(Context context, List<QLPhim> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public PhimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng ViewBinding cho item_movie
        ItemPhimBinding binding = ItemPhimBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PhimViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PhimViewHolder holder, int position) {
        QLPhim movie = movieList.get(position);

        // Đặt dữ liệu vào các thành phần view
        holder.binding.movieTitle.setText(movie.getName());
        holder.binding.movieYear.setText(String.valueOf(movie.getYear()));

        // Sử dụng Glide để load ảnh từ URL
        Glide.with(context)
                .load(movie.getPoster_url())
                .into(holder.binding.moviePoster);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class PhimViewHolder extends RecyclerView.ViewHolder {
        private final ItemPhimBinding binding;

        public PhimViewHolder(ItemPhimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
