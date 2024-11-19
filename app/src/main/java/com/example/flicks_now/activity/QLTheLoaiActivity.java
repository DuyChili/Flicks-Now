package com.example.flicks_now.activity;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.flicks_now.R;
import com.example.flicks_now.adapter.CategoryAdapter;
import com.example.flicks_now.databinding.ActivityQltheLoaiBinding;
import com.example.flicks_now.databinding.DialogAddCategoryBinding;
import com.example.flicks_now.model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QLTheLoaiActivity extends AppCompatActivity {

    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();
    private ActivityQltheLoaiBinding binding;
    private DatabaseReference databaseReference;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ánh xạ layout của Activity với View Binding
        binding = ActivityQltheLoaiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Thể Loại"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Khởi tạo Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("theLoai");

        // Khởi tạo danh sách và RecyclerView
        categoryList = new ArrayList<>();
        xulyRecyclerView();
        xulyThem();

        // Đọc dữ liệu từ Firebase
        docDuLieuFirebase();
    }

    private void showCategoryDialog(int position, String currentName) {
        // Tạo dialog và sử dụng View Binding cho dialog
        Dialog dialog = new Dialog(QLTheLoaiActivity.this);
        DialogAddCategoryBinding dialogBinding = DialogAddCategoryBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Đặt tên thể loại hiện tại vào EditText nếu là sửa
        if (position != -1) { // Nếu position khác -1 thì là sửa
            dialogBinding.editTextCategoryName.setText(currentName);
            dialogBinding.buttonAddCategory.setText("Cập nhật");
        } else {
            dialogBinding.editTextCategoryName.setText(""); // Nếu là thêm, thì để trống
        }

        // Set background bo tròn cho dialog
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        dialog.getWindow().setLayout(800, 600); // Tùy chỉnh kích thước

        // Set sự kiện khi nhấn nút "Thêm/Cập nhật" trong dialog
        dialogBinding.buttonAddCategory.setOnClickListener(v -> {
            String newCategoryName = dialogBinding.editTextCategoryName.getText().toString();
            if (!newCategoryName.isEmpty()) {
                if (position != -1) {

                    // Nếu là sửa
                    new AlertDialog.Builder(QLTheLoaiActivity.this)
                            .setTitle("Xác nhận cập nhật")
                            .setMessage("Bạn có muốn cập nhật thể loại này không?")
                            .setPositiveButton("Có", (dialog1, which) -> {
                                updateCategory(position, newCategoryName);
                            })
                            .setNegativeButton("Không", null) // Không làm gì nếu nhấn "Không"
                            .show();

                } else {
                    // Nếu là thêm
                    new AlertDialog.Builder(QLTheLoaiActivity.this)
                            .setTitle("Xác nhận thêm")
                            .setMessage("Bạn có muốn thêm thể loại này không?")
                            .setPositiveButton("Có", (dialog1, which) -> {
                                themTheLoaiFirebase(newCategoryName);
                            })
                            .setNegativeButton("Không", null) // Không làm gì nếu nhấn "Không"
                            .show();
                }
                dialog.dismiss(); // Đóng dialog sau khi thêm hoặc cập nhật
            } else {
                Toast.makeText(QLTheLoaiActivity.this, "Vui lòng nhật tên thể loại", Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị dialog
        dialog.show();
    }
private void deleteCategory(int position) {
    // Lấy ID từ categoryList
    String categoryId = categoryList.get(position).getId();
    String categoryName = categoryList.get(position).getName(); // Lấy tên thể loại (Hành Động, Tình Cảm)

    // Truy vấn Firebase Movies để kiểm tra thể loại có xuất hiện trong phim nào không
    FirebaseDatabase.getInstance()
            .getReference("Movies")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean hasMatchingCategory = false;

                    // Kiểm tra từng phim trong Firebase
                    for (DataSnapshot movieSnapshot : dataSnapshot.getChildren()) {
                        String theLoai = movieSnapshot.child("theLoai").getValue(String.class);

                        if (theLoai != null) {
                            // Cắt chuỗi theLoai thành mảng và kiểm tra xem categoryName có tồn tại không
                            String[] categories = theLoai.split(", ");
                            for (String category : categories) {
                                if (category.trim().equals(categoryName)) {
                                    hasMatchingCategory = true;
                                    break;
                                }
                            }
                        }

                        if (hasMatchingCategory) {
                            break;
                        }
                    }

                    if (hasMatchingCategory) {
                        // Nếu tìm thấy phim có thể loại này, không xóa thể loại và thông báo
                        Toast.makeText(QLTheLoaiActivity.this,
                                "Không thể xóa thể loại vì có phim thuộc thể loại này",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Nếu không có phim nào thuộc thể loại này, tiến hành xóa
                        // Xóa thể loại khỏi Firebase
                        FirebaseDatabase.getInstance()
                                .getReference("theLoai")
                                .child(categoryId) // Xóa thể loại khỏi Firebase
                                .removeValue();

                        // Xóa thể loại khỏi danh sách và cập nhật RecyclerView
                        categoryList.remove(position);
                        categoryAdapter.notifyItemRemoved(position);

                        // Thông báo thành công
                        Toast.makeText(QLTheLoaiActivity.this, "Xóa thể loại thành công", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Xử lý lỗi nếu có
                    Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi kiểm tra thể loại", Toast.LENGTH_SHORT).show();
                }
            });
}




    // sửa thể loại
    private void updateCategory(int position, String newCategoryName) {
        // Lấy ID từ categoryList
        String categoryId = categoryList.get(position).getId();

        // Cập nhật tên thể loại trong Firebase dựa trên ID
        FirebaseDatabase.getInstance()
                .getReference("theLoai")
                .child(categoryId) // Cập nhật theo ID từ Firebase
                .child("name")
                .setValue(newCategoryName);

        // Cập nhật tên trong danh sách và RecyclerView
        categoryList.get(position).name = newCategoryName;
        categoryAdapter.notifyItemChanged(position);

        Toast.makeText(QLTheLoaiActivity.this, "Cập nhật thể loại thành công", Toast.LENGTH_SHORT).show();
    }


    private void xulyThem() {
        binding.btnAddTheLoai.setOnClickListener(v -> {
            // Gọi dialog để thêm thể loại
            showCategoryDialog(-1, null); // -1 cho biết là thêm
        });
    }

    private void xulyRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 cột

        // Khởi tạo adapter và set cho RecyclerView
        categoryAdapter = new CategoryAdapter(this, categoryList, new CategoryAdapter.OnEditClickListener() {
            @Override
            public void onEditClick(int position, Category currentCategory) {
                // Truyền cả đối tượng Category (bao gồm id và name)
                showCategoryDialog(position, currentCategory.getName());
            }
        }, new CategoryAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteCategory(position); // Gọi phương thức xóa
            }
        });

        binding.recyclerView.setAdapter(categoryAdapter);
    }


    private void docDuLieuFirebase() {
        // Lắng nghe dữ liệu từ Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryList.clear(); // Xóa dữ liệu cũ

                // Duyệt qua tất cả các thể loại trong "theLoai"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị "id" và "name" từ từng mục
                    String id = snapshot.getKey(); // Sử dụng key từ Firebase
                    String categoryName = snapshot.child("name").getValue(String.class);

                    // Thêm đối tượng Category vào danh sách
                    categoryList.add(new Category(id, categoryName));
                }

                // Cập nhật adapter
                categoryAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi (nếu có)
                Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi lấy dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void themTheLoaiFirebase(String categoryName) {
        // Lấy số lượng thể loại hiện tại để tạo id mới
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount(); // Đếm số lượng hiện có để tạo id mới
                // Tạo id mới
                String newId = String.valueOf(count + 1); // Nếu count bắt đầu từ 0, id đầu tiên sẽ là 1

                boolean idExists;
                do {
                    idExists = false;
                    for (DataSnapshot theloaiSnapshot : dataSnapshot.getChildren()) {
                        Object idloaiObj = theloaiSnapshot.child("id").getValue();
                        String idloai = idloaiObj != null ? String.valueOf(idloaiObj) : "";
                        if (newId.equals(idloai)) {
                            int newIdInt = Integer.parseInt(newId);
                            newIdInt++; // Tăng giá trị id nếu bị trùng
                            newId = String.valueOf(newIdInt);
                            idExists = true;
                            break;
                        }
                    }
                } while (idExists);


                // Tạo một HashMap để thêm dữ liệu
                HashMap<String, Object> newCategory = new HashMap<>();
                newCategory.put("id", newId);  // Sử dụng id vừa tạo
                newCategory.put("name", categoryName);

                // Thêm thể loại mới vào Firebase
                databaseReference.child(newId).setValue(newCategory)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(QLTheLoaiActivity.this, "Thêm thể loại thành công", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi thêm thể loại", Toast.LENGTH_SHORT).show()
                        );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Thiết lập OnBackPressedDispatcher
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                finishAffinity();  // Thoát ứng dụng
                return;
            }
            doubleBackToExitPressedOnce = true;
            Toast.makeText(getApplicationContext(), "Nhấn thoát thêm một lần nữa", Toast.LENGTH_SHORT).show();

            // Reset lại cờ sau 2 giây
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng tài nguyên của binding khi Activity bị hủy
        binding = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
