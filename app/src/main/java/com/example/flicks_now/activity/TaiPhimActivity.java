package com.example.flicks_now.activity;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.flicks_now.adapter.TaiPhimAdapter;
import com.example.flicks_now.model.MovieItem;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityDownLoadBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TaiPhimActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    private ActivityDownLoadBinding binding; // Sử dụng Binding
    private TaiPhimAdapter adapter;
    private List<MovieItem> downloadedMovies = new ArrayList<>(); // Danh sách phim đã tải

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo binding
        binding = ActivityDownLoadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Thiết lập RecyclerView với GridLayoutManager (3 cột)
        binding.recyclerViewDownloadedMovies.setLayoutManager(new GridLayoutManager(this, 3));

        // Lấy danh sách phim đã tải
        loadDownloadedMovies();

        // Thiết lập adapter cho RecyclerView
        adapter = new TaiPhimAdapter(downloadedMovies, new TaiPhimAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(MovieItem movieItem) {
                playDownloadedMovie(movieItem);
            }

            @Override
            public void onDeleteMovie(MovieItem movie, int position) {
                deleteMovie(movie, position);
            }
        });
        binding.recyclerViewDownloadedMovies.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = binding.bottomNavigation;

        // Đặt item mặc định là màn hình Download
        bottomNavigationView.setSelectedItemId(R.id.nav_download);

        // Xử lý sự kiện chọn item của Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;

            if (item.getItemId() == R.id.nav_home) {
                intent = new Intent(TaiPhimActivity.this, MainActivity.class);
            } else if (item.getItemId() == R.id.nav_vip) {
                intent = new Intent(TaiPhimActivity.this, VipActivity.class);
            } else if (item.getItemId() == R.id.nav_profile) {
                intent = new Intent(TaiPhimActivity.this, CaNhanActivity.class);
            } else if (item.getItemId() == R.id.nav_download) {
                return true; // Không khởi tạo lại Activity
            }

            if (intent != null) {
                intent.putExtra("selected_item_id", item.getItemId());
                startActivity(intent);
                overridePendingTransition(0, 0); // Không có hoạt ảnh cho chuyển đổi mượt mà
            }
            return true;
        });
    }

    private void loadDownloadedMovies() {
        // Lấy thư mục chứa phim đã tải
        File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies");

        if (movieDir.exists() && movieDir.isDirectory()) {
            File[] movieFiles = movieDir.listFiles();
            if (movieFiles != null) {
                for (File movieFile : movieFiles) {
                    // Kiểm tra nếu là thư mục
                    if (movieFile.isDirectory()) {
                        String movieName = movieFile.getName();
                        File posterFile = new File(movieFile, movieName + "_poster.jpg");

                        // Kiểm tra nếu tệp poster tồn tại
                        if (posterFile.exists()) {
                            downloadedMovies.add(new MovieItem(movieName, posterFile.getAbsolutePath()));
                        }
                    }
                }
            }
        }
    }

    private void deleteMovie(MovieItem movie, int position) {
        // Hiển thị thông báo xác nhận trước khi xóa
        new AlertDialog.Builder(this)
                .setTitle("Cảnh báo!")
                .setMessage("Bạn có chắc muốn xóa phim \"" + movie.getName() + "\" không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Nếu người dùng xác nhận xóa, tiến hành xóa thư mục phim
                    File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies/" + movie.getName());
                    if (movieDir.exists() && movieDir.isDirectory()) {
                        deleteRecursive(movieDir); // Gọi phương thức xóa đệ quy
                    }

                    // Xóa phim khỏi danh sách
                    downloadedMovies.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Đã xóa phim \"" + movie.getName() + "\"", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Không", null) // Nếu người dùng không muốn xóa, chỉ cần đóng dialog
                .show();
    }
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
    // Phát phim đã tải khi nhấn vào
    private void playDownloadedMovie(MovieItem movieItem) {
        Intent intent = new Intent(this, XemPhimTaixuongActivity.class);
        intent.putExtra("movie_name", movieItem.getName());
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Xóa cờ giữ màn hình sáng khi ứng dụng không còn hoạt động
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.finishAffinity();  // Exit the app
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Nhấn thoát thêm một lần nữa", Toast.LENGTH_SHORT).show();

        // Reset the flag after 2 seconds
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

}

