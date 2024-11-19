package com.example.flicks_now.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.adapter.QuocGiaAdapter;
import com.example.flicks_now.databinding.ActivityQlquocGiaBinding;
import com.example.flicks_now.model.QuocGia;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QLQuocGiaActivity extends AppCompatActivity {
    private ActivityQlquocGiaBinding binding;
    private QuocGiaAdapter quocGiaAdapter;
    private List<QuocGia> danhSachQuocGia;
    private DatabaseReference quocGiaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQlquocGiaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
// Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Quốc Gia"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
        // Cấu hình RecyclerView
        binding.rvQlyQuocGia.setLayoutManager(new LinearLayoutManager(this));
        danhSachQuocGia = new ArrayList<>();
        quocGiaAdapter = new QuocGiaAdapter(danhSachQuocGia, this);
        binding.rvQlyQuocGia.setAdapter(quocGiaAdapter);

        // Kết nối Firebase
        quocGiaRef = FirebaseDatabase.getInstance().getReference("quocGia");
        hienThiDanhSachQuocGia();

        // Xử lý sự kiện khi nhấn nút thêm
        binding.btnAdd.setOnClickListener(v -> guiQuocGiaMoi());
    }

    // Gửi thông tin quốc gia mới
    private void guiQuocGiaMoi() {
        String tenQuocGia = binding.etQuocGia.getText().toString().trim();
        String linkAnh = binding.etLinkAnh.getText().toString().trim();

        if (tenQuocGia.isEmpty() || linkAnh.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin quốc gia và link ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }

        luuQuocGiaVaoFirebase(tenQuocGia, linkAnh);
    }

    // Lưu thông tin quốc gia vào Realtime Database
    private void luuQuocGiaVaoFirebase(String tenQuocGia, String linkAnh) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("quocGia");

        // Lấy danh sách quốc gia hiện có để tính toán ID tiếp theo
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long maxId = 0;

                // Lặp qua tất cả bản ghi hiện có để tìm ID lớn nhất
                for (DataSnapshot quocGiaSnapshot : snapshot.getChildren()) {
                    Long currentId = quocGiaSnapshot.child("id").getValue(Long.class);
                    if (currentId != null && currentId > maxId) {
                        maxId = currentId;  // Tìm ID lớn nhất
                    }
                }

                // Tăng ID lớn nhất lên 1 cho quốc gia mới
                long newId = maxId + 1;

                // Tạo HashMap lưu dữ liệu quốc gia
                HashMap<String, Object> duLieu = new HashMap<>();
                duLieu.put("id", newId);  // Dùng ID mới tính toán
                duLieu.put("name", tenQuocGia);
                duLieu.put("imageLink", linkAnh);



                // Lưu quốc gia mới vào Firebase với ID mới
                databaseRef.child(String.valueOf(newId)).setValue(duLieu)
                        .addOnSuccessListener(aVoid -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(QLQuocGiaActivity.this);
                            builder.setTitle("Thông báo");
                            builder.setMessage("Thêm quốc gia thành công.");
                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                            builder.create().show();
                            datLaiForm();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(QLQuocGiaActivity.this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QLQuocGiaActivity.this, "Lỗi khi lấy dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





    // Hiển thị danh sách quốc gia từ Firebase
    private void hienThiDanhSachQuocGia() {
        quocGiaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                danhSachQuocGia.clear(); // Xóa danh sách cũ
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    QuocGia quocGia = dataSnapshot.getValue(QuocGia.class);
                    danhSachQuocGia.add(quocGia);
                }
                // Cập nhật RecyclerView
                quocGiaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QLQuocGiaActivity.this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Đặt lại form sau khi thêm thành công
    private void datLaiForm() {
        binding.etQuocGia.setText("");
        binding.etLinkAnh.setText("");
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Xóa cờ giữ màn hình sáng khi ứng dụng không còn hoạt động
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
