package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityCaiDatBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CaiDatActivity extends AppCompatActivity {
    private ActivityCaiDatBinding binding;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private boolean doubleBackToExitPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaiDatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setEvent();
        checkAdminStatus();
        kiemTraDangNhap();
        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    public void setEvent(){
        laythongtinUser();
        binding.tvTendangnhap.setText(nameUser);
        binding.btnThoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CaiDatActivity.this, CaNhanActivity.class);
                startActivity(intent);
            }
        });
        binding.tvThaydoiMk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CaiDatActivity.this, ChinhSuaThongTinActivity.class);
                startActivity(intent);
            }
        });
        binding.troGiup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(CaiDatActivity.this, HoTroActivity.class);
                startActivity(a);
            }
        });
        binding.tvVeChungToi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CaiDatActivity.this, VeChungToiActivity.class );
                startActivity(intent);
            }
        });
        binding.ivdangxuat.setOnClickListener(view -> dangXuat());
        binding.admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CaiDatActivity.this, AdminActivity.class );
                startActivity(intent);
            }
        });
    }
    private void kiemTraDangNhap(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Người dùng đã đăng nhập
            binding.ivdangxuat.setText(R.string.logout);
            binding.ivdangxuat.setOnClickListener(view -> dangXuat());
        } else {
            // Người dùng chưa đăng nhập
            binding.ivdangxuat.setText(R.string.login);
            binding.ivdangxuat.setOnClickListener(view -> dangNhap());
        }
    }
    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

    }
    private void checkAdminStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Lấy id_loaiND của người dùng
            userRef.child("id_loaiND").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer idLoaiND = snapshot.getValue(Integer.class);

                    // Kiểm tra nếu người dùng là admin
                    if (idLoaiND != null && idLoaiND == 2 || idLoaiND == 3|| idLoaiND == 4) {
                        binding.cvDangXuat.setVisibility(View.VISIBLE); // Hiển thị nút admin
                    } else {
                        binding.cvDangXuat.setVisibility(View.GONE); // Ẩn nút admin
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu có
                }
            });
        } else {
            binding.cvDangXuat.setVisibility(View.GONE); // Ẩn nút admin khi người dùng không tồn tại
        }
    }
    private void dangXuat() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("status");

            // Đặt trạng thái là "offline" trong Firebase Database
            userStatusRef.setValue("offline").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Thực hiện đăng xuất khỏi Firebase Auth
                    FirebaseAuth.getInstance().signOut();
                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                    googleSignInClient.signOut();
                    // Xóa thông tin trong SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // Xóa tất cả dữ liệu trong SharedPreferences
                    editor.apply();

                    // Chuyển người dùng về MainActivity
                    Intent intent = new Intent(CaiDatActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Đóng CaiDatActivity

                    Toast.makeText(CaiDatActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CaiDatActivity.this, "Đăng xuất thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
    private void dangNhap() {
        // Chuyển đến màn hình đăng nhập
        Intent intent = new Intent(CaiDatActivity.this, DangNhapActivity.class);
        startActivity(intent);
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

}