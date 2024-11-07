package com.example.flicks_now.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityDangKyBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class DangKyActivity extends AppCompatActivity {

    private ActivityDangKyBinding binding;
    // Firebase Database and Auth
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;// Flag for mật khẩu 2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDangKyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Thiết lập sự kiện xem mật khẩu
        setupPasswordToggle(binding.edtMk);
        setupPasswordToggle(binding.edtNLMK);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        
        xulyDangKy();

        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void setupPasswordToggle(EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            boolean isPasswordVisible = false; // Trạng thái mặc định là ẩn mật khẩu

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_END = 2; // DrawableEnd (icon con mắt)
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                        if (isPasswordVisible) {
                            // Ẩn mật khẩu
                            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                        } else {
                            // Hiển thị mật khẩu
                            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                        }
                        isPasswordVisible = !isPasswordVisible; // Đổi trạng thái
                        editText.setSelection(editText.getText().length()); // Đặt lại vị trí con trỏ
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void xulyDangKy() {
        // Xử lý sự kiện đăng ký
        binding.btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hoTen = binding.edtHT.getText().toString().trim();
                String email = binding.edtEmail.getText().toString().trim();
                String matKhau = binding.edtMk.getText().toString().trim();
                String nhapLaiMatKhau = binding.edtNLMK.getText().toString().trim();

                if (TextUtils.isEmpty(hoTen) || TextUtils.isEmpty(email) || TextUtils.isEmpty(matKhau) || TextUtils.isEmpty(nhapLaiMatKhau)) {
                    Toast.makeText(DangKyActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!matKhau.equals(nhapLaiMatKhau)) {
                    Toast.makeText(DangKyActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                }else if (!isValidGmail(email)) {
                    Toast.makeText(DangKyActivity.this, "Email phải theo định dạng ...@gmail.com", Toast.LENGTH_SHORT).show();
                } else {
                    kiemtraEmailDaCoChua(email, hoTen, matKhau);
                }
            }
        });


    }

    private boolean isValidGmail(String email) {
        // Kiểm tra xem email có kết thúc bằng @gmail.com không
        return email.endsWith("@gmail.com");
    }
 //kiem tra email đã có trong firebase chưa
    private void kiemtraEmailDaCoChua(final String email, final String hoTen, final String matKhau) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(DangKyActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                } else {
                    xulyTaoMaKhackHang(hoTen, email, matKhau);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Hàm tạo mã khách hàng
    private void xulyTaoMaKhackHang(final String hoTen, final String email, final String matKhau) {
        String maKH = taoMaKH(hoTen);
        // đọc ma khach hang trong firebase
        usersRef.orderByChild("maKH").equalTo(maKH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu mã đã tồn tại, tạo lại mã khác
                    xulyTaoMaKhackHang(hoTen, email, matKhau);
                } else {
                    // Mã hợp lệ, tiến hành đăng ký
                    dangKyTaiKhoan(email, hoTen, matKhau, maKH);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi khi kiểm tra mã KH", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String taoMaKH(String hoTen) {
        String[] words = hoTen.split(" ");
        StringBuilder initials = new StringBuilder("FN");

        for (String word : words) {
            initials.append(word.charAt(0));  // Lấy chữ cái đầu
        }

        // Thêm 3 chữ số ngẫu nhiên
        Random random = new Random();
        String randomNumbers = String.format("%03d", random.nextInt(1000));
        initials.append(randomNumbers);

        // Xáo trộn các ký tự sau FN
        List<Character> charList = new ArrayList<>();
        for (int i = 2; i < initials.length(); i++) {
            charList.add(initials.charAt(i));
        }
        Collections.shuffle(charList);

        StringBuilder shuffledMaKH = new StringBuilder("FN");
        for (char c : charList) {
            shuffledMaKH.append(c);
        }

        return shuffledMaKH.toString();
    }

    private void dangKyTaiKhoan(final String email, final String hoTen, String matKhau, final String maKH) {
        //Gọi phương thức từ Firebase Authentication để tạo tài khoản mới với email và mật khẩu đã cung cấp
        mAuth.createUserWithEmailAndPassword(email, matKhau).addOnCompleteListener(task -> {
            //Kiểm tra xem thao tác đăng ký có thành công hay không
            if (task.isSuccessful()) {
                //Lấy thông tin của người dùng hiện tại (người vừa đăng ký)
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Tạo một Map để lưu thông tin người dùng
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id_user", maKH); // Mã KH
                    userMap.put("name", hoTen); // Tên người dùng
                    userMap.put("email", email); // Địa chỉ email
                    userMap.put("password", matKhau); // Mật khẩu (không nên lưu mật khẩu trong cơ sở dữ liệu)
                    userMap.put("created_at", ServerValue.TIMESTAMP); // Thời gian tạo
                    userMap.put("updated_at", ServerValue.TIMESTAMP); // Thời gian cập nhật
                    userMap.put("id_loaiND", 0); // ID loại người dùng mặc định là 0

                    // Lưu thông tin người dùng vào Firebase
                    usersRef.child(user.getUid()).setValue(userMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(DangKyActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi khi lưu thông tin", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                String errorMessage;
                try {
                    throw task.getException();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                }
                Toast.makeText(DangKyActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
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