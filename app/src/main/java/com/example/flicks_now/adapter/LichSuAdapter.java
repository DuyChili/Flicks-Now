package com.example.flicks_now.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.databinding.ItemLichsuBinding;

import java.util.List;

public class LichSuAdapter extends RecyclerView.Adapter<LichSuAdapter.MovieViewHolder> {
    private Activity context;
    private List<ChiTietPhim.MovieItem> movies;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    public LichSuAdapter(Activity context, List<ChiTietPhim.MovieItem> movies) {
        this.context = context;
        this.movies = movies;
    }

    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        recyclerViewItemClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLichsuBinding binding = ItemLichsuBinding.inflate(context.getLayoutInflater(), parent, false);
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        ChiTietPhim.MovieItem movie = movies.get(position);
        holder.binding.movieTitle.setText(movie.getName());

        // Sử dụng Glide để load hình ảnh
        Glide.with(context)  // Sử dụng context đã được cung cấp
                .load(movie.getPosterUrl())
                .into(holder.binding.moviePoster);



        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        ItemLichsuBinding binding;
        int position;
        public MovieViewHolder(@NonNull ItemLichsuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewItemClickListener.onItemClick(view, position);
                }
            });
        }
    }

    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}
