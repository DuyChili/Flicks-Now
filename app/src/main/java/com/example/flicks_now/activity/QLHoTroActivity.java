package com.example.flicks_now.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.flicks_now.R;

import com.example.flicks_now.databinding.ActivityQlHoTroBinding;
import com.example.flicks_now.model.HoTro;
import com.example.flicks_now.model.ThongBao;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QLHoTroActivity extends AppCompatActivity {
    private ActivityQlHoTroBinding binding;
    private String id_user;
    private String imageUrl;
    private String title;
    private String time;
    private String content;
    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityQlHoTroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
// Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thông Tin Hỗ Trợ"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        // Nhận dữ liệu từ Intent
        title = getIntent().getStringExtra("title");
        time = getIntent().getStringExtra("time");
        content = getIntent().getStringExtra("content");
        imageUrl = getIntent().getStringExtra("imageUrl");
        id_user = getIntent().getStringExtra("userId");


        // Hiển thị dữ liệu lên giao diện
        binding.tvName.setText(title);
        binding.tvTime.setText(time);
        binding.tvContent.setText(content);
        Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.ic_notification)
                .into(binding.ivHinhanh);

        binding.btnSaveChanges.setOnClickListener(v -> sendHoTro(id_user));
    }

    private void sendHoTro(String id_user) {
        // Kiểm tra nếu id_user null hoặc rỗng
        if (id_user == null || id_user.isEmpty()) {
            Toast.makeText(this, "Không thể gửi hỗ trợ: id_user bị null hoặc rỗng", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = binding.inputTicketCode.getText().toString();
        String description = binding.inputDescription.getText().toString();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Kiểm tra dữ liệu nhập
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo thông báo hỗ trợ cho người dùng
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("HoTro");
        String notificationId = databaseReference.push().getKey();

        // Lưu thông tin hỗ trợ
        HoTro hoTro = new HoTro(title,description,id_user,time);

        // Tạo thông báo cho người dùng với id_user
        DatabaseReference userNotifRef = FirebaseDatabase.getInstance().getReference("ThongBao");
        String userNotifId = userNotifRef.push().getKey();

        // Lưu dữ liệu hỗ trợ và thông báo vào Firebase
        databaseReference.child(notificationId).setValue(hoTro)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Lưu thông báo hỗ trợ vào màn hình thông báo của người dùng
                        ThongBao thongBao = new ThongBao(userNotifId,id_user,title,time,description); // Thêm thời gian để sắp xếp
                        userNotifRef.child(userNotifId).setValue(thongBao)
                                .addOnCompleteListener(notifTask -> {
                                    if (notifTask.isSuccessful()) {
                                        Toast.makeText(this, "Hỗ trợ đã được gửi và thông báo lưu thành công", Toast.LENGTH_SHORT).show();
                                        finish();  // Quay lại màn hình trước
                                    } else {
                                        Toast.makeText(this, "Gửi thông báo thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Gửi hỗ trợ thất bại", Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Giữ màn hình sáng khi hoạt động
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Tắt giữ màn hình sáng khi dừng hoạt động
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
