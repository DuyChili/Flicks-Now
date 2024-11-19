package com.example.flicks_now.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.example.flicks_now.adapter.YeuCauAdapter;
import com.example.flicks_now.databinding.ActivityQlyeuCauBinding;
import com.example.flicks_now.databinding.DialogYeuCauDetailBinding;
import com.example.flicks_now.model.LichSuThanhToan;
import com.example.flicks_now.model.YeuCau;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QLYeuCauActivity extends AppCompatActivity {
    private ActivityQlyeuCauBinding binding;
    private YeuCauAdapter adapter;
    private List<YeuCau> yeuCauList;
    private List<YeuCau> LocDanhSach;
    private boolean doubleBackToExitPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityQlyeuCauBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        // Khởi tạo danh sách yêu cầu và adapter
        yeuCauList = new ArrayList<>();
        LocDanhSach = new ArrayList<>();
        adapter = new YeuCauAdapter(LocDanhSach);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        loadYeuCauData();
        // Set sự kiện click cho từng yêu cầu
        adapter.setOnItemClickListener(yeuCau -> chiTietYeuCau(yeuCau));

        // Chọn mặc định "Hôm Nay"
        binding.radioToday.setChecked(true);

        // Cấu hình spinner lọc
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.trang_thai, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(spinnerAdapter);

        // Xử lý sự kiện khi Spinner thay đổi
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Lấy giá trị được chọn trong Spinner và RadioButton hiện tại
                String statusFilter = parent.getItemAtPosition(position).toString();
                String dateFilter = ((RadioButton) findViewById(binding.radioGroupTimeFilter.getCheckedRadioButtonId())).getText().toString();

                // Gọi phương thức lọc khi Spinner thay đổi
                locTheoSpinnerVaRadio(dateFilter, statusFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        //  Xử lý sự kiện khi RadioButton thay đổi
        binding.radioGroupTimeFilter.setOnCheckedChangeListener((group, checkedId) -> {
            // Lấy giá trị của RadioButton và Spinner hiện tại
            String dateFilter = ((RadioButton) findViewById(checkedId)).getText().toString();
            String statusFilter = binding.spinnerStatus.getSelectedItem().toString();

            // Gọi phương thức lọc khi RadioButton thay đổi
            locTheoSpinnerVaRadio(dateFilter, statusFilter);
        });
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh Sách Yêu Cầu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
    }
    // Hàm kết hợp để áp dụng cả hai bộ lọc
    private void locTheoSpinnerVaRadio(String dateRange, String statusFilter) {
        LocDanhSach.clear();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();

        // Lọc theo dateRange
        List<YeuCau> dateFilteredList = new ArrayList<>();
        switch (dateRange) {
            case "Hôm nay":
                dateFilteredList = yeuCauList.stream()
                        .filter(yeuCau -> checkNgayHomNay(chuyenNgaySangDate(yeuCau.getPaymentDate()), today))
                        .collect(Collectors.toList());
                break;
            case "7 ngày":
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                Date weekAgo = calendar.getTime();
                dateFilteredList = yeuCauList.stream()
                        .filter(yeuCau -> checkThoiGian(chuyenNgaySangDate(yeuCau.getPaymentDate()), weekAgo, today))
                        .collect(Collectors.toList());
                break;
            case "1 tháng":
                calendar.add(Calendar.MONTH, -1);
                Date monthAgo = calendar.getTime();
                dateFilteredList = yeuCauList.stream()
                        .filter(yeuCau -> checkThoiGian(chuyenNgaySangDate(yeuCau.getPaymentDate()), monthAgo, today))
                        .collect(Collectors.toList());
                break;
            case "1 năm":
                calendar.add(Calendar.YEAR, -1);
                Date yearAgo = calendar.getTime();
                dateFilteredList = yeuCauList.stream()
                        .filter(yeuCau -> checkThoiGian(chuyenNgaySangDate(yeuCau.getPaymentDate()), yearAgo, today))
                        .collect(Collectors.toList());
                break;
            case "All":
                dateFilteredList.addAll(yeuCauList);
                break;
        }

        // Lọc theo statusFilter sau khi đã áp dụng dateFilteredList
        for (YeuCau yeuCau : dateFilteredList) {
            if ("Đã xử lý".equals(statusFilter) && yeuCau.getIdTrangThai() == 1) {
                LocDanhSach.add(yeuCau);
            } else if ("Chưa xử lý".equals(statusFilter) && yeuCau.getIdTrangThai() == 0) {
                LocDanhSach.add(yeuCau);
            } else if ("All".equals(statusFilter)) {
                LocDanhSach.add(yeuCau);
            }
        }

        adapter.notifyDataSetChanged();
    }
    // Hàm tải dữ liệu từ Firebase
    private void loadYeuCauData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("YeuCau");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                yeuCauList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    YeuCau yeuCau = dataSnapshot.getValue(YeuCau.class);
                    if (yeuCau != null) {
                        yeuCauList.add(yeuCau);
                        Log.d("DataCheck", "YeuCau size: " + yeuCauList.size());
                    }
                }
                // Áp dụng lọc ngay sau khi dữ liệu được tải
                String dateFilter = ((RadioButton) findViewById(binding.radioGroupTimeFilter.getCheckedRadioButtonId())).getText().toString();
                String statusFilter = binding.spinnerStatus.getSelectedItem().toString();
                locTheoSpinnerVaRadio(dateFilter, statusFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QLYeuCauActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chiTietYeuCau(YeuCau yeuCau) {
        DialogYeuCauDetailBinding dialogBinding = DialogYeuCauDetailBinding.inflate(LayoutInflater.from(this));

        // Lấy tham chiếu tới bảng Users
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Truy vấn thông tin người dùng từ bảng Users theo id_user trong yêu cầu
        userRef.orderByChild("id_user").equalTo(yeuCau.getIdUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Lấy thông tin người dùng
                        String userName = userSnapshot.child("name").getValue(String.class);
                        dialogBinding.tvUserNameDialog.setText(userName);  // Hiển thị tên người dùng
                    }
                } else {
                    dialogBinding.tvUserNameDialog.setText("Unknown User");  // Không tìm thấy người dùng
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBinding.tvUserNameDialog.setText("Error Loading User");
            }
        });

        dialogBinding.tvIdUserDialog.setText(yeuCau.getIdUser());
        dialogBinding.tvContentDialog.setText(yeuCau.getContent());
        dialogBinding.tvAmountDialog.setText(String.valueOf(yeuCau.getAmount()));
        dialogBinding.tvPaymentDateDialog.setText(yeuCau.getPaymentDate());

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        if(yeuCau.getIdTrangThai() == 1){
            // Nếu idTrangThai là 1, thay đổi text nút thành "Quay lại"
            dialogBinding.btnXacNhanVip.setText("Quay lại");

            // Khi nhấn vào nút, chỉ đóng hộp thoại
            dialogBinding.btnXacNhanVip.setOnClickListener(v -> {
                dialog.dismiss(); // Đóng hộp thoại
            });
        }else{
            // Nếu idTrangThai là 0, giữ nguyên chức năng xác nhận VIP
            dialogBinding.btnXacNhanVip.setText("Xác nhận lên VIP");
            // Xử lý sự kiện khi nhấn nút "Xác nhận lên VIP"
            dialogBinding.btnXacNhanVip.setOnClickListener(v -> {
                // Hiển thị hộp thoại xác nhận
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc chắn muốn xác nhận yêu cầu này lên VIP không?")
                        .setPositiveButton("Có", (dialogInterface, which) -> {
                            // Cập nhật idTrangThai của yêu cầu lên 1
                            DatabaseReference yeuCauRef = FirebaseDatabase.getInstance().getReference("YeuCau")
                                    .child(yeuCau.getIdYeuCau());

                            yeuCauRef.child("idTrangThai").setValue(1).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Lấy idUser từ yêu cầu và cập nhật bảng Users
                                    String idUser = yeuCau.getIdUser();
                                    CapNhapUserLenVip(idUser);  // Gọi hàm cập nhật người dùng lên VIP
                                    themLichSuDaThanhToan(yeuCau); // Gọi hàm thêm vào LichSuThanhToan
                                    dialog.dismiss();
                                }
                            });
                        })
                        .setNegativeButton("Không", (dialogInterface, which) -> dialogInterface.dismiss())
                        .show();
            });
        }
    }

    // Hàm cập nhật người dùng lên VIP
    private void CapNhapUserLenVip(String idUser) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Cập nhật id_loaiND thành 1 (VIP)
                        userSnapshot.getRef().child("id_loaiND").setValue(1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void themLichSuDaThanhToan(YeuCau yeuCau) {
        DatabaseReference lichSuThanhToanRef = FirebaseDatabase.getInstance().getReference("LichSuThanhToan");

        // Tạo id thanh toán duy nhất
        String idThanhToan = lichSuThanhToanRef.push().getKey();

        // Lấy ngày hiện tại và ngày hết hạn (30 ngày sau)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String ngayXacNhan = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        String ngayHetHan = sdf.format(calendar.getTime());

        // Tạo đối tượng lich su thanh toan
        LichSuThanhToan lsThanhToan = new LichSuThanhToan(
                idThanhToan,
                yeuCau.getIdUser(),
                yeuCau.getContent(),
                yeuCau.getPaymentDate(),
                ngayXacNhan,
                yeuCau.getAmount(),
                ngayHetHan
        );

        // Thêm giao dịch vào Firebase
        lichSuThanhToanRef.child(idThanhToan).setValue(lsThanhToan);
    }

    // Hàm kiểm tra xem có phải ngày hôm nay hay không
    private boolean checkNgayHomNay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // Hàm kiểm tra xem ngày có trong khoảng thời gian không
    private boolean checkThoiGian(Date date, Date start, Date end) {
        return date != null && !date.before(start) && !date.after(end);
    }

    // Hàm chuyển đổi chuỗi ngày sang đối tượng Date
    private Date chuyenNgaySangDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
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
            Intent intent = new Intent(this, QLUserActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}