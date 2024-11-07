package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityDangNhapBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DangNhapActivity extends AppCompatActivity {

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private ActivityDangNhapBinding binding;
    private boolean isPasswordVisible = false;  // Trạng thái của mật khẩu
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDangNhapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MainActivity.truycap = false;
        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Đảm bảo rằng bạn đã khởi tạo đúng đường dẫn

        xulyDangNhap();
        taoTaiKhoan();
        xemMatKhau();


    }

    private void taoTaiKhoan() {

        binding.tvTaoTaiKhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangNhapActivity.this, DangKyActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void xemMatKhau() {
        // Thiết lập sự kiện nhấn vào biểu tượng con mắt
        binding.edtMk.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;  // Vị trí của drawableEnd (con mắt) là vị trí thứ 2 (bên phải)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.edtMk.getRight() - binding.edtMk.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Kiểm tra trạng thái hiện tại của mật khẩu
                    if (isPasswordVisible) {
                        // Nếu mật khẩu đang hiển thị, chuyển sang ẩn
                        binding.edtMk.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.edtMk.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Nếu mật khẩu đang ẩn, chuyển sang hiển thị
                        binding.edtMk.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.edtMk.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                    }
                    // Thay đổi trạng thái
                    isPasswordVisible = !isPasswordVisible;
                    binding.edtMk.setSelection(binding.edtMk.getText().length()); // Để con trỏ vẫn ở cuối EditText
                    return true;
                }
            }
            return false;
        });
    }
    private void xulyDangNhap() {
        // Xử lý sự kiện khi bấm đăng nhập
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.edtEmail.getText().toString().trim();
                String matKhau = binding.edtMk.getText().toString().trim();

                // Kiểm tra rỗng
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(matKhau)) {
                    Toast.makeText(DangNhapActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    // Thực hiện đăng nhập
                    loginUser(email, matKhau);
                }
            }
        });
    }

    private void loginUser(String email, String matKhau) {
        mAuth.signInWithEmailAndPassword(email, matKhau).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Lấy thông tin người dùng từ Firebase
                    usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Lấy thông tin người dùng từ snapshot
                                String hoTen = dataSnapshot.child("name").getValue(String.class);
                                String idUser = dataSnapshot.child("id_user").getValue(String.class);
                                String email = dataSnapshot.child("email").getValue(String.class);
                                Integer idLoaiND = dataSnapshot.child("id_loaiND").getValue(Integer.class);


                                    // Lưu thông tin vào SharedPreferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("id_user", idUser);
                                    editor.putString("name", hoTen);
                                    editor.putString("email", email);
                                    editor.putInt("id_loaiND", idLoaiND);
                                    editor.apply();



                                // Chuyển đến màn hình chính
                                Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(DangNhapActivity.this, "Thông tin người dùng không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(DangNhapActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Xử lý lỗi đăng nhập
                String errorMessage = "Đăng nhập thất bại"; // Thông báo lỗi mặc định
                if (task.getException() != null) {
                    errorMessage = task.getException().getMessage(); // Lấy thông điệp lỗi từ exception
                    Log.e("DangNhapActivity", "Đăng nhập thất bại: " + errorMessage); // Ghi log lỗi
                }
                Toast.makeText(DangNhapActivity.this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
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