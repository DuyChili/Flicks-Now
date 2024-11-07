package com.example.flicks_now.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityXemPhimBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DanhGiaPhim {
    private ActivityXemPhimBinding binding;
    private DatabaseReference ratingsRef;
    private Context context;
    private String idUser;
    private String movieSlug;

    public DanhGiaPhim(Context context, ActivityXemPhimBinding binding, String movieSlug) {
        this.context = context;
        this.binding = binding;
        this.movieSlug = movieSlug;
        laythongtinUser();
        // Khởi tạo tham chiếu đến bảng Ratings
        ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
    }


    public void luuDanhGia(String movieSlug, String userId, float rating) {
        // Lưu đánh giá vào Firebase
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("userId", userId);
        ratingData.put("rating", rating);
        ratingData.put("ratedAt", System.currentTimeMillis());

        new AlertDialog.Builder(context)
                .setTitle("Đánh Giá")
                .setMessage("Cảm ơn bạn đã đáng giá")
                .setPositiveButton("Ok", (dialog, which) -> {

                    ratingsRef.child(movieSlug).child(userId).setValue(ratingData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đánh giá đã được lưu!", Toast.LENGTH_SHORT).show();
                                // Cập nhật rating cho RatingBar
                                binding.ratingBar.setRating(rating); // Cập nhật số sao đã đánh giá
                                binding.ratingBar.setProgressTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.color_your_rating))); // Đặt màu sắc cho RatingBar
                                // Gọi hàm tính toán điểm trung bình
                                tinhTrungBinhDanhGia();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Lỗi khi lưu đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .show(); // Hiển thị hộp thoại
    }


    public void tinhTrungBinhDanhGia() {
        ratingsRef.child(movieSlug).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int luot = 0;
                float tongDanhGia = 0;
                // Lặp qua tất cả các đánh giá của phim
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    // Lấy thông tin đánh giá
                    float rating = ratingSnapshot.child("rating").getValue(Float.class);
                    // Tính tổng điểm và số lượng đánh giá
                    tongDanhGia += rating; // Tổng điểm
                    luot++; // Đếm số lượng đánh giá
                }

                if (luot > 0) {
                    // Tính trung bình sao
                    float diemDanhGia = tongDanhGia / luot;
                    // Cập nhật giao diện với tổng số đánh giá và trung bình sao
                    binding.tvAverageRating.setText("( " + diemDanhGia + " / " + luot + " lượt)");
                } else {
                    // Nếu không có đánh giá, hiển thị thông báo và đặt điểm trung bình là 0
                    binding.tvAverageRating.setText("( 0 / 0 lượt )");
                    binding.ratingBar.setRating(0); // Reset ratingBar nếu không có đánh giá
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
                Toast.makeText(context, "Lỗi khi tính toán đánh giá: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void kiemTraDanhGia() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim

        // Kiểm tra nếu người dùng đã đánh giá phim hay chưa
        DatabaseReference ratingRef;

        if (userId != null) {
            // Nếu người dùng đã đăng nhập, lấy thông tin đánh giá từ Firebase
            ratingRef = FirebaseDatabase.getInstance().getReference("Ratings")
                    .child(movieSlug)  // slug của phim
                    .child(userId);  // ID người dùng
            // Kiểm tra nếu người dùng đã đánh giá phim
            ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Người dùng đã đánh giá, lấy rating
                        int nguoiDungDanhGia = dataSnapshot.child("rating").getValue(Integer.class);
                        // Highlight sao đã đánh giá với màu sắc tương ứng
                        binding.ratingBar.setRating(nguoiDungDanhGia);
                        binding.ratingBar.setIsIndicator(false); // Cho phép người dùng chỉnh sửa
                        binding.ratingBar.setProgressTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.color_your_rating))); // Đặt màu sao theo rating
                    } else {
                        // Người dùng chưa đánh giá
                        binding.ratingBar.setRating(0);
                        binding.ratingBar.setIsIndicator(false); // Cho phép đánh giá
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Xử lý lỗi
                    Toast.makeText(context, "Lỗi khi kiểm tra đánh giá: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nếu người dùng chưa đăng nhập, cho phép đánh giá nhưng không lưu vào Firebase
            binding.ratingBar.setRating(0);
            binding.ratingBar.setIsIndicator(false); // Cho phép đánh giá
            binding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                Toast.makeText(context, "Bạn đã đánh giá " + rating + " sao", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void laythongtinUser() {
        // Lấy thông tin người dùng từ SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
    }
}
