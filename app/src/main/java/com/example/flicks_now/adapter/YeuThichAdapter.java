package com.example.flicks_now.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.databinding.ItemLichsuBinding;


import java.util.List;

public class YeuThichAdapter extends RecyclerView.Adapter<YeuThichAdapter.ViewHolder> {
    private Activity context;
    private List<ChiTietPhim.MovieItem> movies;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    public YeuThichAdapter(Activity context, List<ChiTietPhim.MovieItem> favoriteMovies) {
        this.context = context;
        this.movies = favoriteMovies;
    }

    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.recyclerViewItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLichsuBinding binding = ItemLichsuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietPhim.MovieItem movie = movies.get(position);
        holder.binding.movieTitle.setText(movie.getName());

        // Sử dụng Glide để load hình ảnh
        Glide.with(context)
                .load(movie.getPosterUrl())
                .into(holder.binding.moviePoster);

        final int pos = position;
        holder.position = pos; // Lưu Position cho Holder
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemLichsuBinding binding;
        int position;

        public ViewHolder(@NonNull ItemLichsuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewItemClickListener != null) {
                        recyclerViewItemClickListener.onItemClick(view, position);
                    }
                }
            });
        }
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}
