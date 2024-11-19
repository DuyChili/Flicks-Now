package com.example.flicks_now.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.adapter.LSThanhToanAdapter;
import com.example.flicks_now.databinding.DialogLichsuThanhtoanBinding;
import com.example.flicks_now.model.LichSuThanhToan;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.example.flicks_now.databinding.ActivityDoanhThuBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DoanhThuActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private ActivityDoanhThuBinding binding;

    private HashMap<Integer, Long> monthlyRevenue = new HashMap<>();;
    private long tongDTHomNay = 0;
    private long tongDT7Ngay = 0;
    private long tongDT1Thang = 0;
    private long tongGDHomNay = 0;

    private LSThanhToanAdapter adapter;
    private List<LichSuThanhToan> lichSuTTList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoanhThuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Khởi tạo Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("LichSuThanhToan");
        // Khởi tạo danh sách và adapter
        lichSuTTList = new ArrayList<>();
        adapter = new LSThanhToanAdapter(this, lichSuTTList);
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Doanh Thu"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        // Thiết lập RecyclerView
        binding.recyclerViewLichSu.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewLichSu.setAdapter(adapter);

        // Thiết lập sự kiện khi nhấn vào item trong RecyclerView
        adapter.setOnItemClickListener((view, position) -> {
            LichSuThanhToan thanhToan = lichSuTTList.get(position);
            // Tạo dialog hiển thị thông tin chi tiết thanh toán
            hienThiChiTietLSThanhToan(thanhToan);
            Toast.makeText(this, "Chi tiết thanh toán của: " + thanhToan.getIdUser(), Toast.LENGTH_SHORT).show();
            // Bạn có thể mở một Activity khác để hiển thị chi tiết thanh toán nếu cần
        });

        datLaiDoanhThuTheoThang();
        layDuLieuFirebase();
        // Thiết lập Spinner năm
        thietLapSpinner();

        // Lắng nghe thay đổi của Spinner để cập nhật dữ liệu
        caiDatSuKienChonNam();
        loadLichSuThanhToan();
    }
    private void loadLichSuThanhToan() {
        // Hiển thị ProgressBar trong khi tải dữ liệu
        binding.progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Xóa dữ liệu cũ trong danh sách
                lichSuTTList.clear();

                // Duyệt qua các bản ghi trong Firebase
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LichSuThanhToan thanhToan = snapshot.getValue(LichSuThanhToan.class);
                    if (thanhToan != null) {
                        lichSuTTList.add(thanhToan);
                    }
                }

                // Ẩn ProgressBar sau khi tải xong dữ liệu
                binding.progressBar.setVisibility(View.GONE);

                // Cập nhật adapter sau khi thêm dữ liệu mới
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(DoanhThuActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;  // Giải phóng View Binding khi Activity bị hủy
    }
    private void layDuLieuFirebase() {
        binding.progressBar.setVisibility(View.VISIBLE);
        datLaiDoanhThuTheoThang();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Xóa các giá trị cũ
                lichSuTTList.clear();
                tongDTHomNay = 0;
                tongDT7Ngay = 0;
                tongDT1Thang = 0;
                tongGDHomNay = 0;

                // Xử lý dữ liệu mới từ Firebase
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    LichSuThanhToan thanhToan = childSnapshot.getValue(LichSuThanhToan.class);
                    if (thanhToan != null) {
                        lichSuTTList.add(thanhToan);
                        String ngayXacNhan = thanhToan.getNgayXacNhan();
                        Long soTien = thanhToan.getSoTien();

                        if (ngayXacNhan != null && soTien != null) {
                            tinhDoanhThu(ngayXacNhan, soTien);
                            capNhatDoanhThuHangThang(ngayXacNhan, soTien);
                        }
                    }
                }

                // Cập nhật UI và biểu đồ
                capNhapDuLieu();
                hienThiBieuDo();

                // Ẩn ProgressBar khi hoàn tất
                binding.progressBar.setVisibility(View.GONE);

                // Thông báo adapter rằng dữ liệu đã thay đổi
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(DoanhThuActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                Log.e("DoanhThuActivity", "Lỗi khi lắng nghe dữ liệu: " + error.getMessage());
            }
        });
    }

    private void tinhDoanhThu(String ngayXacNhan, long soTien) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        try {
            Date transactionDate = sdf.parse(ngayXacNhan);
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - transactionDate.getTime();

            // Doanh thu hôm nay
            if (timeDifference <= TimeUnit.DAYS.toMillis(1)) {
                tongDTHomNay += soTien;
                tongGDHomNay++; // Tổng số giao dịch
            }

            // Doanh thu 7 ngày trước
            if (timeDifference <= TimeUnit.DAYS.toMillis(7)) {
                tongDT7Ngay += soTien;
            }

            // Doanh thu 1 tháng trước
            if (timeDifference <= TimeUnit.DAYS.toMillis(30)) {
                tongDT1Thang += soTien;
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void capNhapDuLieu() {
        // Cập nhật UI thông qua binding (các biến sẽ tự động hiển thị trên giao diện)
        binding.tvTongDoanhThu.setText(dinhDangTien(Double.valueOf(tongDTHomNay)));
        binding.tvDoanhThu7Ngay.setText(dinhDangTien(Double.valueOf(tongDT7Ngay)));
        binding.tvDoanhThu1Thang.setText(dinhDangTien(Double.valueOf(tongDT1Thang)));
        binding.tvSoGiaoDich.setText(dinhDangTien(Double.valueOf(tongGDHomNay)));
    }

    private void hienThiBieuDo() {
        ArrayList<Entry> entries = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            long revenue = monthlyRevenue.getOrDefault(month, 0L);
            Log.d("DoanhThuActivity", "Tháng " + month + ": " + revenue); // Kiểm tra dữ liệu
            entries.add(new Entry(month, revenue));
        }

        // Tạo LineDataSet và thiết lập cho biểu đồ
        LineDataSet lineDataSet = new LineDataSet(entries, "Doanh Thu Hàng Tháng");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setCircleRadius(4f);// Kích thước của điểm tròn
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(lineDataSet);
        binding.lineChart.setData(lineData);

        // // Thiết lập cho trục X (hiển thị từ tháng 1 đến tháng 12)
        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setGranularity(1f);// Khoảng cách giữ các nhãn trục X
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(12);// Đặt số lượng nhãn trục X là 12
        xAxis.setAxisMinimum(1f);
        xAxis.setAxisMaximum(12f);

        // Thiết lập cho trục Y(dùng để hiển thị doanh thu)
        YAxis leftAxis = binding.lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        binding.lineChart.getAxisRight().setEnabled(false);
        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.invalidate(); // Refresh biểu đồ
    }
    private void capNhatDoanhThuHangThang(String ngayXacNhan, long soTien) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        try {
            Date transactionDate = sdf.parse(ngayXacNhan);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
            int month = Integer.parseInt(monthFormat.format(transactionDate));


            // Kiểm tra và cộng dồn doanh thu cho tháng tương ứng
            long currentRevenue = monthlyRevenue.getOrDefault(month, 0L);
            // Cộng dồn doanh thu cho tháng tương ứng
            monthlyRevenue.put(month, currentRevenue + soTien);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void hienThiChiTietLSThanhToan(LichSuThanhToan thanhToan){
        DialogLichsuThanhtoanBinding dialogBinding = DialogLichsuThanhtoanBinding.inflate(LayoutInflater.from(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        // Hiển thị thông tin chi tiết lịch sử thanh toán
        hienThiNameLenDiaLog(dialogBinding,thanhToan);
        dialogBinding.tvIdUserDialog.setText(thanhToan.getIdUser());
        dialogBinding.tvContentDialog.setText(thanhToan.getNoiDung());
        dialogBinding.tvAmountDialog.setText(String.valueOf(thanhToan.getSoTien()));
        dialogBinding.tvPaymentDateDialog.setText(thanhToan.getNgayThanhToan());
        dialogBinding.tvNgayXacNhan.setText(thanhToan.getNgayXacNhan());
        dialogBinding.tvNgayHetHan.setText(thanhToan.getNgayHetHan());


        // Xử lý các sự kiện liên quan đến thanh toán
        dialogBinding.btnDong.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void hienThiNameLenDiaLog(DialogLichsuThanhtoanBinding dialogBinding,LichSuThanhToan thanhToan){
        // Truy vấn người dùng từ Firebase dựa trên id_user tự tạo
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.orderByChild("id_user").equalTo(thanhToan.getIdUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy tên của người dùng đầu tiên tìm thấy
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userName = userSnapshot.child("name").getValue(String.class);
                        dialogBinding.tvUserNameDialog.setText(userName);
                        break; // Thoát khỏi vòng lặp sau khi tìm thấy người dùng đầu tiên
                    }
                } else {
                    dialogBinding.tvUserNameDialog.setText("Tên không xác định");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBinding.tvUserNameDialog.setText("Lỗi kết nối");
            }
        });
    }

    private String dinhDangTien(Double doanhThu){
        if (doanhThu == null) return "0";
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedDoanhThu = decimalFormat.format(doanhThu);
        return formattedDoanhThu;
    }
    // Thiết lập danh sách năm cho Spinner và chọn năm hiện tại theo mặc định
    private void thietLapSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Integer> yearsList = new ArrayList<>();
        for (int i = 2015; i <= currentYear; i++) {
            yearsList.add(i);
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerYear.setAdapter(adapter);
        binding.spinnerYear.setSelection(yearsList.indexOf(currentYear));

        // Tải dữ liệu ban đầu cho năm hiện tại
        //taiDuLieuChoNamDuocChon(currentYear);
    }

    // Hàm lắng nghe sự kiện khi chọn năm trên Spinner
    private void caiDatSuKienChonNam() {
        binding.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedYear = (int) parent.getItemAtPosition(position);
                taiDuLieuChoNamDuocChon(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Tuỳ chọn: xử lý trường hợp khi không có gì được chọn nếu cần
            }
        });
    }
    // Hàm tải dữ liệu doanh thu theo năm
    private void taiDuLieuChoNamDuocChon(int year) {
        datLaiDoanhThuTheoThang();  // Đặt lại dữ liệu doanh thu hàng tháng

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String ngayXacNhan = childSnapshot.child("ngayXacNhan").getValue(String.class);
                    Long soTien = childSnapshot.child("soTien").getValue(Long.class);

                    // Chỉ cập nhật doanh thu nếu ngày giao dịch khớp với năm được chọn
                    if (ngayXacNhan != null && soTien != null && ngayXacNhan.contains("/" + year)) {
                        capNhatDoanhThuHangThang(ngayXacNhan, soTien);
                    }
                }
                hienThiBieuDo();  // Làm mới biểu đồ sau khi tải dữ liệu
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("DoanhThuActivity", "Lỗi khi tải dữ liệu: " + error.getMessage());
            }
        });
    }
    // Hàm đặt lại doanh thu hàng tháng về 0
    private void datLaiDoanhThuTheoThang() {
        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.put(i, 0L);
        }
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

