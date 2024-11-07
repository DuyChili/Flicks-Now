package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.databinding.ActivityCaiDatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CaiDatActivity extends AppCompatActivity {
    private ActivityCaiDatBinding binding;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaiDatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        setEvent();

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
        binding.ivdangxuat.setOnClickListener(new View.OnClickListener() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            @Override
            public void onClick(View view) {
                // Lấy ID người dùng
                String userId = user.getUid();
                DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("status");
                MainActivity.truycap = false;
                // Đặt trạng thái là "offline"
                userStatusRef.setValue("offline");
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // Xóa tất cả thông tin
                editor.apply();
                Intent intent = new Intent(CaiDatActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(CaiDatActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

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