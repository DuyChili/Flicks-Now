package com.example.flicks_now.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.databinding.ItemThanhToanBinding;
import com.example.flicks_now.model.LichSuThanhToan;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LSThanhToanAdapter extends RecyclerView.Adapter<LSThanhToanAdapter.ViewHolder> {

    private List<LichSuThanhToan> thanhToanList;
    private Context context;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    public LSThanhToanAdapter(Context context, List<LichSuThanhToan> thanhToanList) {
        this.context = context;
        this.thanhToanList = thanhToanList;
    }

    // Phương thức để thiết lập listener
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        recyclerViewItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout using View Binding
        ItemThanhToanBinding binding = ItemThanhToanBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichSuThanhToan thanhToan = thanhToanList.get(position);

        // Truy vấn người dùng từ Firebase dựa trên id_user tự tạo
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.orderByChild("id_user").equalTo(thanhToan.getIdUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy tên của người dùng đầu tiên tìm thấy
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userName = userSnapshot.child("name").getValue(String.class);
                        holder.binding.tvUserName.setText(userName != null ? userName : "Tên không xác định");
                        break; // Thoát khỏi vòng lặp sau khi tìm thấy người dùng đầu tiên
                    }
                } else {
                    holder.binding.tvUserName.setText("Tên không xác định");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.binding.tvUserName.setText("Lỗi kết nối");
            }
        });


        holder.binding.tvMaUser.setText(thanhToan.getIdUser());
        holder.binding.tvNoiDung.setText(thanhToan.getNoiDung());
        holder.binding.tvNgayThanhToan.setText(thanhToan.getNgayThanhToan());
        holder.binding.tvNgayXacNhan.setText(thanhToan.getNgayXacNhan());
        holder.binding.tvSoTien.setText(String.valueOf(thanhToan.getSoTien()));


        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return thanhToanList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Khai báo binding
        ItemThanhToanBinding binding;
        int position;
        public ViewHolder(ItemThanhToanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Thiết lập sự kiện click vào item
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
    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}

