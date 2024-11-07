package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flicks_now.adapter.LichSuAdapter;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.api.ApiService;

import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityCanhanBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaNhanActivity extends AppCompatActivity {
    private ActivityCanhanBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private boolean isUserLoggedIn = false; // Biến để theo dõi trạng thái đăng nhập
    private DatabaseReference usersRef;
    private DatabaseReference lichSuXemRef;
    private ApiService apiService;
    private List<ChiTietPhim.MovieItem> watchedMoviesList;
    private LichSuAdapter lichSuAdapter;
    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Data Binding
        binding = ActivityCanhanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        setControl();
        setEven();

    }
    public void setControl(){
        watchedMoviesList = new ArrayList<>();
        lichSuAdapter = new LichSuAdapter(CaNhanActivity.this, watchedMoviesList);
        binding.rcvLichSu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rcvLichSu.setAdapter(lichSuAdapter);
    }
    public void setEven(){
        laythongtinUser();
        Toast.makeText(CaNhanActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();
        binding.tvTenNguoiDung.setText(nameUser);
        binding.tvEmail.setText(emailUser);

        // Kiểm tra trạng thái đăng nhập
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (idUser != null) {
            if (idLoaiND == 3 || idLoaiND == 2){
                isUserLoggedIn = true;
                binding.btnDangNhap.setVisibility(View.VISIBLE);
                binding.btnDangNhap.setText("Admin");
            }else{
                isUserLoggedIn = true; // Người dùng đã đăng nhập
                binding.btnDangNhap.setVisibility(View.GONE);
            }
        }
        else {
            isUserLoggedIn = false; // Người dùng chưa đăng nhập
            binding.btnDangNhap.setVisibility(View.VISIBLE);
            binding.btnDangNhap.setText("Đăng Nhập"); // Đổi văn bản nút thành "Đăng Nhập"
        }

        binding.btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.btnDangNhap.getText().equals("Admin")){
                    Intent intent = new Intent(CaNhanActivity.this, AdminActivity.class );
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(CaNhanActivity.this, DangNhapActivity.class);
                    startActivity(intent);
                }
        }});
        binding.caiDat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CaNhanActivity.this, CaiDatActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.dsYeuThich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaNhanActivity.this, DanhSachYeuThichActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.tvXemtatca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CaNhanActivity.this, LichSuXemActivity.class);
                startActivity(intent);
            }
        });
        navigationBottom();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        apiService = ApiClient.getClient().create(ApiService.class);
        hienThiLichSuXem();
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            hienThiLichSuXem();
        });

    }



    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);
    }


    private void navigationBottom() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Đặt item mặc định được chọn là màn hình Home
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Xử lý sự kiện chọn item của Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(CaNhanActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.nav_vip) {
                    intent = new Intent(CaNhanActivity.this, VipActivity.class);
                } else if(item.getItemId() == R.id.nav_profile) {
                    return  true;
                }else if (item.getItemId() == R.id.nav_download) {
                    intent = new Intent(CaNhanActivity.this, TaiPhimActivity.class);
                }
                // Pass the selected item to the new Activity
                if (intent != null) {
                    intent.putExtra("selected_item_id", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0);  // No animation for smooth transition
                    return true;
                }
                return false;

            }
        });
    }
    private void hienThiLichSuXem() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người dùng từ database
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idUser = dataSnapshot.child("id_user").getValue(String.class); // Lấy id_user

                    if (idUser == null) {
                        Toast.makeText(CaNhanActivity.this, "Lỗi: id_user không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Sử dụng idUser để tải lịch sử xem phim
                    lichSuXemRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            watchedMoviesList.clear(); // Xóa danh sách cũ trước khi thêm dữ liệu mới

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String movieSlug = snapshot.child("slug").getValue(String.class);
                                String episodeName = snapshot.child("episode_name").getValue(String.class);

                                if (movieSlug != null) {
                                    ChiTietPhim.MovieItem movieItem = new ChiTietPhim.MovieItem();
                                    movieItem.setSlug(movieSlug);
                                    movieItem.setEpisodeCurrent(episodeName);
                                    watchedMoviesList.add(0,movieItem);
                                    lichSuAdapter.notifyItemChanged(0);
                                    chiTietPhim(movieSlug, movieItem); // Lấy chi tiết phim
                                }
                            }
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(CaNhanActivity.this, "Lỗi khi tải lịch sử xem", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }
                    });
                } else {
                    Toast.makeText(CaNhanActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CaNhanActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    private void chiTietPhim(String slug, ChiTietPhim.MovieItem movieItem) {
        Call<ChiTietPhim> call = apiService.getChiTietPhim(slug);
        call.enqueue(new Callback<ChiTietPhim>() {
            @Override
            public void onResponse(Call<ChiTietPhim> call, Response<ChiTietPhim> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChiTietPhim movieDetail = response.body();
                    movieItem.setName(movieDetail.getMovie().getName());
                    movieItem.setPosterUrl(movieDetail.getMovie().getPosterUrl());
                    lichSuAdapter.notifyDataSetChanged();

                    // Đặt listener cho mỗi mục phim trong lịch sử
                    lichSuAdapter.setRecyclerViewItemClickListener(new LichSuAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // Lấy mục phim đã nhấn
                            ChiTietPhim.MovieItem clickedMovie = watchedMoviesList.get(position);
                            String movieSlug = clickedMovie.getSlug();

                            if (movieSlug == null) {
                                Log.e("CaNhanActivity", "Slug phim là null. Không thể lấy liên kết phim.");
                                return; // Kết thúc hàm nếu slug là null
                            }

                            // Tham chiếu đến Firebase để lấy liên kết phim
                            DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
                            moviesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            // Lấy liên kết phim
                                            String movieLink = snapshot.child("movie_link").getValue(String.class);
                                            String episodeName = snapshot.child("episode").getValue(String.class);
                                            String slug = snapshot.child("slug").getValue(String.class);

                                            if (movieLink != null) {
                                                Log.d("CaNhanActivity", "Liên kết phim: " + movieLink);

                                                // Tạo intent để mở màn hình phát phim
                                                Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                                                intent.putExtra("movie_link", movieLink);
                                                intent.putExtra("episode", episodeName);
                                                intent.putExtra("slug", slug);
                                                intent.putExtra("lichsu", true);

                                                // Bắt đầu màn hình phát phim
                                                view.getContext().startActivity(intent);
                                            } else {
                                                Log.e("CaNhanActivity", "Liên kết phim là null cho slug: " + movieSlug);
                                            }
                                        }
                                    } else {
                                        Log.e("CaNhanActivity", "Không tìm thấy phim cho slug: " + movieSlug);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("CaNhanActivity", "Lỗi khi lấy liên kết phim", databaseError.toException());
                                }
                            });
                        }
                    });

                } else {
                    Log.e("CaNhanActivity", "Không thể lấy chi tiết phim cho slug: " + slug);
                }
            }

            @Override
            public void onFailure(Call<ChiTietPhim> call, Throwable t) {
                Log.e("CaNhanActivity", "Lỗi khi lấy thông tin chi tiết phim", t);
            }
        });
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
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Xóa cờ giữ màn hình sáng khi ứng dụng không còn hoạt động
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}