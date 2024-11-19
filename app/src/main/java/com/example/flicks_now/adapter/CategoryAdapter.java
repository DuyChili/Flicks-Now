package com.example.flicks_now.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.R;
import com.example.flicks_now.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    // Interface để gọi lại khi người dùng nhấn vào nút chỉnh sửa
    public interface OnEditClickListener {
        void onEditClick(int position, Category currentCategory);
    }

    // Interface để gọi lại khi người dùng nhấn vào nút xóa
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public CategoryAdapter(Context context, List<Category> categoryList,
                           OnEditClickListener onEditClickListener, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        // Lấy đối tượng Category hiện tại từ danh sách
        Category category = categoryList.get(position);

        // Hiển thị tên thể loại
        holder.categoryName.setText(category.getName());

        // Xử lý sự kiện click nút chỉnh sửa
        holder.editIcon.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position, category);  // Truyền cả đối tượng Category
            }
        });

        // Xử lý sự kiện click nút xóa
        holder.deleteIcon.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có muốn xóa thể loại này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            onDeleteClickListener.onDeleteClick(position);
                        })
                        .setNegativeButton("Không", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // Lớp ViewHolder để ánh xạ các thành phần giao diện
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView editIcon, deleteIcon;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            editIcon = itemView.findViewById(R.id.edit_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}
