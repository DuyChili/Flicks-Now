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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityDangNhapBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import timber.log.Timber;


public class DangNhapActivity extends AppCompatActivity {

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private ActivityDangNhapBinding binding;
    private boolean isPasswordVisible = false;  // Trạng thái của mật khẩu
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDangNhapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        MainActivity.truycap = false;
        // Kiểm tra xem người dùng đã đăng nhập chưa
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id_user", null);
        if (userId != null) {
            // Nếu người dùng đã đăng nhập, chuyển ngay đến MainActivity
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Xóa tất cả dữ liệu
            editor.apply();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Đảm bảo rằng bạn đã khởi tạo đúng đường dẫn
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
        xulyDangNhap();
        taoTaiKhoan();
        xemMatKhau();

        binding.forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangNhapActivity.this, QuenMatKhauActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    // Đối tượng signInLauncher sử dụng ActivityResultLauncher<Intent> để xử lý kết quả đăng nhập Google
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            // Đăng ký ActivityResultLauncher với StartActivityForResult contract
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Kiểm tra kết quả trả về: nếu thành công (RESULT_OK) và có dữ liệu (result.getData() không null)
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Tạo một tác vụ GoogleSignInAccount từ dữ liệu trả về để xử lý thông tin đăng nhập
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    // Gọi hàm handleSignInResult để xử lý kết quả đăng nhập Google
                    xuLyDangNhap(task);
                } else {
                    // Nếu đăng nhập thất bại, ghi log lỗi với thông tin chi tiết
                    Timber.e("Google Sign In Failed: %s", result.getData() != null ? result.getData().toString() : "No data");

                }
            });
    // Trong onCreate(), thay vì gọi startActivityForResult, bạn gọi signInLauncher.launch():
    private void xuLyDangNhap(Task<GoogleSignInAccount> completedTask) {
        try {
            // Lấy thông tin tài khoản Google từ kết quả đăng nhập
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Nếu lấy thành công tài khoản, tiếp tục đăng nhập vào Firebase với thông tin Google này
            dangNhapFirebaseBangGoogle(account);
        } catch (ApiException e) {
            // Nếu xảy ra lỗi trong quá trình đăng nhập Google, hiển thị thông báo lỗi cho người dùng
            Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void dangNhapFirebaseBangGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Lưu thông tin user vào database
                        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    // Tạo user mới trong database nếu chưa tồn tại
                                    Map<String, Object> userMap = new HashMap<>();
                                    // Tạo ID ngẫu nhiên theo format FNxxxxx
                                    String randomId = "FN" + new Random().nextInt(900) + 100;

                                    userMap.put("id_user", randomId);
                                    userMap.put("name", user.getDisplayName());
                                    userMap.put("email", user.getEmail());
                                    userMap.put("id_loaiND", 0); // Mặc định là user thường
                                    userMap.put("status", "offline");
                                    userMap.put("created_at", ServerValue.TIMESTAMP);
                                    userMap.put("updated_at", ServerValue.TIMESTAMP);
                                    // Không cần password vì đăng nhập qua Google

                                    usersRef.child(user.getUid()).setValue(userMap).addOnCompleteListener(databaseTask -> {
                                        if (databaseTask.isSuccessful()) {
                                            // Lưu thông tin vào SharedPreferences
                                            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("id_user", randomId);  // Lưu id_user tự tạo
                                            editor.putString("name", user.getDisplayName());
                                            editor.putString("email", user.getEmail());
                                            editor.putInt("id_loaiND", 0);
                                            editor.apply();

                                            // Chuyển đến MainActivity
                                            Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Log.e("DangNhapActivity", "Lỗi khi lưu user vào database", databaseTask.getException());
                                            Toast.makeText(DangNhapActivity.this, "Lỗi khi lưu thông tin người dùng", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    // User đã tồn tại, lấy thông tin và lưu vào SharedPreferences
                                    String idUser = dataSnapshot.child("id_user").getValue(String.class);
                                    String hoTen = dataSnapshot.child("name").getValue(String.class);
                                    String email = dataSnapshot.child("email").getValue(String.class);
                                    Integer idLoaiND = dataSnapshot.child("id_loaiND").getValue(Integer.class);

                                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("id_user", idUser);
                                    editor.putString("name", hoTen);
                                    editor.putString("email", email);
                                    editor.putInt("id_loaiND", idLoaiND != null ? idLoaiND : 0);
                                    editor.apply();

                                    // Chuyển đến MainActivity
                                    Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(DangNhapActivity.this, "Lỗi khi kiểm tra thông tin người dùng", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(DangNhapActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
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