package com.example.flicks_now.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityHoTroBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class HoTroActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1; // Mã yêu cầu chọn ảnh
    private ActivityHoTroBinding binding;  // View Binding
    private Uri imageUri;  // Để lưu URI của ảnh đã chọn
    private String idUser;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHoTroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Lắng nghe sự kiện khi người dùng nhấn vào imgUpload để chọn ảnh
        binding.imgUpload.setOnClickListener(view -> openGallery());

        // Lắng nghe sự kiện khi người dùng nhấn vào nút gửi yêu cầu
        binding.btnSubmit.setOnClickListener(view -> submitForm());
        laythongtinUser();
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);

    }

    // Mở thư viện ảnh để chọn
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Bộ lọc tất cả các định dạng ảnh
        startActivityForResult(Intent.createChooser(intent, "Chọn một ảnh"), PICK_IMAGE);
    }

    // Xử lý kết quả sau khi chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.imgUpload.setImageURI(imageUri);  // Hiển thị ảnh đã chọn
        }
    }

    // Gửi form hỗ trợ
    private void submitForm() {
        String name = binding.inputTicketCode.getText().toString().trim();
        String description = binding.inputDescription.getText().toString().trim();

        // Kiểm tra nếu thông tin không đầy đủ
        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        // Lấy ID của người dùng đã đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để gửi yêu cầu hỗ trợ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tải ảnh lên Firebase Storage trước, rồi sau đó lưu thông tin vào Firebase Realtime Database
        uploadImageAndSubmitRequest(name, description,idUser);
    }

    // Tải ảnh lên Firebase Storage và lưu thông tin vào Realtime Database
    private void uploadImageAndSubmitRequest(String name, String description, String userId) {
        if (imageUri == null) {
            Toast.makeText(this, "Không tìm thấy ảnh. Vui lòng chọn lại ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra file extension
        String fileExtension = getFileExtension(imageUri);
        if (fileExtension == null || fileExtension.isEmpty()) {
            Toast.makeText(this, "Phần mở rộng tệp không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tham chiếu tới Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("hinhAnhHoTro")
                .child(System.currentTimeMillis() + "." + fileExtension);

        // Thực hiện tải lên tệp
        storageRef.putFile(imageUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d("Upload Progress", "Tải lên được " + progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy URL của ảnh đã tải lên
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String imageUrl = downloadUri.toString();
                        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        // Lưu thông tin vào Firebase Realtime Database
                        saveToFirebaseRealtimeDatabase(name, description, imageUrl,userId,time);
                        Toast.makeText(this, "Tải ảnh lên thành công.", Toast.LENGTH_SHORT).show();
                        resetForm();
                    }).addOnFailureListener(e -> {
                        Log.e("Get URL Error", "Không thể lấy URL của hình ảnh: " + e.getMessage());
                        Toast.makeText(this, "Lỗi khi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Upload Error", "Không thể tải lên hình ảnh: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải lên ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức lấy phần mở rộng tệp từ Uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // Phương thức lưu thông tin vào Firebase Realtime Database
    private void saveToFirebaseRealtimeDatabase(String name, String description, String imageUrl, String userId, String time) {
        // Tạo đối tượng để lưu
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("description", description);
        data.put("imageUrl", imageUrl);
        data.put("time", time);
        data.put("userId", userId);  // Lưu ID của người dùng đã đăng nhập

        // Tham chiếu đến Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("HoTro");

        // Lưu dữ liệu vào Firebase
        databaseRef.push().setValue(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu yêu cầu thành công.", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Thông báo");
                    builder.setMessage("Chúng tôi đã ghi nhận yêu cầu hỗ trợ của bạn. Chúng tôi sẽ hỗ trợ bạn sớm nhất có thể.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // Đóng dialog
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show(); // Hiển thị dialog
                })
                .addOnFailureListener(e -> {
                    Log.e("Database Error", "Không thể lưu yêu cầu: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi lưu yêu cầu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    // Xóa form sau khi gửi thành công
    private void resetForm() {
        binding.inputTicketCode.setText("");
        binding.inputDescription.setText("");
        binding.imgUpload.setImageResource(R.drawable.baseline_upload_file_24);  // Đặt lại ảnh mặc định
        imageUri = null;
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
}
