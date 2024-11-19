package com.example.flicks_now.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ItemQuocgiaBinding;
import com.example.flicks_now.model.QuocGia;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class QuocGiaAdapter extends RecyclerView.Adapter<QuocGiaAdapter.QuocGiaViewHolder> {

    private List<QuocGia> quocGiaList;
    private Activity context;
    private DatabaseReference quocGiaRef;

    public QuocGiaAdapter(List<QuocGia> quocGiaList, Activity context) {
        this.quocGiaList = quocGiaList;
        this.context = context;
        quocGiaRef = FirebaseDatabase.getInstance().getReference("quocGia");
    }

    @NonNull
    @Override
    public QuocGiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuocgiaBinding binding = ItemQuocgiaBinding.inflate(LayoutInflater.from(context), parent, false);
        return new QuocGiaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuocGiaViewHolder holder, int position) {
        QuocGia quocGia = quocGiaList.get(position);
        holder.binding.editQuocGia.setText(quocGia.getName());
        holder.binding.tvQuocGiaName.setText(quocGia.getName());

        // Hiển thị logo theo quốc gia
        // Sử dụng Glide để hiển thị hình ảnh
        Glide.with(holder.itemView.getContext())
                .load(quocGia.getImageLink()) // URL hình ảnh
                .error(R.drawable.vietnam) // Hình ảnh hiển thị khi lỗi
                .into(holder.binding.imgQuocGia);

        // Set the position for further use in the ViewHolder
        final int pos = position;
        holder.position = pos;
        holder.bind(quocGia);
    }

    @Override
    public int getItemCount() {
        return quocGiaList.size();
    }

    public class QuocGiaViewHolder extends RecyclerView.ViewHolder {
        ItemQuocgiaBinding binding;
        int position;

        public QuocGiaViewHolder(@NonNull ItemQuocgiaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public void bind (QuocGia quocgia){
            // Xử lý nút Sửa
            binding.btnEdit.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc chắn muốn sửa thông tin quốc gia này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Hiển thị form chỉnh sửa
                            binding.editQuocGia.setVisibility(View.VISIBLE);
                            binding.editLink.setVisibility(View.VISIBLE);
                            binding.btnSave.setVisibility(View.VISIBLE);
                            binding.editQuocGia.requestFocus();
                            binding.editQuocGia.setText(quocGiaList.get(position).getName());
                            binding.editLink.setText(quocGiaList.get(position).getImageLink());
                        })
                        .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                        .show();
            });

            binding.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc chắn muốn xóa quốc gia này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Lấy ID và tên quốc gia cần xóa
                            long quocGiaId = quocGiaList.get(position).getId();
                            String quocGiaName = quocGiaList.get(position).getName(); // Lấy tên quốc gia

                            // Truy vấn Firebase Movies để kiểm tra có bộ phim nào sử dụng quốc gia này không
                            FirebaseDatabase.getInstance()
                                    .getReference("Movies")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            boolean hasMovieWithCountry = false;

                                            // Kiểm tra từng bộ phim trong Firebase
                                            for (DataSnapshot movieSnapshot : dataSnapshot.getChildren()) {
                                                String quocGia = movieSnapshot.child("quocGia").getValue(String.class);

                                                if (quocGia != null && quocGia.equals(quocGiaName)) {
                                                    hasMovieWithCountry = true;
                                                    break;
                                                }
                                            }

                                            if (hasMovieWithCountry) {
                                                // Nếu có bộ phim nào sử dụng quốc gia này, không xóa quốc gia
                                                Toast.makeText(context,
                                                        "Không thể xóa quốc gia này vì có phim thuộc quốc gia này",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Nếu không có bộ phim nào sử dụng quốc gia này, tiến hành xóa
                                                quocGiaRef.child(String.valueOf(quocGiaId)).removeValue()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(context, "Đã xóa quốc gia!", Toast.LENGTH_SHORT).show();
                                                            if (position >= 0 && position < quocGiaList.size()) {
                                                                quocGiaList.remove(position);  // Xóa quốc gia khỏi danh sách
                                                                notifyItemRemoved(position);  // Cập nhật lại RecyclerView
                                                            } else {
                                                                Toast.makeText(context, "Lỗi vị trí, không thể xóa!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Lỗi khi xóa quốc gia!", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Xử lý lỗi nếu có
                                            Toast.makeText(context, "Lỗi khi kiểm tra quốc gia", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                        .show();
            });



            // Xử lý nút Lưu
            binding.btnSave.setOnClickListener(v -> {
                String newName = binding.editQuocGia.getText().toString();
                String newImageLink = binding.editLink.getText().toString();

                if (!newName.isEmpty() && !newImageLink.isEmpty()) {
                    String quocGiaId = String.valueOf(quocgia.getId());
                    quocGiaRef.child(quocGiaId).child("name").setValue(newName);
                    quocGiaRef.child(quocGiaId).child("imageLink").setValue(newImageLink)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã cập nhật quốc gia và link hình ảnh!", Toast.LENGTH_SHORT).show();
                                // Cập nhật trong danh sách
                                quocgia.setName(newName);
                                quocgia.setImageLink(newImageLink);

                                // Ẩn các trường nhập liệu
                                binding.editQuocGia.setVisibility(View.GONE);
                                binding.editLink.setVisibility(View.GONE);
                                binding.btnSave.setVisibility(View.GONE);

                                // Cập nhật lại item trong RecyclerView
                                notifyItemChanged(position);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Lỗi khi cập nhật dữ liệu!", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(context, "Tên quốc gia và link hình ảnh không được để trống!", Toast.LENGTH_SHORT).show();
                }
            });

            // Lắng nghe sự kiện nhấn ra ngoài để ẩn các trường chỉnh sửa
            binding.getRoot().setOnTouchListener((v, event) -> {
                // Kiểm tra xem có phải là sự kiện nhấn ra ngoài vùng chỉnh sửa không
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Nếu người dùng nhấn ra ngoài, ẩn các trường nhập liệu
                    binding.editQuocGia.setVisibility(View.GONE);
                    binding.editLink.setVisibility(View.GONE);
                    binding.btnSave.setVisibility(View.GONE);
                }
                return false;
            });
        }
    }
}
