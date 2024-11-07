package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityVipBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VipActivity extends AppCompatActivity {
    private ActivityVipBinding binding;
    private String idUser;
    private String nameUser;
    private String emailUser;
    private int idLoaiND;
    private DatabaseReference yeuCauRef;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        laythongtinUser();
        Toast.makeText(VipActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();
        // Kết nối tới Firebase
        yeuCauRef = FirebaseDatabase.getInstance().getReference("YeuCau");
        // Đặt item mặc định được chọn là màn hình Home
        binding.bottomNavigation.setSelectedItemId(R.id.nav_vip);

        // Kiểm tra xem idUser có trong bảng YeuCau hay chưa
        kiemTraYeuCau(idUser);
        kiemTraLoaiNguoiDung();
        // Xử lý sự kiện chọn item của Bottom Navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(VipActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.nav_vip) {
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(VipActivity.this, CaNhanActivity.class);
                } else if (item.getItemId() == R.id.nav_download) {
                    intent = new Intent(VipActivity.this, TaiPhimActivity.class);
                }
                if (intent != null) {
                    intent.putExtra("selected_item_id", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;

            }
        });

        binding.btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VipActivity.this, ThanhToanActivity.class);
                startActivity(intent);
                finish();
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

    private void kiemTraLoaiNguoiDung() {
        FirebaseDatabase.getInstance().getReference("Users").orderByChild("id_user").equalTo(idUser)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        if (userSnapshot.exists()) {
                            for (DataSnapshot user : userSnapshot.getChildren()) {
                                Integer idLoaiND = user.child("id_loaiND").getValue(Integer.class);
                                // Thêm log kiểm tra giá trị của id_loaiND
                                if (idLoaiND != null) {
                                    Log.d("id loại người dùng", "idLoaiND: " + idLoaiND);
                                } else {
                                    Log.d("id loại người dùng", "idLoaiND is null for user " + idUser);
                                }
                                // Kiểm tra trạng thái của yêuq cầu
                                if (idLoaiND != null && idLoaiND == 1) {
                                    // Nếu idLoaiND là 1, đổi màu và text của các nút
                                    binding.btnDangKy.setText("Đang sử dụng");
                                    binding.btnDangKy.setEnabled(false); // Vô hiệu hóa nút
                                    binding.btnDangKy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FB21"))); // Xanh

                                    binding.btnFree.setText("Sử dụng");
                                    binding.btnFree.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff66b2"))); // màu hồng
                                } else {
                                    binding.btnDangKy.setText("Đăng ký ngay");
                                    binding.btnDangKy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff66b2"))); // Hồng ban đầu
                                    binding.btnDangKy.setEnabled(true);
                                    binding.btnFree.setText("Đang sử dụng");
                                    binding.btnFree.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FB21"))); // Xanh lá

                                }
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // Kiểm tra nếu idUser đã có trong bảng YeuCau
    private void kiemTraYeuCau(String idUser) {
        yeuCauRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot yeuCauSnapshot : dataSnapshot.getChildren()) {
                    String idUserInDB = yeuCauSnapshot.child("idUser").getValue(String.class);
                    Integer idTrangThai = yeuCauSnapshot.child("idTrangThai").getValue(Integer.class);

                    // Kiểm tra nếu idUser trùng khớp
                    if (idUserInDB != null && idUserInDB.equals(idUser)) {
                        found = true;
                        // Thêm ghi log để kiểm tra idUser được lấy đúng cách
                        Log.d("KiemTraYeuCau", "Found idUser: " + idUserInDB);
                        FirebaseDatabase.getInstance().getReference("Users").orderByChild("id_user").equalTo(idUser)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        if (userSnapshot.exists()) {
                                            for (DataSnapshot user : userSnapshot.getChildren()) {
                                                Integer idLoaiND = user.child("id_loaiND").getValue(Integer.class);
                                                // Thêm log kiểm tra giá trị của id_loaiND
                                                if (idLoaiND != null) {
                                                    Log.d("id loại người dùng", "idLoaiND: " + idLoaiND);
                                                } else {
                                                    Log.d("id loại người dùng", "idLoaiND is null for user " + idUser);
                                                }
                                                // Kiểm tra trạng thái của yêuq cầu
                                                if (idLoaiND != null && idLoaiND == 1) {
                                                    // Nếu idTrangThai là 1, đổi màu và text của các nút
                                                    binding.btnDangKy.setText("Đang sử dụng");
                                                    binding.btnDangKy.setEnabled(false); // Vô hiệu hóa nút
                                                    binding.btnDangKy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FB21"))); // Xanh

                                                    binding.btnFree.setText("Sử dụng");
                                                    binding.btnFree.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff66b2"))); // Hồng
                                                } else {
                                                    if (idTrangThai != null && idTrangThai == 0) {
                                                        // Nếu idTrangThai không phải là 1 nhưng idUser đã có trong bảng YeuCau
                                                        binding.btnDangKy.setText("Đang xử lý");
                                                        binding.btnDangKy.setEnabled(false); // Vô hiệu hóa nút
                                                        binding.btnDangKy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff66b2"))); // Hồng ban đầu
                                                    }
                                                }
                                            }
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        break;
                    }
                }

                if (!found) {
                    // idUser chưa có, nút sẽ hoạt động bình thường
                    binding.btnDangKy.setEnabled(true); // Cho phép nhấn vào nút
                    binding.btnDangKy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff66b2"))); // Hồng ban đầu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi khi kết nối Firebase thất bại
                Toast.makeText(VipActivity.this, "Lỗi kết nối tới Firebase", Toast.LENGTH_SHORT).show();
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


}