package com.example.flicks_now.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flicks_now.activity.ChiTietPhimActivity;
import com.example.flicks_now.model.DSPhimAPiOphim;
import com.example.flicks_now.databinding.ItemDsphimBinding;

import java.util.List;

public class DSPhimAdapterOphim extends RecyclerView.Adapter<DSPhimAdapterOphim.SeriesViewHolder> {
    private Activity context;  // Thêm biến Activity context
    private List<DSPhimAPiOphim> seriesList;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    // Constructor
    public DSPhimAdapterOphim(Activity context, List<DSPhimAPiOphim> seriesList) {
        this.context = context;
        this.seriesList = seriesList;
    }

    // Setter cho listener
    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
        DSPhimAdapterOphim.recyclerViewItemClickListener = recyclerViewItemClickListener;
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDsphimBinding binding = ItemDsphimBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SeriesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        DSPhimAPiOphim series = seriesList.get(position);
        holder.binding.movieTitle.setText(series.getName());
        holder.binding.movieYear.setText(String.valueOf(series.getYear()));

        // Sử dụng Glide để load hình ảnh
        Glide.with(context)  // Sử dụng context đã được cung cấp
                .load(series.getPosterUrl())
                .into(holder.binding.moviePoster);

        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    // ViewHolder class
    public class SeriesViewHolder extends RecyclerView.ViewHolder {
        ItemDsphimBinding binding;
        int position;

        public SeriesViewHolder(@NonNull ItemDsphimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    recyclerViewItemClickListener.onItemClick(view, position);
//                }
//            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Lay thong tin chi tiet phim tu slug truyen den man hinh chi tiet phim
                    Intent intent = new Intent(view.getContext(), ChiTietPhimActivity.class);
                    DSPhimAPiOphim phimbo = seriesList.get(position);
                    intent.putExtra("slug", phimbo.getSlug());
                    view.getContext().startActivity(intent);
                }
            });
        }

    }
    public void updateData(List<DSPhimAPiOphim> newData) {
        seriesList.clear(); // Xóa dữ liệu cũ
        seriesList.addAll(newData); // Thêm dữ liệu mới
        notifyDataSetChanged(); // Thông báo adapter đã có dữ liệu mới
    }

    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}
