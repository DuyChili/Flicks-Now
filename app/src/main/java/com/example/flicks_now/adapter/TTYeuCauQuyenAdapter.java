package com.example.flicks_now.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ItemTtYeucauBinding;
import com.example.flicks_now.model.LichSuCapQuyen;
import com.example.flicks_now.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TTYeuCauQuyenAdapter extends RecyclerView.Adapter<TTYeuCauQuyenAdapter.TTYeuCauQuyenViewHolder> {
    private Context context;
    private List<LichSuCapQuyen> yeuCauList;
    private OnItemClickListener listener;
    private DatabaseReference userRef;

    public TTYeuCauQuyenAdapter(Context context, List<LichSuCapQuyen> yeuCauList) {
        this.context = context;
        this.yeuCauList = yeuCauList;
        this.userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TTYeuCauQuyenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTtYeucauBinding binding = ItemTtYeucauBinding.inflate(inflater, parent, false);
        return new TTYeuCauQuyenViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TTYeuCauQuyenViewHolder holder, int position) {
        LichSuCapQuyen yeuCau = yeuCauList.get(position);
        // Thiết lập màu nền dựa trên idTrangThai
        if (yeuCau.getIdTrangThai() == 0) {
            // Tin chưa đọc
            holder.binding.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.colorUnread));
        } else if (yeuCau.getIdTrangThai() == 1) {
            // Tin đã đọc
            holder.binding.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.colorRead));
        }
        holder.bind(yeuCau);
        // Hiển thị các thông tin vào TextView
        //holder.binding.tvUserRequest.setText(yeuCau.getId_userYeuCau());
        holder.binding.tvUpdateDate.setText(yeuCau.getNgayUpdater());
        // Lưu vị trí mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return yeuCauList != null ? yeuCauList.size() : 0;
    }

    public class TTYeuCauQuyenViewHolder extends RecyclerView.ViewHolder {

        private final ItemTtYeucauBinding binding;
        int position;

        public TTYeuCauQuyenViewHolder(@NonNull ItemTtYeucauBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(v -> {
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(yeuCauList.get(position));
                }
            });
        }
        public void bind(LichSuCapQuyen yeuCau) {
            final Map<String, String> userNames = new HashMap<>();

            userRef.orderByChild("id_user").equalTo(yeuCau.getId_userYeuCau()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                userNames.put(user.getId_user(), user.getName());
                                binding.tvUserRequest.setText(user.getName());

                            } else {
                                binding.tvUserRequest.setText("Unknown User");
                            }
                        }
                    } else {
                        binding.tvUserRequest.setText("User Not Found");
                    }
                    userRef.orderByChild("id_user").equalTo(yeuCau.getId_userCanUpdate()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    User user = userSnapshot.getValue(User.class);
                                    if (user != null) {
                                        userNames.put(user.getId_user(), user.getName());
                                    }
                                }
                            } else {
                                userNames.put(yeuCau.getId_userCanUpdate(), "User Not Found");
                            }

                            // Hiển thị thông tin sau khi đã lấy đủ hai tên
                            String tenNguoiThayDoi = userNames.getOrDefault(yeuCau.getId_userYeuCau(), "User Not Found");
                            String tenNguoiBiThayDoi = userNames.getOrDefault(yeuCau.getId_userCanUpdate(), "User Not Found");

                            binding.tvRequestContent.setText(tenNguoiThayDoi + " đã thay đổi quyền của " + tenNguoiBiThayDoi);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            binding.tvUserRequest.setText("Error Loading");
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.tvUserRequest.setText("Error Loading");
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(LichSuCapQuyen yeuCauChangeQuyen);
    }
}
