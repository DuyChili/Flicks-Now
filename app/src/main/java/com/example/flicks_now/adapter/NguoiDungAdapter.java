package com.example.flicks_now.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.flicks_now.databinding.ItemUserThongbaoBinding;
import com.example.flicks_now.model.User;

import java.util.ArrayList;
import java.util.List;

public class NguoiDungAdapter extends RecyclerView.Adapter<NguoiDungAdapter.MovieViewHolder> {
    private Activity context;
    private List<User> nguoiDungList;
    private List<User> selectedUsers = new ArrayList<>();

    public NguoiDungAdapter(Activity context, List<User> nguoiDung) {
        this.context = context;
        this.nguoiDungList = nguoiDung;
    }

    // Trả về danh sách người dùng đã chọn
    public List<User> getSelectedUsers() {
        return selectedUsers;
    }
    public void resetCheckboxes() {
        selectedUsers.clear(); // Xóa danh sách người dùng đã chọn
        for (User user : nguoiDungList) {
            user.setChecked(false); // Đặt lại trạng thái checkbox
        }
        notifyDataSetChanged(); // Cập nhật RecyclerView để hiển thị trạng thái mới
    }



    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserThongbaoBinding binding = ItemUserThongbaoBinding.inflate(context.getLayoutInflater(), parent, false);
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        User nguoiDung = nguoiDungList.get(position);
        holder.binding.tennguoidung.setText(nguoiDung.getName());
        // Đặt trạng thái của CheckBox dựa trên danh sách người dùng được chọn
        holder.binding.checkboxExample.setChecked(selectedUsers.contains(nguoiDung));

        // Xử lý thay đổi trạng thái CheckBox
        holder.binding.checkboxExample.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                selectedUsers.add(nguoiDung);  // Thêm vào danh sách người dùng đã chọn
            } else {
                selectedUsers.remove(nguoiDung);  // Bỏ ra khỏi danh sách người dùng đã chọn
            }
        });


        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return nguoiDungList.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        ItemUserThongbaoBinding binding;
        int position;
        public MovieViewHolder(@NonNull ItemUserThongbaoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}
