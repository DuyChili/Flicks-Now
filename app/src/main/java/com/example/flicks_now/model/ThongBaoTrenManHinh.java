package com.example.flicks_now.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.flicks_now.R;
import com.example.flicks_now.activity.ThongBaoActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThongBaoTrenManHinh {
    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String CHANNEL_ID = "PhimChill"; // Đặt ID kênh thông báo

    // Phương thức khởi tạo, tạo kênh thông báo và khởi tạo SharedPreferences
    public ThongBaoTrenManHinh(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("CaiDatThongBao", Context.MODE_PRIVATE); // Khởi tạo SharedPreferences
        taoKenhThongBao(); // Tạo kênh thông báo khi khởi tạo
    }

    // Phương thức khởi động dịch vụ thông báo khi ứng dụng tắt
    public void batDichVuThongBao() {
        Intent serviceIntent = new Intent(context, ThongBaoKhiUngDungTat.class);
        context.startService(serviceIntent); // Khởi động dịch vụ
    }

    // Tạo kênh thông báo (yêu cầu Android 8.0 trở lên)
    private void taoKenhThongBao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence tenKenh = "Phimchill"; // Tên kênh
            String moTaKenh = "Ung dung xem phim chill"; // Mô tả kênh
            int doQuanTrong = NotificationManager.IMPORTANCE_DEFAULT; // Độ quan trọng của thông báo

            NotificationChannel kenh = new NotificationChannel(CHANNEL_ID, tenKenh, doQuanTrong);
            kenh.setDescription(moTaKenh);

            NotificationManager quanLyThongBao = context.getSystemService(NotificationManager.class);
            quanLyThongBao.createNotificationChannel(kenh);
            Log.d("ThongBaoTrenManHinh", "Kênh thông báo đã được tạo: " + CHANNEL_ID);
        }
    }

    // Phương thức lấy ID người dùng từ bảng Users trên Firebase
    public void layIdNguoiDungHienTai() {
        FirebaseUser nguoiDungFirebase = FirebaseAuth.getInstance().getCurrentUser();
        if (nguoiDungFirebase != null) { // Kiểm tra xem người dùng có đang đăng nhập không
            String idNguoiDungFirebase = nguoiDungFirebase.getUid();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

            usersRef.orderByKey().equalTo(idNguoiDungFirebase).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String id_user = userSnapshot.child("id_user").getValue(String.class);
                            if (id_user != null) {
                                Log.d("ThongBaoTrenManHinh", "Tìm thấy ID người dùng: " + id_user);
                                batDauLangNgheThongBao(id_user); // Bắt đầu lắng nghe thông báo cho người dùng đã đăng nhập
                            } else {
                                Log.d("ThongBaoTrenManHinh", "id_user null với người dùng: " + idNguoiDungFirebase);
                            }
                        }
                    } else {
                        Toast.makeText(context, "Không tìm thấy người dùng trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                        Log.d("ThongBaoTrenManHinh", "Không tìm thấy người dùng với ID: " + idNguoiDungFirebase);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Lỗi khi lấy thông tin người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ThongBaoTrenManHinh", "Lỗi khi lấy người dùng: " + error.getMessage());
                }
            });
        } else {
            // Người dùng chưa đăng nhập, không lắng nghe thông báo
            Toast.makeText(context, "Người dùng chưa đăng nhập. Không nhận được thông báo.", Toast.LENGTH_SHORT).show();
            Log.d("ThongBaoTrenManHinh", "Người dùng chưa đăng nhập.");
        }
    }

    // Hiển thị thông báo trên thanh trạng thái
    private void hienThongBaoTrenThanhTrangThai(String tieuDe, String noiDung) {
        NotificationManager quanLyThongBao = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo Intent để chuyển đến màn hình Hỗ trợ khi người dùng nhấn vào thông báo
        Intent intent = new Intent(context, ThongBaoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Đảm bảo rằng Activity sẽ được mở mới

        // Tạo PendingIntent từ Intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);



        // Tạo thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Biểu tượng thông báo
                .setContentTitle(tieuDe) // Tiêu đề thông báo
                .setContentText(noiDung) // Nội dung thông báo
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Đặt mức độ ưu tiên
                .setAutoCancel(true) // Tự động xóa thông báo khi người dùng nhấn vào
                .setContentIntent(pendingIntent); // Gán PendingIntent cho thông báo

        // Hiển thị thông báo
        quanLyThongBao.notify((int) System.currentTimeMillis(), builder.build());
        Log.d("ThongBaoTrenManHinh", "Thông báo hiển thị: " + tieuDe);
    }

    // Lắng nghe thông báo từ Firebase chỉ dành cho người dùng hiện tại
    public void batDauLangNgheThongBao(String idNguoiDungHienTai) {
        DatabaseReference thongBaoRef = FirebaseDatabase.getInstance().getReference("ThongBao");

        // Lấy thời gian của thông báo gần nhất đã nhận
        String thoiGianThongBaoGanNhat = sharedPreferences.getString("thoiGianThongBaoGanNhat", "1970-01-01 00:00:00");

        // Chuyển đổi thời gian lưu trữ từ chuỗi sang đối tượng Date
        SimpleDateFormat dinhDangNgayGio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date thoiGianGanNhat = null;
        try {
            thoiGianGanNhat = dinhDangNgayGio.parse(thoiGianThongBaoGanNhat);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date thoiGianCuoiCung = thoiGianGanNhat;
        thongBaoRef.orderByChild("id_user").equalTo(idNguoiDungHienTai)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                        ThongBao thongBao = dataSnapshot.getValue(ThongBao.class);
                        if (thongBao != null) {
                            // Chuyển đổi thời gian thông báo từ chuỗi sang đối tượng Date
                            Date thoiGianThongBao = null;
                            try {
                                thoiGianThongBao = dinhDangNgayGio.parse(thongBao.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            // Kiểm tra nếu thông báo mới hơn thông báo đã nhận gần nhất
                            if (thoiGianThongBao != null && thoiGianCuoiCung != null && thoiGianThongBao.after(thoiGianCuoiCung)) {
                                // Hiển thị thông báo
                                hienThongBaoTrenThanhTrangThai(thongBao.getTitle(), thongBao.getContent());

                                // Lưu thời gian của thông báo này vào SharedPreferences
                                sharedPreferences.edit().putString("thoiGianThongBaoGanNhat", thongBao.getTime()).apply();
                            }
                        } else {
                            Log.d("ThongBaoTrenManHinh", "ThongBao null");
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                        Log.d("ThongBaoTrenManHinh", "Thông báo thay đổi.");
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("ThongBaoTrenManHinh", "Thông báo bị xóa.");
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                        Log.d("ThongBaoTrenManHinh", "Thông báo di chuyển.");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "Lỗi khi nhận thông báo: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ThongBaoTrenManHinh", "Lỗi nhận thông báo: " + databaseError.getMessage());
                    }
                });
    }
}
