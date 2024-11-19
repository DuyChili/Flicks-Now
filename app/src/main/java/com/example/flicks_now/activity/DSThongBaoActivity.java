package com.example.flicks_now.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.adapter.ThongBaoAdapter;
import com.example.flicks_now.databinding.ActivityDsthongBaoBinding;

import com.example.flicks_now.model.ThongBao;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DSThongBaoActivity extends AppCompatActivity implements ThongBaoAdapter.OnRecyclerViewItemClickListener  {
    private ActivityDsthongBaoBinding binding;
    private ThongBaoAdapter thongBaoAdapter;
    private List<ThongBao> thongBaoList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDsthongBaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        // Khởi tạo Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding.recyclerViewApis.setLayoutManager(new LinearLayoutManager(this));
        thongBaoAdapter = new ThongBaoAdapter(DSThongBaoActivity.this,thongBaoList);
        binding.recyclerViewApis.setAdapter(thongBaoAdapter);
        getThongBaoFromDatabase();
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý thông báo"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        binding.ivThemThongBao.setOnClickListener(v -> {
            Intent intent = new Intent(DSThongBaoActivity.this, QLThongBaoActivity.class);
            startActivity(intent);
        });
    }
    private void getThongBaoFromDatabase() {
        DatabaseReference thongBaoRef = FirebaseDatabase.getInstance().getReference().child("ThongBao");

        thongBaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                thongBaoList.clear(); // Xóa danh sách trước khi thêm mới
                List<String> seenIds = new ArrayList<>(); // Danh sách để lưu ID đã thấy

                for (DataSnapshot thongBaoSnapshot : snapshot.getChildren()) {
                    ThongBao thongBao = thongBaoSnapshot.getValue(ThongBao.class);
                    String idThongBao = thongBaoSnapshot.child("idThongBao").getValue(String.class);
                    if (thongBao != null) {
                        // Kiểm tra xem ID đã thấy hay chưa
                        if (!seenIds.contains(idThongBao)) {
                            thongBaoList.add(thongBao); // Thêm vào danh sách nếu ID chưa thấy
                            seenIds.add(idThongBao); // Đánh dấu ID là đã thấy
                        }
                    }
                }
                // Cập nhật RecyclerView với danh sách thông báo mới
                thongBaoAdapter.notifyDataSetChanged(); // Cập nhật adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DSThongBaoActivity.this, "Lỗi khi lấy thông báo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Hiển thị dialog thông báo
    public void hienDialogThongBao(ThongBao thongBao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(thongBao.getTitle());
        builder.setMessage(thongBao.getContent());

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    public void onItemClick(View view, ThongBao thongBao) {
        hienDialogThongBao(thongBao);
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