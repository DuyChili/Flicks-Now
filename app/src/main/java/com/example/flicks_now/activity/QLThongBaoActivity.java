package com.example.flicks_now.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.adapter.NguoiDungAdapter;
import com.example.flicks_now.model.ThongBao;
import com.example.flicks_now.model.User;
import com.example.flicks_now.databinding.ActivityQlThongBaoBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QLThongBaoActivity extends AppCompatActivity {
    private ActivityQlThongBaoBinding binding;
    private DatabaseReference mDatabase;
    private List<User> nguoiDungList = new ArrayList<>();
    private NguoiDungAdapter adapter;
    private String idUser;
    private DatabaseReference thongBaoRef;
    boolean trangthai = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityQlThongBaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> onBackPressed());


        // Khởi tạo Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding.btnSaveChanges.setOnClickListener(v -> saveThongBaoToDatabase());

        binding.rcvNguoiDung.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NguoiDungAdapter(QLThongBaoActivity.this,nguoiDungList);
        binding.rcvNguoiDung.setAdapter(adapter);
        getNguoiDungFromDatabase();

        // Khởi tạo Firebase Realtime Database
        thongBaoRef = FirebaseDatabase.getInstance().getReference().child("ThongBao");
        listenForNotifications();
    }


    private void saveThongBaoToDatabase() {
        // Lấy dữ liệu từ các trường nhập liệu
        String idThongBao = mDatabase.child("ThongBao").push().getKey();
        String title = binding.edtTitle.getText().toString().trim();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String content = binding.edtNoiDung.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy danh sách người dùng đã chọn
        List<User> selectedUsers = adapter.getSelectedUsers();
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn người dùng để gửi thông báo!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi thông báo và lưu vào Firebase cho từng người dùng được chọn
        for (User user : selectedUsers) {
            // Tạo đối tượng ThongBao với userId là ID người dùng bạn muốn gửi thông báo
            ThongBao thongBao = new ThongBao(idThongBao,user.getId_user(), title, time, content);

            // Lưu thông báo vào bảng ThongBao
            DatabaseReference thongBaoRef = FirebaseDatabase.getInstance().getReference().child("ThongBao").push(); // Tạo ID tự động cho thông báo
            thongBaoRef.setValue(thongBao)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(QLThongBaoActivity.this, "Đã gửi thông báo cho " + user.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(QLThongBaoActivity.this, "Lỗi khi gửi thông báo cho " + user.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        resetForm();
        adapter.resetCheckboxes();
    }



    private void getNguoiDungFromDatabase() {
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nguoiDungList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User nguoiDung = userSnapshot.getValue(User.class);
                    String id_user = userSnapshot.child("id_user").getValue(String.class); // Lấy id_user từ Firebase
                    if (nguoiDung != null) {
                        nguoiDung.setId_user(id_user); // Lưu ID vào đối tượng người dùng
                        nguoiDungList.add(nguoiDung);
                    }
                }
                adapter.notifyDataSetChanged(); // Cập nhật adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QLThongBaoActivity.this, "Lỗi khi lấy dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Xóa form sau khi gửi thành công
    private void resetForm() {
        binding.edtTitle.setText("");
        binding.edtNoiDung.setText("");
    }

    private void listenForNotifications() {
        thongBaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot thongBaoSnapshot : snapshot.getChildren()) {
                    ThongBao thongBao = thongBaoSnapshot.getValue(ThongBao.class);
                    if (thongBao != null && thongBao.getId_user().equals(idUser)) { // Kiểm tra userId
                        // Hiển thị thông báo
                        Log.d("QLThongBaoActivity", "Notification received: " + thongBao.getContent());
                        showNotification(thongBao);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QLThongBaoActivity.this, "Lỗi khi lấy thông báo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNotification(ThongBao thongBao) {
        // Kiểm tra nội dung thông báo
        if (thongBao.getContent() == null || thongBao.getContent().isEmpty()) {
            return; // Nếu không có nội dung thì không hiển thị
        }

        Snackbar snackbar = Snackbar.make(binding.getRoot(), thongBao.getContent(), Snackbar.LENGTH_INDEFINITE)
                .setAction("Đóng", v -> {
                    // Đóng snackbar khi người dùng nhấn vào nút "Đóng"
                });

        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Giữ màn hình sáng khi hoạt động
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Tắt giữ màn hình sáng khi dừng hoạt động
    }
}