package com.example.flicks_now.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flicks_now.adapter.LichSuAdapter;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.api.ApiService;
import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.databinding.ActivityLichSuXemBinding;
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


public class LichSuXemActivity extends AppCompatActivity {
    private ActivityLichSuXemBinding binding;
    private LichSuAdapter lichSuAdapter;
    private List<ChiTietPhim.MovieItem> watchedMoviesList;
    private List<ChiTietPhim.TapPhim.DuLieuServer> tapPhimList = new ArrayList<>();
    private DatabaseReference lichSuXemRef;
    private String idUser;
    private ApiService apiService;
    private DatabaseReference usersRef;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLichSuXemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        apiService = ApiClient.getClient().create(ApiService.class);
        setControl();

        hienThiLichSuPhim();
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch sử đã xem"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            hienThiLichSuPhim();
        });
    }

    public void setControl() {
        watchedMoviesList = new ArrayList<>();
        lichSuAdapter = new LichSuAdapter(LichSuXemActivity.this, watchedMoviesList);
        binding.rcvlichsuxem.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rcvlichsuxem.setAdapter(lichSuAdapter); // Set the adapter
    }

    private void hienThiLichSuPhim() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Lấy người dùng hiện tại

        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xoa du lieu cu
        watchedMoviesList.clear();
        lichSuAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView

        // Lấy id_user từ database
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idUser = dataSnapshot.child("id_user").getValue(String.class); // Lấy id_user

                    if (idUser == null) {
                        Toast.makeText(LichSuXemActivity.this, "Lỗi: id_user không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Sử dụng idUser để tải lịch sử xem phim
                    lichSuXemRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String movieSlug = snapshot.child("slug").getValue(String.class);
                                String episodeName = snapshot.child("episode_name").getValue(String.class);

                                if (movieSlug != null) {
                                    ChiTietPhim.MovieItem movieItem = new ChiTietPhim.MovieItem();
                                    movieItem.setSlug(movieSlug);
                                    movieItem.setEpisodeCurrent(episodeName);
                                    watchedMoviesList.add(0, movieItem);
                                    lichSuAdapter.notifyItemInserted(0); // Notify that an item was inserted at position 0
                                    chiTietPhim(movieSlug, movieItem);
                                }
                            }
                            lichSuAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView sau khi thêm tất cả phim
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(LichSuXemActivity.this, "Lỗi khi tải lịch sử xem", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }
                    });
                } else {
                    Toast.makeText(LichSuXemActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LichSuXemActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }


    // Load thong tin phim
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
                                Log.e("LichSuXemActivity", "Slug phim là null. Không thể lấy liên kết phim.");
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
                                                Log.d("LichSuXemActivity", "Liên kết phim: " + movieLink);

                                                // Tạo intent để mở màn hình phát phim
                                                Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                                                intent.putExtra("movie_link", movieLink);
                                                intent.putExtra("episode", episodeName);
                                                intent.putExtra("slug", slug);
                                                intent.putExtra("lichsu", true);

                                                // Bắt đầu màn hình phát phim
                                                view.getContext().startActivity(intent);
                                            } else {
                                                Log.e("LichSuXemActivity", "Liên kết phim là null cho slug: " + movieSlug);
                                            }
                                        }
                                    } else {
                                        Log.e("LichSuXemActivity", "Không tìm thấy phim cho slug: " + movieSlug);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("LichSuXemActivity", "Lỗi khi lấy liên kết phim", databaseError.toException());
                                }
                            });
                        }
                    });

                    binding.progressBar.setVisibility(View.GONE);
                    binding.layout.setVisibility(View.VISIBLE);
                } else {
                    Log.e("LichSuXemActivity", "Không thể lấy chi tiết phim cho slug: " + slug);
                }
            }

            @Override
            public void onFailure(Call<ChiTietPhim> call, Throwable t) {
                Log.e("LichSuXemActivity", "Lỗi khi lấy thông tin chi tiết phim", t);
            }
        });
    }


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
}

