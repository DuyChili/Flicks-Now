package com.example.flicks_now.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.databinding.ItemTheloaiBinding;
import com.example.flicks_now.model.Series;

import java.util.List;


public class TheLoaiAdapter extends RecyclerView.Adapter<TheLoaiAdapter.ViewHolder> {
    private Context context;
    private List<Series.Category> categories;
    private OnItemClickListener listener;

    public TheLoaiAdapter(Context context, List<Series.Category> categories) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }
    public interface OnItemClickListener {
        void onItemClick(Series.Category series);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTheloaiBinding binding = ItemTheloaiBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Series.Category series = categories.get(position);
        holder.binding.tvTheLoai.setText(series.getName()); // Sử dụng binding để truy cập các phần tử giao diện

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemTheloaiBinding binding;

        public ViewHolder(@NonNull ItemTheloaiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
