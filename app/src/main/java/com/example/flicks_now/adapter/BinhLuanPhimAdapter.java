package com.example.flicks_now.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.model.BinhLuanPhim;
import com.example.flicks_now.databinding.ItemBinhluanphimBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BinhLuanPhimAdapter extends RecyclerView.Adapter<BinhLuanPhimAdapter.MovieViewHolder> {
    private Activity context;
    private List<BinhLuanPhim> binhLuanPhimList;
    private OnCommentDeleteListener deleteListener;

    public BinhLuanPhimAdapter(Activity context, List<BinhLuanPhim> binhLuanPhims, OnCommentDeleteListener listener) {
        this.context = context;
        this.binhLuanPhimList = binhLuanPhims;
        this.deleteListener = listener;
    }
    public void removeComment(int position) {
        binhLuanPhimList.remove(position);
        notifyItemRemoved(position);
    }
    public String getCommentUserId(int position) {
        return binhLuanPhimList.get(position).getUserId();
    }
    public String getCommentText(int position) {
        return binhLuanPhimList.get(position).getCommentText(); // Giả sử bạn có phương thức getCommentText() trong lớp BinhLuanPhim
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBinhluanphimBinding binding = ItemBinhluanphimBinding.inflate(context.getLayoutInflater(), parent, false);
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        BinhLuanPhim binhLuanPhim = binhLuanPhimList.get(position);
        holder.binding.tvTenNguoiDung.setText(binhLuanPhim.getUserName());
        holder.binding.tvBinhLuan.setText(binhLuanPhim.getCommentText());
        // Định dạng thời gian tùy theo yêu cầu
        // Tạo Date từ timestamp
        Date date = new Date(binhLuanPhim.timestamp);

// Định dạng ngày tháng năm
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String ngayThangNam = dateFormat.format(date);

// Định dạng giờ:phút:giây
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String gioPhutGiay = timeFormat.format(date);

        holder.binding.tvNgayBinhLuan.setText(ngayThangNam);
        holder.binding.tvThoiGianBinhLuan.setText(gioPhutGiay);
    }

    @Override
    public int getItemCount() {
        return binhLuanPhimList.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        ItemBinhluanphimBinding binding;
        public MovieViewHolder(@NonNull ItemBinhluanphimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            // Gọi hàm xóa bình luận khi nút được nhấn
            binding.btnXoaBinhLuan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition(); // Lấy vị trí hiện tại của ViewHolder
                    if (position != RecyclerView.NO_POSITION) { // Kiểm tra vị trí hợp lệ
                        deleteListener.xoaBinhLuan(position); // Gọi hàm xóa bình luận
                    }
                }
            });
        }
    }
    // Khai báo interface
    public interface OnCommentDeleteListener {
        void xoaBinhLuan(int position);
    }

}