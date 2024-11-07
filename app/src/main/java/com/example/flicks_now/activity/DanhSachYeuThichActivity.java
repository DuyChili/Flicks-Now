package com.example.flicks_now.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flicks_now.adapter.YeuThichAdapter;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.api.ApiService;
import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.databinding.ActivityFavoriteMoviesBinding;
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

public class DanhSachYeuThichActivity extends AppCompatActivity {
    private ActivityFavoriteMoviesBinding binding;
    private YeuThichAdapter yeuThichAdapter;
    private List<ChiTietPhim.MovieItem> PhimYeuThich = new ArrayList<>();
    private DatabaseReference yeuThichRef;
    private ApiService apiService;
    private DatabaseReference usersRef;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo binding
        binding = ActivityFavoriteMoviesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Gán layout cho a

        apiService = ApiClient.getClient().create(ApiService.class);

        yeuThichRef = FirebaseDatabase.getInstance().getReference("Favorite");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        setControl();
        loadFavoriteMovies();
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh Sách yêu thích"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFavoriteMovies();
        });
    }
    private void setControl() {
        PhimYeuThich = new ArrayList<>();
        yeuThichAdapter = new YeuThichAdapter(DanhSachYeuThichActivity.this, PhimYeuThich);
        binding.rvFavorites.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvFavorites.setAdapter(yeuThichAdapter); // Set the adapter
    }

    private void loadFavoriteMovies() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Lấy người dùng hiện tại

        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show();
            return;
        }
        // Clear the watchedMoviesList to avoid duplication
        PhimYeuThich.clear();
        yeuThichAdapter.notifyDataSetChanged(); // Notify adapter about the cleared list
        // Lấy id_user từ database
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idUser = dataSnapshot.child("id_user").getValue(String.class); // Lấy id_user

                    if (idUser == null) {
                        Toast.makeText(DanhSachYeuThichActivity.this, "Lỗi: id_user không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // Sử dụng idUser để tải danh sách yêu thích
                    yeuThichRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ChiTietPhim.MovieItem movieItem = new ChiTietPhim.MovieItem();
                                String movieSlug = snapshot.child("slug").getValue(String.class);
                                movieItem.setSlug(movieSlug);
                                fetchMovieDetails(movieSlug);
                            }
                            binding.progressBar.setVisibility(View.GONE);
                            binding.layout.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(DanhSachYeuThichActivity.this, "Lỗi khi tải danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }
                    });
                } else {
                    Toast.makeText(DanhSachYeuThichActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DanhSachYeuThichActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }
    //Load thong tin phim
    private void fetchMovieDetails(String slug) {
        Call<ChiTietPhim> call = apiService.getChiTietPhim(slug);
        call.enqueue(new Callback<ChiTietPhim>() {
            @Override
            public void onResponse(Call<ChiTietPhim> call, Response<ChiTietPhim> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChiTietPhim.MovieItem movieItem = response.body().getMovie();
                    PhimYeuThich.add(movieItem); // Thêm phim vào danh sách yêu thích
                    // Notify the adapter that data has changed
                    yeuThichAdapter.notifyDataSetChanged();  // Notify adapter for changes
                    yeuThichAdapter.setRecyclerViewItemClickListener(new YeuThichAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // Truyền slug sang ChiTietActivity
                            String slug = PhimYeuThich.get(position).getSlug(); // Lấy slug từ phim
                            Intent intent = new Intent(DanhSachYeuThichActivity.this, ChiTietPhimActivity.class);
                            intent.putExtra("slug", slug); // Truyền slug
                            startActivity(intent);
                        }
                    });
                    // Remove the reinitialization of lichSuAdapter
                    // Update click listener outside this function if necessary
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layout.setVisibility(View.VISIBLE);
                } else {
                    Log.e("FavoriteMoviesActivity", "Failed to fetch movie details for slug: " + slug);
                }
            }

            @Override
            public void onFailure(Call<ChiTietPhim> call, Throwable t) {
                Log.e("FavoriteMoviesActivity", "Error fetching movie details", t);
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
