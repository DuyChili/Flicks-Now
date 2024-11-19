package com.example.flicks_now.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.databinding.ActivityThanhToanBinding;
import com.example.flicks_now.model.YeuCau;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ThanhToanActivity extends AppCompatActivity {
    private String idUser;
    private String nameUser;
    private String emailUser;
    private int idLoaiND;
    private DatabaseReference yeuCauRef;
    private ActivityThanhToanBinding binding;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThanhToanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        laythongtinUser();
        Toast.makeText(ThanhToanActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();

        // Khởi tạo Firebase database reference
        yeuCauRef = FirebaseDatabase.getInstance().getReference("YeuCau");
        // Tạo nội dung thanh toán
        TaoNoiDungThanhToan(idUser);
// Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thanh toán"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
        binding.btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hienThiThongBaoXacNhan(); // Hiển thị hộp thoại xác nhận
            }
        });

        // Xử lý sự kiện nhấn nút "Sao chép nội dung thanh toán"
        binding.btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyNoiDungThanhToan(); // Thực hiện sao chép nội dung thanh toán
            }
        });
    }

    private void laythongtinUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);
    }
    private void copyNoiDungThanhToan() {
        // Sao chép nội dung thanh toán vào clipboard
        String content = binding.tvNoiDungThanhToan.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("PaymentContent", content);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Đã sao chép nội dung thanh toán", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nội dung thanh toán trống", Toast.LENGTH_SHORT).show();
        }
    }
    private void TaoNoiDungThanhToan(String maKH) {
        // Tạo nội dung thanh toán ban đầu
        String initialContent = maKH + "_" + RanDomMa(5);

        // Kiểm tra nếu nội dung thanh toán đã tồn tại và tự động tạo mã mới
        kiemTraNoiDung(initialContent);
    }

    // Phương thức kiểm tra và tạo mã thanh toán mới nếu mã đã tồn tại
    private void kiemTraNoiDung(String content) {
        Query query = yeuCauRef.orderByChild("content").equalTo(content);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu tồn tại, tạo mã mới và kiểm tra lại
                    kiemTraNoiDung(idUser + "_" + RanDomMa(5));
                } else {
                    // Nếu không tồn tại, cập nhật mã thanh toán lên TextView
                    binding.tvNoiDungThanhToan.setText(content);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThanhToanActivity.this, "Lỗi khi kiểm tra nội dung thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String RanDomMa(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    private void luuThongTinYeuCau() {
        String content = binding.tvNoiDungThanhToan.getText().toString();
        // Định dạng ngày và giờ thanh toán theo dd-MM-yyyy HH:mm:ss
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date()); // Lấy ngày và giờ hiện tại và định dạng

        String idYeuCau = yeuCauRef.push().getKey(); // Tạo id lịch sử thanh toán mới
        YeuCau yeuCauInfo = new YeuCau(idYeuCau, idUser, content, 99000, formattedDate, 0); // Sử dụng ngày và giờ đã định dạng

        yeuCauRef.child(idYeuCau).setValue(yeuCauInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ThanhToanActivity.this, "Yêu cầu lên Vip thành công, chúng tôi sẽ phản hồi bạn trong 24h tới", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ThanhToanActivity.this, "Yêu cầu lên Vip thất bại, vui lòng liên hệ admin để được hỗ trợ", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void hienThiThongBaoXacNhan() {
        // Hiển thị hộp thoại xác nhận thanh toán
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn có chắc chắn đã thanh toán không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    luuThongTinYeuCau(); // Lưu thông tin thanh toán nếu người dùng xác nhận
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss(); // Đóng hộp thoại nếu người dùng không đồng ý
                })
                .show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, VipActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
