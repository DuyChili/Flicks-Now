package com.example.flicks_now.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.model.ApiModel;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ItemApiBinding;

import java.util.List;

public class ApiAdapter extends RecyclerView.Adapter<ApiAdapter.ApiViewHolder> {

    private List<ApiModel> apiList;
    private OnApiActionListener listener;

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
                // Hiển thị EditText và nút Save khi click vào nút Edit
                binding.editApiName.setVisibility(View.VISIBLE);
                binding.editApiUrl.setVisibility(View.VISIBLE);
                binding.btnSave.setVisibility(View.VISIBLE);
                binding.editApiName.setText(api.getName());
                binding.editApiUrl.setText(api.getUrl());

                // Khi nhấn nút Save, gọi sự kiện sửa và ẩn các thành phần
                binding.btnSave.setOnClickListener(saveView -> {
                    String newName = binding.editApiName.getText().toString().trim();
                    String newUrl = binding.editApiUrl.getText().toString().trim();
                    if (!newName.isEmpty() && !newUrl.isEmpty()) {
                        api.setName(newName); // Cập nhật tên mới cho API
                        api.setUrl(newUrl); // Cập nhật URL mới cho API
                        listener.onEditClick(api); // Gọi sự kiện sửa với API đã được chỉnh sửa
                        notifyItemChanged(getAdapterPosition()); // Cập nhật lại item đã sửa
                    }
                    // Ẩn các trường sau khi lưu
                    binding.editApiName.setVisibility(View.GONE);
                    binding.editApiUrl.setVisibility(View.GONE);
                    binding.btnSave.setVisibility(View.GONE);
                });
            });
        }
    }
}




