package com.example.flicks_now.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ItemApiBinding;
import com.example.flicks_now.model.ApiModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ApiAdapter extends RecyclerView.Adapter<ApiAdapter.ApiViewHolder> {

    private List<ApiModel> apiList;
    private OnApiActionListener listener;
    private Context context;

    // Khai báo interface để lắng nghe sự kiện
    public interface OnApiActionListener {
        void onEditClick(ApiModel api);
        void onDeleteClick(ApiModel api);
        void onSelectClick(ApiModel api);
    }

    public ApiAdapter(List<ApiModel> apiList, OnApiActionListener listener) {
        this.apiList = apiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng View Binding thay vì inflate layout
        ItemApiBinding binding = ItemApiBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ApiViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ApiViewHolder holder, int position) {
        ApiModel api = apiList.get(position);

        // Sử dụng View Binding để gán dữ liệu và thiết lập sự kiện
        holder.bind(api);
    }

    @Override
    public int getItemCount() {
        return apiList.size();
    }

    public class ApiViewHolder extends RecyclerView.ViewHolder {

        private final ItemApiBinding binding;

        public ApiViewHolder(@NonNull ItemApiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ApiModel api) {
            // Thiết lập logo API dựa trên tên
            if (api.getName().equalsIgnoreCase("Kkphim")) {
                binding.imgApiLogo.setImageResource(R.drawable.logo_kkphim);
            } else if (api.getName().equalsIgnoreCase("ophim")) {
                binding.imgApiLogo.setImageResource(R.drawable.ic_logo_ophim);
            } else {
                binding.imgApiLogo.setImageResource(R.drawable.logo);
            }
            // Sự kiện chọn API khi click vào logo
            binding.imgApiLogo.setOnClickListener(v -> listener.onSelectClick(api));
            // Sự kiện xóa
            binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(api));

            // Sự kiện chỉnh sửa
            binding.btnEdit.setOnClickListener(v -> {
                // Lấy ID của API đang chỉnh sửa khi người dùng nhấn "Edit"
                String apiId = api.getId();
                Log.d("ApiUpdate", "API ID: " + apiId); // Kiểm tra ID trong log

                // Lấy context từ itemView để tránh lỗi null
                Context context = itemView.getContext();

                // Tạo một dialog hỏi người dùng có chắc chắn muốn chỉnh sửa không
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận chỉnh sửa")
                        .setMessage("Bạn có chắc chắn muốn chỉnh sửa thông tin API này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Hiển thị EditText và nút Save khi người dùng xác nhận chỉnh sửa
                            binding.editApiName.setVisibility(View.VISIBLE);
                            binding.editApiUrl.setVisibility(View.VISIBLE);
                            binding.btnSave.setVisibility(View.VISIBLE);
                            binding.editApiName.setText(api.getName());
                            binding.editApiUrl.setText(api.getUrl());

                            // Khi người dùng nhấn vào nút Save
                            binding.btnSave.setOnClickListener(saveView -> {
                                String newName = binding.editApiName.getText().toString().trim();
                                String newUrl = binding.editApiUrl.getText().toString().trim();

                                // Kiểm tra tên và URL hợp lệ
                                if (!newName.isEmpty() && !newUrl.isEmpty()) {
                                    // Cập nhật API model với tên và URL mới
                                    api.setName(newName);
                                    api.setUrl(newUrl);

                                    // Kiểm tra xem apiId có hợp lệ không
                                    if (apiId != null && !apiId.isEmpty()) {
                                        // Truy cập Firebase để cập nhật API
                                        DatabaseReference apiRef = FirebaseDatabase.getInstance().getReference("api_sources/api_list");

                                        // Cập nhật tên và URL của API trong Firebase
                                        apiRef.child(apiId).child("name").setValue(newName);
                                        apiRef.child(apiId).child("url").setValue(newUrl)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Thông báo thành công
                                                    Toast.makeText(context, "API đã được cập nhật!", Toast.LENGTH_SHORT).show();

                                                    // Cập nhật item trong RecyclerView
                                                    notifyItemChanged(getAdapterPosition());
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Thông báo lỗi
                                                    Toast.makeText(context, "Lỗi khi cập nhật API: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // Thông báo lỗi nếu không có ID hợp lệ
                                        Toast.makeText(context, "ID API không hợp lệ!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Thông báo lỗi nếu tên hoặc URL trống
                                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                                }

                                // Ẩn các trường sau khi lưu
                                binding.editApiName.setVisibility(View.GONE);
                                binding.editApiUrl.setVisibility(View.GONE);
                                binding.btnSave.setVisibility(View.GONE);
                            });
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            // Nếu người dùng chọn "Không", không làm gì cả
                            dialog.dismiss();
                        })
                        .show();
            });
            // Lắng nghe sự kiện nhấn ra ngoài để ẩn các trường chỉnh sửa
            binding.getRoot().setOnTouchListener((v, event) -> {
                // Kiểm tra xem có phải là sự kiện nhấn ra ngoài vùng chỉnh sửa không
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Nếu người dùng nhấn ra ngoài, ẩn các trường nhập liệu
                    binding.editApiName.setVisibility(View.GONE);
                    binding.editApiUrl.setVisibility(View.GONE);
                    binding.btnSave.setVisibility(View.GONE);
                }
                return false;
            });

        }
    }
}




