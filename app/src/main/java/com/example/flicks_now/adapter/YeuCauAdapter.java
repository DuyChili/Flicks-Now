package com.example.flicks_now.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ItemYeuCauBinding;
import com.example.flicks_now.model.User;
import com.example.flicks_now.model.YeuCau;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class YeuCauAdapter extends RecyclerView.Adapter<YeuCauAdapter.YeuCauViewHolder> {
    private List<YeuCau> yeuCauList;
    private OnItemClickListener listener;
    private DatabaseReference userRef;

    public YeuCauAdapter(List<YeuCau> yeuCauList) {
        this.yeuCauList = yeuCauList;
        this.userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public YeuCauViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemYeuCauBinding binding = ItemYeuCauBinding.inflate(inflater, parent, false);
        return new YeuCauViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull YeuCauViewHolder holder, int position) {
        YeuCau yeuCau = yeuCauList.get(position);
        holder.bind(yeuCau);
        Resources resources = holder.itemView.getResources();
        // Đổi màu trạng thái: cam nếu chưa xử lý, xanh nếu đã xử lý
        if (yeuCau.getIdTrangThai() == 0) {
            holder.binding.tvTrangThai.setText("Chưa xử lý");
            holder.binding.tvTrangThai.setBackgroundResource(R.drawable.status_pending_background);
            holder.binding.tvTrangThai.setTextColor(resources.getColor(R.color.status_pending_text));

            // Thêm hiệu ứng rung nhẹ để thu hút sự chú ý
            Animation shake = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.shake_animation);
            holder.binding.tvTrangThai.startAnimation(shake);
        } else {
            holder.binding.tvTrangThai.setText("Đã xử lý");
            holder.binding.tvTrangThai.setBackgroundResource(R.drawable.status_completed_background);
            holder.binding.tvTrangThai.setTextColor(resources.getColor(R.color.status_completed_text));

            holder.binding.tvTrangThai.clearAnimation();
        }
        // Lưu vị trí mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return yeuCauList != null ? yeuCauList.size() : 0;
    }

    public class YeuCauViewHolder extends RecyclerView.ViewHolder {
        private final ItemYeuCauBinding binding;
        int position;

        public YeuCauViewHolder(@NonNull ItemYeuCauBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(yeuCauList.get(position));
                }
            });
        }

        public void bind(YeuCau yeuCau) {
            userRef.orderByChild("id_user").equalTo(yeuCau.getIdUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                binding.tvUserName.setText(user.getName());
                            } else {
                                binding.tvUserName.setText("Unknown User");
                            }
                        }
                    } else {
                        binding.tvUserName.setText("User Not Found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.tvUserName.setText("Error Loading");
                }
            });

            binding.tvMaUser.setText(yeuCau.getIdUser());
            binding.tvContent.setText(yeuCau.getContent());
            binding.tvTrangThai.setText(yeuCau.getIdTrangThai() == 0 ? "Chưa xử lý" : "Đã xử lý");
        }
    }

    public interface OnItemClickListener {
        void onItemClick(YeuCau yeuCau);
    }
}
