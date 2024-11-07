package com.example.flicks_now.activity;

import static java.lang.Long.parseLong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityAdminBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private DatabaseReference dataUser;
    private DatabaseReference dataTruyCap;
    private DatabaseReference dataThanhToan;
    private long startOfDay;
    private long endOfDay;
    private Calendar calendar = Calendar.getInstance();

    private Button selectedButton = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        dataUser = FirebaseDatabase.getInstance().getReference("Users");
        dataTruyCap = FirebaseDatabase.getInstance().getReference("TruyCap");
        dataThanhToan = FirebaseDatabase.getInstance().getReference("YeuCau");

        // Đặt lại khoảng thời gian cho hôm nay
        layThongTinDangKy();
        updateSelectedButton(binding.btnHMNay);
        layThongTInDoanhThu();
        layThongTinTruyCapHomNay(); // Gọi hàm lấy thông tin truy cập hôm nay

        xulyXemThongTin();
        xulybuttonMenu();

    }



    private void xulyXemThongTin() {
        // Lắng nghe sự kiện khi nhấn nút "Hôm nay"
        binding.btnHMNay.setOnClickListener(view -> {

            updateSelectedButton(binding.btnHMNay);
            layThongTinDangKy();
            layThongTInDoanhThu();
            layThongTinTruyCapHomNay();
            // Gọi hàm lấy thông tin truy cập hôm nay
        });
// Lắng nghe sự kiện khi nhấn nút "3 ngày qua"
        binding.btn3ngay.setOnClickListener(view -> {

            updateSelectedButton(binding.btn3ngay);
            layThongTinDKTrongKhoangThoiGian(3);
            laythongtinDoanhThuTrongKhoang(3);
            layThongTinTruyCapTrongKhoangThoiGian(3);
            // Gọi hàm lấy thông tin truy cập 7 ngày qua
        });
        // Lắng nghe sự kiện khi nhấn nút "7 ngày qua"
        binding.btn7NgY.setOnClickListener(view -> {

            updateSelectedButton(binding.btn7NgY);
            layThongTinDKTrongKhoangThoiGian(7);
            laythongtinDoanhThuTrongKhoang(7);
            layThongTinTruyCapTrongKhoangThoiGian(7);
            // Gọi hàm lấy thông tin truy cập 7 ngày qua
        });
        // Lắng nghe sự kiện khi nhấn nút "1 tháng qua"
        binding.btnThang.setOnClickListener(view -> {

            updateSelectedButton(binding.btnThang);
            layThongTinDKTrongKhoangThoiGian(30);
            laythongtinDoanhThuTrongKhoang(30);
            layThongTinTruyCapTrongKhoangThoiGian(30);
            // Cập nhật số lượng truy cập 1 tháng qua
        });
        // Lắng nghe sự kiện khi nhấn nút "1 năm qua"
        binding.btnnam.setOnClickListener(view -> {

            updateSelectedButton(binding.btnnam);
            layThongTinDKTrongKhoangThoiGian(365);
            laythongtinDoanhThuTrongKhoang(365);
            layThongTinTruyCapTrongKhoangThoiGian(365);
            // Cập nhật số lượng truy cập 1 tháng qua
        });
    }
    // Phương thức để cập nhật màu của các nút
    private void updateSelectedButton(Button newButton) {
        // Nếu nút đã được chọn khác nút hiện tại, đổi màu
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorDefault)); // Màu mặc định
            selectedButton.setTextColor(getResources().getColor(R.color.defaultTextColor)); // Màu chữ mặc định
        }

        // Cập nhật nút hiện tại và đổi màu
        selectedButton = newButton;
        selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorSelected)); // Màu đã chọn
        selectedButton.setTextColor(getResources().getColor(R.color.selectedTextColor)); // Màu chữ đã chọn
    }

    private void layThongTinDangKy() {
        LayThoigianNgayHomNay();

        dataUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int userTodayCount = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Long createdAt = userSnapshot.child("created_at").getValue(Long.class);

                    // Kiểm tra thời gian và tính toán
                    if (createdAt != null && createdAt >= startOfDay && createdAt <= endOfDay) {
                        userTodayCount++; // Tăng số lượng người dùng đăng ký hôm nay
                    }
                }


                binding.tvLuotDangKyAmount.setText("" + userTodayCount);
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }
    private void layThongTinDKTrongKhoangThoiGian(int soNgay) {
        calendar.setTimeInMillis(System.currentTimeMillis()); // Đặt lại về hiện tại
        long startTime = LayThoigianCachDay(soNgay);
        long endTime = System.currentTimeMillis();

        dataUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int userCount = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Long createdAt = userSnapshot.child("created_at").getValue(Long.class);
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);

                    if (createdAt != null && createdAt >= startTime && createdAt <= endTime) {
                        userCount++;
                    }
                }
                binding.tvLuotDangKyAmount.setText("" + userCount);
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }

    private void layThongTInDoanhThu() {
        LayThoigianNgayHomNay(); // 23:59:59 hôm nay

        dataThanhToan.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double doanhthu = 0;
                for (DataSnapshot data : snapshot.getChildren()){
                    Long trangThai = data.child("idTrangThai").getValue(Long.class);
                    String ngaymua = data.child("paymentDate").getValue(String.class);
                    Long amount = data.child("amount").getValue(Long.class);
                    if (ngaymua != null && trangThai == 1 ) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            Date date = sdf.parse(ngaymua); // Chuyển đổi chuỗi thành Date
                            long timeInMillis = date.getTime(); // Lấy thời gian mili giây

                            // Kiểm tra xem timeInMillis có nằm trong khoảng startOfDay và endOfDay không
                            if (timeInMillis >= startOfDay && timeInMillis <= endOfDay) {
                                doanhthu += amount;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace(); // In lỗi nếu không thể phân tích cú pháp
                        }
                    }

                }
                // Định dạng và hiển thị số lượng gói VIP
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedDoanhThu = decimalFormat.format(doanhthu);
                binding.tvDoanhThuAmount.setText(formattedDoanhThu + " đ");
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }
    // doanh thu
    private void laythongtinDoanhThuTrongKhoang(int soNgay) {

        long startTime = LayThoigianCachDay(soNgay);
        long endTime = System.currentTimeMillis();

        dataThanhToan.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double doanhthu = 0;
                for (DataSnapshot data : snapshot.getChildren()){
                    Long trangThai = data.child("idTrangThai").getValue(Long.class);
                    String ngaymua = data.child("paymentDate").getValue(String.class);
                    Long amount = data.child("amount").getValue(Long.class);
                    if (ngaymua != null && trangThai == 1 ) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            Date date = sdf.parse(ngaymua); // Chuyển đổi chuỗi thành Date
                            long timeInMillis = date.getTime(); // Lấy thời gian mili giây

                            // Kiểm tra xem timeInMillis có nằm trong khoảng startOfDay và endOfDay không
                            if (timeInMillis >= startTime && timeInMillis <= endTime) {
                                doanhthu += amount;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace(); // In lỗi nếu không thể phân tích cú pháp
                        }
                    }

                }
                // Định dạng và hiển thị số lượng gói VIP
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedDoanhThu = decimalFormat.format(doanhthu);
                binding.tvDoanhThuAmount.setText(formattedDoanhThu + " đ");
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    // Truy Caap
    private void layThongTinTruyCapHomNay() {
        LayThoigianNgayHomNay(); // 23:59:59 hôm nay

        dataTruyCap.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot accessSnapshot : snapshot.getChildren()) {
                    String timestamp = accessSnapshot.child("thoigiantruycap").getValue(String.class);
                    if (timestamp != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            Date date = sdf.parse(timestamp); // Chuyển đổi chuỗi thành Date
                            long timeInMillis = date.getTime(); // Lấy thời gian mili giây

                            // Kiểm tra xem timeInMillis có nằm trong khoảng startOfDay và endOfDay không
                            if (timeInMillis >= startOfDay && timeInMillis <= endOfDay) {
                                count++;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace(); // In lỗi nếu không thể phân tích cú pháp
                        }
                    }
                }

                binding.tvTruyCapAmount.setText("" + count);
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }
    private void layThongTinTruyCapTrongKhoangThoiGian(int soNgay) {

        long startTime = LayThoigianCachDay(soNgay);
        long endTime = System.currentTimeMillis();


        dataTruyCap.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot accessSnapshot : snapshot.getChildren()) {
                    String timestamp = accessSnapshot.child("thoigiantruycap").getValue(String.class);

                    if (timestamp != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            Date date = sdf.parse(timestamp); // Chuyển đổi chuỗi thành Date
                            long timeInMillis = date.getTime(); // Lấy thời gian mili giây

                            // Kiểm tra xem timeInMillis có nằm trong khoảng startOfDay và endOfDay không
                            if (timeInMillis >= startTime && timeInMillis <= endTime) {
                                count++;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace(); // In lỗi nếu không thể phân tích cú pháp
                        }
                    }
                }

                binding.tvTruyCapAmount.setText("" + count);
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }
    // Lay thoi gian ngay hom nay
    private void LayThoigianNgayHomNay() {
        calendar = Calendar.getInstance();  // Đặt lại calendar về thời gian hiện tại
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        endOfDay = calendar.getTimeInMillis();
    }

    private long LayThoigianCachDay(int soNgay) {
        calendar = Calendar.getInstance();  // Đặt lại calendar về thời gian hiện tại
        calendar.add(Calendar.DAY_OF_YEAR, -soNgay);
        return calendar.getTimeInMillis();
    }


    private void xulyBieuDo() {
        // Lấy giá trị từ các TextView
        String doanhThuText = binding.tvDoanhThuAmount.getText().toString();
        String truyCapText = binding.tvTruyCapAmount.getText().toString();
        String luotDangKyText = binding.tvLuotDangKyAmount.getText().toString();
        String goiVIPText = binding.tvGoiVIPAmount.getText().toString();

        // Chuyển đổi giá trị từ TextView thành float
        float doanhThu = parseValue(doanhThuText);
        float truyCap = parseValue(truyCapText);
        float luotDangKy = parseValue(luotDangKyText);
        float goiVIP = parseValue(goiVIPText);

        // Tạo dữ liệu cho BarChart
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, doanhThu));
        barEntries.add(new BarEntry(1, truyCap));
        barEntries.add(new BarEntry(2, luotDangKy));
        barEntries.add(new BarEntry(3, goiVIP));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Thống kê");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f); // Độ rộng của cột


        // Cập nhật BarChart
        binding.barChart.setData(barData);
        binding.barChart.setFitBars(true); // Đảm bảo các cột vừa với biểu đồ
        binding.barChart.invalidate(); // Làm mới biểu đồ

        // Tùy chỉnh Legend (chú thích) cho biểu đồ
        Legend legend = binding.barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Đặt legend ở dưới
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // Canh giữa
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Hiển thị theo chiều ngang
        legend.setDrawInside(false); // Để bên ngoài biểu đồ
        legend.setWordWrapEnabled(true); // Tự động xuống dòng nếu cần
        legend.setYOffset(10f); // Khoảng cách giữa legend và biểu đồ
        legend.setXOffset(0f); // Khoảng cách bên trái/phải của legend
        legend.setTextSize(12f); // Kích thước chữ của legend

        // Đặt màu sắc và nhãn cho mỗi dữ liệu (thay thế cho tên chung "Thống kê")
        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(new LegendEntry("Doanh thu", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[0]));
        legendEntries.add(new LegendEntry("Truy cập", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[1]));
        legendEntries.add(new LegendEntry("Lượt đăng ký", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[2]));
        legendEntries.add(new LegendEntry("Gói VIP", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[3]));

        legend.setCustom(legendEntries); // Đặt các ghi chú tùy chỉnh vào legend

    }
    // Phương thức để chuyển đổi giá trị từ chuỗi thành float
    private float parseValue(String value) {
        value = value.replaceAll("[^\\d.]", ""); // Xóa các ký tự không phải số
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    //xu ly button menu
    //xu ly button menu
    private void xulybuttonMenu() {
        binding.ivButtonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the menu layout
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.menu_layout_admin, null);

                // Create the PopupWindow
                PopupWindow popupWindow = new PopupWindow(popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true);

                // Show the PopupWindow at the desired location
                popupWindow.showAsDropDown(binding.ivButtonMenu, 0, 0);

                // Handle Doanh thu button click
                popupView.findViewById(R.id.btn_doanh_thu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Doanh thu
//                        Intent intent = new Intent(AdminActivity.this, QLPhimActivity.class);
//                        startActivity(intent);
//                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });
                popupView.findViewById(R.id.btn_hotro).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AdminActivity.this, DSHoTroActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();
                    }
                });

                popupView.findViewById(R.id.btn_ThongBao).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AdminActivity.this, DSThongBaoActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

                popupView.findViewById(R.id.btn_ql_api).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AdminActivity.this, QuanLyAPI.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });
                popupView.findViewById(R.id.btn_ThongBao).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AdminActivity.this, DSThongBaoActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });
                popupView.findViewById(R.id.btn_ql_user).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AdminActivity.this, QLUserActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });
                popupView.findViewById(R.id.btn_quanly_phim).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AdminActivity.this, QLPhimActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

            }
        });
    }
}