package com.example.flicks_now.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flicks_now.activity.QLHoTroActivity;
import com.example.flicks_now.model.HoTro;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ItemHotroBinding;

import java.util.List;

public class HoTroAdapter extends RecyclerView.Adapter<HoTroAdapter.ThongBaoViewHolder> {
    private Activity context;
    private List<HoTro> hoTroList; // Danh sách thông báo

    public HoTroAdapter(Activity context, List<HoTro> hoTro) {
        this.context = context;
        this.hoTroList = hoTro;
    }

    @NonNull
    @Override
    public ThongBaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHotroBinding binding = ItemHotroBinding.inflate(context.getLayoutInflater(), parent, false);
        return new ThongBaoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThongBaoViewHolder holder, int position) {
        HoTro hoTro = hoTroList.get(position);
        if (hoTro == null) {
            return;
        }

        // Cài đặt dữ liệu cho từng phần tử trong danh sách
        holder.binding.tvId.setText(hoTro.getUserId());
        holder.binding.tvTitle.setText(hoTro.getName());
        holder.binding.tvTime.setText(hoTro.getTime());
        holder.binding.tvContent.setText(hoTro.getDescription());
        String imageUrl = hoTro.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .error(R.drawable.ic_notification) // Image to show on error
                    .into(holder.binding.imgIcon);
        } else {
            holder.binding.imgIcon.setImageResource(R.drawable.ic_notification); // Default image
        }

        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        if (hoTroList != null) {
            return hoTroList.size();
        }
        return 0;
    }

    public class ThongBaoViewHolder extends RecyclerView.ViewHolder {
        ItemHotroBinding binding;
        int position;
        public ThongBaoViewHolder(@NonNull ItemHotroBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                // Tạo Intent để chuyển sang HoTroDetailActivity
                Intent intent = new Intent(context, QLHoTroActivity.class);
                HoTro hoTro = hoTroList.get(position);
                intent.putExtra("title", hoTro.getName());
                intent.putExtra("time", hoTro.getTime());
                intent.putExtra("content", hoTro.getDescription());
                intent.putExtra("imageUrl", hoTro.getImageUrl());
                intent.putExtra("userId", hoTro.getUserId());
                context.startActivity(intent);;
            });
        }
    }
}
