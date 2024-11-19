package com.example.flicks_now.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityChinhSuaThongTinBinding;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChinhSuaThongTinActivity extends AppCompatActivity {
    private ActivityChinhSuaThongTinBinding binding;
    private DatabaseReference databaseReference; // Tham chiếu đến Realtime Database
    private String userId;
    private boolean isPasswordVisible = false;  // Trạng thái của mật khẩu
    private boolean doubleBackToExitPressedOnce = false;  // Trạng thái của mật khẩu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChinhSuaThongTinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Khởi tạo Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Lấy người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid(); // Lấy ID người dùng từ Firebase Authentication
        } else {
            finish(); // Đóng Activity nếu người dùng chưa đăng nhập
            return;
        }

        // Thiết lập sự kiện cho nút Lưu
        binding.btnLuu.setOnClickListener(view -> thayDoiMatKhau());
        xemMatKhauCu();
        xemMatKhauMoi();
        xemMatKhauMoiNhapLai();
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa thông tin"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
    }

    private void thayDoiMatKhau() {
        String mkCu = binding.edtMkCu.getText().toString().trim(); // Mật khẩu hiện tại
        String mkMoi = binding.edtMkMoi.getText().toString().trim(); // Mật khẩu mới
        String mkMoi2 = binding.edtMkMoi2.getText().toString().trim(); //Mật khẩu mới nhập lại
        // Kiểm tra nếu các trường rỗng
        if (mkCu.isEmpty() || mkMoi.isEmpty() || mkMoi2.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return; // Dừng nếu có trường rỗng
        }

        // Kiểm tra nếu userId không null
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy ID người dùng, không thể cập nhật thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        xacThucMatKhau(mkCu, mkMoi, mkMoi2);
    }

    private void xacThucMatKhau(String mkCu, String mkMoi, String mkMoi2) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Kiểm tra xem mật khẩu mới và mật khẩu nhập lại có trùng nhau không
        if (!mkMoi.equals(mkMoi2)) {
            Toast.makeText(this, "Mật khẩu mới và mật khẩu nhập lại không trùng khớp.", Toast.LENGTH_SHORT).show();
            return; // Dừng hàm nếu không trùng
        }
        if (user != null) {
            String email = user.getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(email, mkCu);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Xác thực thành công.", Toast.LENGTH_SHORT).show();
                    // Xóa nội dung các trường nhập mật khẩu
                    binding.edtMkCu.setText("");
                    binding.edtMkMoi.setText("");
                    binding.edtMkMoi2.setText("");
                    user.updatePassword(mkMoi).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            // Hiển thị Dialog thông báo thành công
                            new AlertDialog.Builder(this)
                                    .setTitle("Thông báo")
                                    .setMessage("Đổi mật khẩu thành công.")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        // Xóa nội dung các trường nhập mật khẩu
                                        binding.edtMkCu.setText("");
                                        binding.edtMkMoi.setText("");
                                    })
                                    .show();

                            // Lưu mật khẩu mới vào cơ sở dữ liệu (tùy chọn, không khuyến nghị lưu mật khẩu)
                            databaseReference.child(userId).child("password").setValue(mkMoi)
                                    .addOnCompleteListener(passwordUpdateTask -> {
                                        if (passwordUpdateTask.isSuccessful()) {
                                            Toast.makeText(this, "Lưu mật khẩu mới vào cơ sở dữ liệu thành công.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Lỗi khi lưu mật khẩu mới vào cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Lỗi khi đổi mật khẩu.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ChinhSuaThongTinActivity.this, "Mật khẩu cũ không trùng khớp.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w("UpdateUserActivity", "Người dùng chưa đăng nhập, không thể xác thực.");
        }
    }

    private void xemMatKhauCu() {
        // Thiết lập sự kiện nhấn vào biểu tượng con mắt
        binding.edtMkCu.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;  // Vị trí của drawableEnd (con mắt) là vị trí thứ 2 (bên phải)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.edtMkCu.getRight() - binding.edtMkCu.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Kiểm tra trạng thái hiện tại của mật khẩu
                    if (isPasswordVisible) {
                        // Nếu mật khẩu đang hiển thị, chuyển sang ẩn
                        binding.edtMkCu.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.edtMkCu.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Nếu mật khẩu đang ẩn, chuyển sang hiển thị
                        binding.edtMkCu.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.edtMkCu.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                    }
                    // Thay đổi trạng thái
                    isPasswordVisible = !isPasswordVisible;
                    binding.edtMkCu.setSelection(binding.edtMkCu.getText().length()); // Để con trỏ vẫn ở cuối EditText
                    return true;
                }
            }
            return false;
        });
    }
    private void xemMatKhauMoi() {
        // Thiết lập sự kiện nhấn vào biểu tượng con mắt
        binding.edtMkMoi.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;  // Vị trí của drawableEnd (con mắt) là vị trí thứ 2 (bên phải)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.edtMkMoi.getRight() - binding.edtMkMoi.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Kiểm tra trạng thái hiện tại của mật khẩu
                    if (isPasswordVisible) {
                        // Nếu mật khẩu đang hiển thị, chuyển sang ẩn
                        binding.edtMkMoi.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.edtMkMoi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Nếu mật khẩu đang ẩn, chuyển sang hiển thị
                        binding.edtMkMoi.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.edtMkMoi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                    }
                    // Thay đổi trạng thái
                    isPasswordVisible = !isPasswordVisible;
                    binding.edtMkMoi.setSelection(binding.edtMkMoi.getText().length()); // Để con trỏ vẫn ở cuối EditText
                    return true;
                }
            }
            return false;
        });
    }
    private void xemMatKhauMoiNhapLai() {
        // Thiết lập sự kiện nhấn vào biểu tượng con mắt
        binding.edtMkMoi2.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;  // Vị trí của drawableEnd (con mắt) là vị trí thứ 2 (bên phải)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.edtMkMoi2.getRight() - binding.edtMkMoi2.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Kiểm tra trạng thái hiện tại của mật khẩu
                    if (isPasswordVisible) {
                        // Nếu mật khẩu đang hiển thị, chuyển sang ẩn
                        binding.edtMkMoi2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.edtMkMoi2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Nếu mật khẩu đang ẩn, chuyển sang hiển thị
                        binding.edtMkMoi2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.edtMkMoi2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                    }
                    // Thay đổi trạng thái
                    isPasswordVisible = !isPasswordVisible;
                    binding.edtMkMoi2.setSelection(binding.edtMkMoi2.getText().length()); // Để con trỏ vẫn ở cuối EditText
                    return true;
                }
            }
            return false;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

