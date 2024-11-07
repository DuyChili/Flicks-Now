package com.example.flicks_now.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.model.User;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ItemUserBinding;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context; // Giả sử bạn có một lớp User để đại diện cho người dùng
    private static OnRecyclerViewItemClickListener  recyclerViewItemClickListener;
    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }
    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        recyclerViewItemClickListener = listener;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        Log.d("UserAdapter", "Binding user: " + user.getName());
        holder.binding.tvUserName.setText(user.getName());
        holder.binding.tvMaUser.setText(user.getId_user());
        holder.binding.tvUserStatus.setText(user.getStatus());
        holder.binding.tvGoi.setText(user.getGoi());
        Log.d("kiểm tra gói", "gói: " + user.getGoi());
        // Đổi màu dựa trên trạng thái (online/offline)
        if (user.getStatus().equals("online")) {
            holder.binding.tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.green));  // Màu xanh lá cho online
        } else {
            holder.binding.tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.red));  // Màu đỏ cho offline
        }

        // Đổi màu dựa trên loại người dùng
        switch (user.getGoi()) {
            case "Thường":
                holder.binding.tvGoi.setBackgroundResource(R.drawable.membership_normal);
                holder.binding.tvGoi.setTextColor(ContextCompat.getColor(context, R.color.normal_text));
                break;
            case "VIP":
                holder.binding.tvGoi.setBackgroundResource(R.drawable.membership_vip);
                holder.binding.tvGoi.setTextColor(ContextCompat.getColor(context, R.color.vip_text));
                break;
            case "Admin":
                holder.binding.tvGoi.setBackgroundResource(R.drawable.membership_admin);
                holder.binding.tvGoi.setTextColor(ContextCompat.getColor(context, R.color.admin_text));
                break;
            case "Quản Lý":
                holder.binding.tvGoi.setBackgroundResource(R.drawable.membership_manager);
                holder.binding.tvGoi.setTextColor(ContextCompat.getColor(context, R.color.manager_text));
                break;
            default:
                holder.binding.tvGoi.setBackgroundResource(R.drawable.membership_normal);
                holder.binding.tvGoi.setTextColor(ContextCompat.getColor(context, R.color.normal_text));
                break;
        }
        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;
        int position;
        public UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Thiết lập sự kiện click cho item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewItemClickListener != null) {
                        recyclerViewItemClickListener.onItemClick(view, getAdapterPosition());
                    }
                }
            });
        }
    }


    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
    // Phương thức để cập nhật dữ liệu và thông báo RecyclerView
    public void updateData(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }



}

