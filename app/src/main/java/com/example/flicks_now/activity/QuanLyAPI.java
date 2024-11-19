package com.example.flicks_now.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.R;
import com.example.flicks_now.adapter.ApiAdapter;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.databinding.ActivityQuanLyApiBinding;
import com.example.flicks_now.databinding.DialogAddApiBinding;
import com.example.flicks_now.model.ApiModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuanLyAPI extends AppCompatActivity {

    private ActivityQuanLyApiBinding binding;
    private ApiAdapter apiAdapter;
    private List<ApiModel> danhSachApi;
    private DatabaseReference thamChieuDatabase;
    private ApiModel apiDaChon;
    private ImageView imageView;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuanLyApiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        imageView = findViewById(R.id.img_selectApi);
// Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý API"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        danhSachApi = new ArrayList<>();
        thamChieuDatabase = FirebaseDatabase.getInstance().getReference("api_sources");

        layDanhSachApiTuFirebase();
        hienThiLogoDaChon();

        apiAdapter = new ApiAdapter(danhSachApi, new ApiAdapter.OnApiActionListener() {
            @Override
            public void onEditClick(ApiModel api) {
                apiDaChon = api;
                capNhatApi(api);
            }

            @Override
            public void onDeleteClick(ApiModel api) {
                new AlertDialog.Builder(QuanLyAPI.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa API này không?")
                        .setPositiveButton("Có", (dialog, which) -> xoaApi(api))
                        .setNegativeButton("Không", null)
                        .show();
            }

            @Override
            public void onSelectClick(ApiModel api) {
                luuApiDaChon(api);
                ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
                    @Override
                    public void onBaseUrlFetched(String name, String url) {
                        Toast.makeText(QuanLyAPI.this, "URL đã được cập nhật: " + name, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(QuanLyAPI.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }, QuanLyAPI.this);
            }
        });

        binding.rvApiList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApiList.setAdapter(apiAdapter);

//        binding.exitIcon.setOnClickListener(v -> finish());
        binding.btnAddapi.setOnClickListener(v -> hienThiDialogThem(-1, null));
    }

    private void hienThiDialogThem(int position, String currentName) {
        // Tạo dialog và sử dụng View Binding cho dialog
        Dialog dialog = new Dialog(QuanLyAPI.this);
        DialogAddApiBinding dialogBinding = DialogAddApiBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialogBinding.edtName.setText(""); // Nếu là thêm, thì để trống
        dialogBinding.edtLink.setText(""); // Nếu là thêm, thì để trống

        // Set background bo tròn cho dialog
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        dialog.getWindow().setLayout(800, 600); // Tùy chỉnh kích thước

        // Set sự kiện khi nhấn nút "Thêm/Cập nhật" trong dialog
        dialogBinding.btnThem.setOnClickListener(v -> {
            String name = dialogBinding.edtName.getText().toString();
            String link = dialogBinding.edtLink.getText().toString();
            if (!name.isEmpty() && !link.isEmpty()) {
                // Nếu là thêm
                new androidx.appcompat.app.AlertDialog.Builder(QuanLyAPI.this)
                        .setTitle("Xác nhận thêm")
                        .setMessage("Bạn có muốn thêm thể loại này không?")
                        .setPositiveButton("Có", (dialog1, which) -> {
                            themApi(name, link);
                        })
                        .setNegativeButton("Không", null) // Không làm gì nếu nhấn "Không"
                        .show();
            }
            dialog.dismiss(); // Đóng dialog sau khi thêm hoặc cập nhật
        });

        // Hiển thị dialog
        dialog.show();
    }
    private void themApi(String name, String url) {
        // Kiểm tra đầu vào
        if (!name.isEmpty() && !url.isEmpty()) {
            // Tham chiếu đến api_list
            thamChieuDatabase.child("api_list").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Lấy ID lớn nhất hiện tại trong api_list
                    long maxId = 0;
                    for (DataSnapshot apiSnapshot : snapshot.getChildren()) {
                        long id = Long.parseLong(apiSnapshot.getKey()); // Lấy key (ID) của mỗi API
                        if (id > maxId) {
                            maxId = id; // Tìm ID lớn nhất
                        }
                    }

                    // Tạo ID mới là maxId + 1
                    long newId = maxId + 1;
                    String apiId = String.valueOf(newId); // Chuyển đổi ID thành chuỗi

                    // Tạo API mới
                    Map<String, Object> apiMoi = new HashMap<>();
                    apiMoi.put("id", apiId);
                    apiMoi.put("name", name);
                    apiMoi.put("url", url);

                    // Lưu API mới vào api_list với ID là số nguyên
                    thamChieuDatabase.child("api_list").child(apiId).setValue(apiMoi)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(QuanLyAPI.this, "API đã được thêm thành công!", Toast.LENGTH_SHORT).show();
                                layDanhSachApiTuFirebase(); // Cập nhật danh sách
                            })
                            .addOnFailureListener(e -> Toast.makeText(QuanLyAPI.this, "Lỗi khi thêm API: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(QuanLyAPI.this, "Lỗi khi lấy dữ liệu API: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(QuanLyAPI.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
        }
    }




    private void layDanhSachApiTuFirebase() {
        //tham chieu database
        thamChieuDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                danhSachApi.clear();
                //lay api tu firebase
                DataSnapshot apiListSnapshot = snapshot.child("api_list");
                for (DataSnapshot apiSnapshot : apiListSnapshot.getChildren()) {
                    String apiId = apiSnapshot.getKey();
                    String ten = apiSnapshot.child("name").getValue(String.class);
                    String url = apiSnapshot.child("url").getValue(String.class);
                    danhSachApi.add(new ApiModel(apiId,ten, url));
                }
                apiAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuanLyAPI.this, "Lỗi tải API: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hienThiLogoDaChon() {
        //tham chieu database
        thamChieuDatabase.child("selected_source").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //lay api tu firebase
                String tenDaChon = snapshot.child("name").getValue(String.class);
                capNhatLogo(tenDaChon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuanLyAPI.this, "Lỗi khi tải selected_source: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void capNhatLogo(String tenDaChon) {
        if (tenDaChon != null) {
            if (tenDaChon.equalsIgnoreCase("Kkphim")) {
                imageView.setImageResource(R.drawable.logo_kkphim);
            } else if (tenDaChon.equalsIgnoreCase("ophim")) {
                imageView.setImageResource(R.drawable.ic_logo_ophim);
            } else {
                imageView.setImageResource(R.drawable.logo);
            }
        } else {
            imageView.setImageResource(R.drawable.logo);
        }
    }

    private void capNhatApi(ApiModel api) {
        //lay ten va url tu api
        String tenMoi = api.getName();
        String urlMoi = api.getUrl();
        //kiem tra ten va url
        if (!tenMoi.isEmpty() && !urlMoi.isEmpty()) {
            //tham chieu database
            thamChieuDatabase.child("api_list").orderByChild("name").equalTo(api.getName()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot apiSnapshot : snapshot.getChildren()) {
                            //cap nhat ten va url
                            apiSnapshot.getRef().child("name").setValue(tenMoi);
                            apiSnapshot.getRef().child("url").setValue(urlMoi)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(QuanLyAPI.this, "API đã được cập nhật!", Toast.LENGTH_SHORT).show();
                                        layDanhSachApiTuFirebase();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(QuanLyAPI.this, "Lỗi khi cập nhật API: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(QuanLyAPI.this, "API không tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(QuanLyAPI.this, "Lỗi khi tìm kiếm API: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(QuanLyAPI.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
        }
    }

    private void xoaApi(ApiModel api) {
        //lay ten api can xoa
        String tenApiCanXoa = api.getName();
        DatabaseReference apiListRef = thamChieuDatabase.child("api_list");
        //lay ten api tu firebase
        apiListRef.orderByChild("name").equalTo(tenApiCanXoa).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot apiSnapshot : snapshot.getChildren()) {
                    //xoa api
                    apiSnapshot.getRef().removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(QuanLyAPI.this, "API đã được xóa!", Toast.LENGTH_SHORT).show();
                                layDanhSachApiTuFirebase();
                            })
                            .addOnFailureListener(e -> Toast.makeText(QuanLyAPI.this, "Lỗi khi xóa API: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                if (!snapshot.exists()) {
                    Toast.makeText(QuanLyAPI.this, "Không tìm thấy API với tên: " + tenApiCanXoa, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuanLyAPI.this, "Lỗi khi truy xuất dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void luuApiDaChon(ApiModel apiDaChon) {
        //tham chieu database
        thamChieuDatabase.child("selected_source").setValue(apiDaChon)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(QuanLyAPI.this, "Đã lưu API thành công!", Toast.LENGTH_SHORT).show();
                    //luu api da chon vao SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("selectedApiUrl", apiDaChon.getUrl());
                    editor.putString("selectedApiName", apiDaChon.getName());
                    editor.apply();
                })
                .addOnFailureListener(e -> Toast.makeText(QuanLyAPI.this, "Lỗi khi lưu API: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
