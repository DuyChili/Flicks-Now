package com.example.flicks_now.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.flicks_now.activity.DangNhapActivity;

import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityXemPhimBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DSPhimYeuThich {
    private String idUser;
    private Context context;
    private String movieSlug;
    private ActivityXemPhimBinding binding;

    public DSPhimYeuThich(Context context, ActivityXemPhimBinding binding, String movieSlug) {
        this.context = context;
        this.binding = binding;
        this.movieSlug = movieSlug;
        laythongtinUser();
    }
    private void laythongtinUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
    }

    public void themYeuThich() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (userId == null) {
            new AlertDialog.Builder(context)
                    .setTitle("Cần đăng nhập")
                    .setMessage("Bạn cần đăng nhập để thêm vào danh sách yêu thích. Bạn có muốn đăng nhập ngay?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Chuyển đến màn hình đăng nhập
                        Intent intent = new Intent(context, DangNhapActivity.class);
                        context.startActivity(intent);
                    })
                    .setNegativeButton("Không", null)
                    .show();
            return; // Ngừng thực hiện nếu người dùng chưa đăng nhập
        }

        // Tham chiếu đến bảng Favorite
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");

        // Tham chiếu đến vị trí phim yêu thích
        favoritesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean movieExists = false;
                String movieIdToDelete = ""; // Để lưu id_phim khi cần xóa

                // Kiểm tra từng entry trong danh sách yêu thích
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PhimYeuThich phimYeuThich = snapshot.getValue(PhimYeuThich.class);
                    if (phimYeuThich != null && phimYeuThich.getId_user() != null && phimYeuThich.getId_user().equals(userId)) {
                        movieExists = true;
                        movieIdToDelete = snapshot.getKey(); // Lưu id_phim để xóa
                        break;
                    }
                }
                if (movieExists) {
                    // Nếu phim đã tồn tại, xóa nó
                    favoritesRef.child(movieIdToDelete).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                                // Đổi màu trái tim về trắng
                                binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24);
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    // Nếu phim chưa tồn tại, thêm nó
                    PhimYeuThich phimYeuThich = new PhimYeuThich(userId, movieSlug);
                    favoritesRef.push().setValue(phimYeuThich)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                                // Đổi màu trái tim thành đỏ
                                binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24_red);
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void kiemTraYeuThich() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");
        // Kiểm tra nếu phim đã có trong danh sách yêu thích
        favoritesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean movieExists = false;

                // Kiểm tra từng entry trong danh sách yêu thích
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PhimYeuThich phimYeuThich = snapshot.getValue(PhimYeuThich.class);
                    if (phimYeuThich != null && phimYeuThich.getId_user() != null && userId != null && phimYeuThich.getId_user().equals(userId)) {
                        movieExists = true;
                        break;
                    }
                }
                // Nếu phim đã tồn tại, đổi màu nút thành đỏ
                if (movieExists) {
                    binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24_red);
                } else {
                    // Nếu phim chưa tồn tại, để màu trắng
                    binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
