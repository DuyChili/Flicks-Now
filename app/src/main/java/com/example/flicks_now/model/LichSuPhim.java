package com.example.flicks_now.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LichSuPhim {
    private DatabaseReference lichSuXemRef;
    private String idUser;
    private Context context;
    private String movieLink;

    public LichSuPhim(Context context) {
        this.context = context;
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        laythongtinUser();
    }

    private void laythongtinUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
    }

    public void luuLichSuXem(String movieSlug, String episodeName, List<ChiTietPhim.TapPhim.DuLieuServer> serverDataList) {
        if (movieSlug == null) {
            Toast.makeText(context, "Không thể lưu lịch sử, thiếu thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (idUser == null) {
            // Người dùng chưa đăng nhập, lưu vào LichSuXemKhongDangNhap
            luuLichSuXemKhiKhongDangNhap(movieSlug, episodeName);
        } else {
            // Người dùng đã đăng nhập, tiếp tục với logic hiện tại
            Query query = lichSuXemRef.orderByChild("id_user").equalTo(idUser);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String idLichSuXem = null;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String existingSlug = snapshot.child("slug").getValue(String.class);
                        if (existingSlug != null && existingSlug.equals(movieSlug)) {
                            idLichSuXem = snapshot.getKey();
                            break;
                        }
                    }

                    if (idLichSuXem != null) {
                        String movieLink = layTenTapPhim(episodeName, serverDataList);
                        capNhatPhimVaoCSDL(idLichSuXem, episodeName, movieLink);
                    } else {
                        String movieLink = layTenTapPhim(episodeName, serverDataList);
                        themMoiPhimVaoCSDL(movieSlug, episodeName, movieLink);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Lỗi khi kiểm tra lịch sử", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void luuLichSuXemKhiKhongDangNhap(String movieSlug, String episodeName) {
        DatabaseReference lichSuKhongDangNhapRef = FirebaseDatabase.getInstance().getReference("LichSuXemKhongDangNhap");
        String idLichSuXem = lichSuKhongDangNhapRef.push().getKey();
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> lichSuXem = new HashMap<>();
        lichSuXem.put("slug", movieSlug);
        lichSuXem.put("watched_at", watchedAt);
        lichSuXem.put("episode", episodeName);
        lichSuXem.put("movie_link", movieLink);

        lichSuKhongDangNhapRef.child(idLichSuXem).setValue(lichSuXem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Lưu lịch sử xem phim thành công cho người dùng không đăng nhập", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lưu lịch sử xem phim thất bại cho người dùng không đăng nhập", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String layTenTapPhim(String episodeName, List<ChiTietPhim.TapPhim.DuLieuServer> serverDataList) {
        for (ChiTietPhim.TapPhim.DuLieuServer episode : serverDataList) {
            if (episode.getName().equals(episodeName)) {
                return episode.getLinkM3u8();
            }
        }
        return null;
    }
    // Hàm để thêm mới lịch sử xem phim nếu slug chưa tồn tại
    private void themMoiPhimVaoCSDL(String movieSlug, String episodeName, String movieLink) {
        // Tạo ID mới cho lịch sử xem phim
        String idLichSuXem = lichSuXemRef.push().getKey();
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        // Tạo bản ghi lịch sử xem phim
        Map<String, Object> lichSuXem = new HashMap<>();
        lichSuXem.put("id_user", idUser);
        lichSuXem.put("slug", movieSlug);
        lichSuXem.put("watched_at", watchedAt);
        lichSuXem.put("movie_link", movieLink);
        lichSuXem.put("episode", episodeName);
        // Lưu vào Firebase dưới node `LichSuXem`
        lichSuXemRef.child(idLichSuXem).setValue(lichSuXem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Lưu lịch sử xem phim thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lưu lịch sử xem phim thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void capNhatPhimVaoCSDL(String idLichSuXem, String episodeName, String movieLink) {
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("episode", episodeName);
        updates.put("movie_link", movieLink);
        updates.put("watched_at", watchedAt);

        lichSuXemRef.child(idLichSuXem).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Cập nhật lịch sử xem thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Cập nhật lịch sử xem thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
