package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.flicks_now.adapter.TapPhimAdapter;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.api.ApiService;
import com.example.flicks_now.databinding.ActivityChiTietPhimBinding;
import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.model.LichSuPhim;
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

public class ChiTietPhimActivity extends AppCompatActivity {
    private String movieSlug;
    private List<ChiTietPhim.TapPhim.DuLieuServer> serverDataList = new ArrayList<>();
    private ActivityChiTietPhimBinding binding;
    private ApiService apiService;
    private String movieLink;
    private ChiTietPhim.MovieItem movieDetails;
    private TapPhimAdapter tapPhimAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private boolean checkPhim = true;
    private LichSuPhim lichSuPhim;
    private DatabaseReference ratingsRef;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChiTietPhimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setEvent();

        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết phim"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
    }
    private void setEvent() {
        laythongtinUser();
        // Lấy slug từ Intent
        movieSlug = getIntent().getStringExtra("slug");
        // Lấy chi tiết phim
        apiService = ApiClient.getClient().create(ApiService.class);
        hienThiChiTietPhim(movieSlug);

        binding.btnXemPhim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (movieLink != null && !movieLink.isEmpty()) {
                    Log.d("Check",String.valueOf(checkPhim));
                    Log.d("Check loại người dùng",String.valueOf(idLoaiND));
                    if(checkPhim && idLoaiND == 1 || idLoaiND == 2 || idLoaiND == 3){
                        Log.d("Check",String.valueOf(checkPhim));
                        Log.d("Check loại người dùng vip",String.valueOf(idLoaiND));
                        Toast.makeText(view.getContext(), "Bạn là Vip", Toast.LENGTH_SHORT).show();
                        String episodeCurrent = serverDataList.get(0).getName();
                        // Lưu lịch sử xem phim
                        lichSuPhim.luuLichSuXem(movieSlug,episodeCurrent,serverDataList);
                        // Khởi động activity phát video
                        Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                        intent.putExtra("movie_link", movieLink);  // Truyền link phim
                        intent.putExtra("slug", movieSlug);
                        intent.putExtra("episodeCurrent", episodeCurrent);
                        view.getContext().startActivity(intent);
                    }else{
                        if (checkPhim  && idLoaiND == 0){
                            Toast.makeText(view.getContext(), "Bạn cần mua vip để xem phim này", Toast.LENGTH_SHORT).show();

                        }else{
                            if(!checkPhim && idLoaiND == 1 || idLoaiND == 2 || idLoaiND == 3){
                                Log.d("Check",String.valueOf(checkPhim));
                                Log.d("Check loại người dùng vip",String.valueOf(idLoaiND));
                                Toast.makeText(view.getContext(), "Bạn là Vip", Toast.LENGTH_SHORT).show();
                                String episodeCurrent = serverDataList.get(0).getName();
                                // Lưu lịch sử xem phim
                                lichSuPhim.luuLichSuXem(movieSlug,episodeCurrent,serverDataList);
                                // Khởi động activity phát video
                                Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                                intent.putExtra("movie_link", movieLink);  // Truyền link phim
                                intent.putExtra("slug", movieSlug);
                                intent.putExtra("episodeCurrent", episodeCurrent);
                                view.getContext().startActivity(intent);
                            }else{
                                if (!checkPhim  && idLoaiND == 0){
                                    Log.d("Check",String.valueOf(checkPhim));
                                    Log.d("Check loại người dùng thường",String.valueOf(idLoaiND));
                                    Toast.makeText(view.getContext(), "Bạn là Thường", Toast.LENGTH_SHORT).show();
                                    String episodeCurrent = serverDataList.get(0).getName();
                                    // Lưu lịch sử xem phim
                                    lichSuPhim.luuLichSuXem(movieSlug,episodeCurrent,serverDataList);
                                    // Khởi động activity phát video
                                    Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                                    intent.putExtra("movie_link", movieLink);  // Truyền link phim
                                    intent.putExtra("slug", movieSlug);
                                    intent.putExtra("episodeCurrent", episodeCurrent);
                                    view.getContext().startActivity(intent);
                                }
                            }
                        }
                    }

                } else {
                    Toast.makeText(view.getContext(), "Link phim không khả dụng", Toast.LENGTH_SHORT).show();
                }
            }
        });
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            hienThiChiTietPhim(movieSlug);
            tinhTrungBinhDanhGia(movieSlug);
        });
        lichSuPhim = new LichSuPhim(this);
        ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
        // Tính và hiển thị trung bình sao và số lượt đánh giá
        tinhTrungBinhDanhGia(movieSlug);
    }
    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);
    }
    public void tinhTrungBinhDanhGia(String movieSlug) {
        ratingsRef.child(movieSlug).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalRatings = 0;
                float sumRatings = 0;

                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    float rating = ratingSnapshot.child("rating").getValue(Float.class);
                    sumRatings += rating;
                    totalRatings++;
                }

                if (totalRatings > 0) {
                    float averageRating = sumRatings / totalRatings;
                    // Cập nhật giao diện với tổng số đánh giá và trung bình sao
                    binding.tvAverageRating.setText("( " + averageRating + " / " + totalRatings + " lượt)");
                    binding.ratingBar.setRating(averageRating);
                    binding.ratingBar.setIsIndicator(true);
                } else {
                    binding.tvAverageRating.setText("( 0 / 0 lượt )");
                    binding.ratingBar.setRating(0); // Reset ratingBar nếu không có đánh giá
                    binding.ratingBar.setIsIndicator(true);
                }
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }
    private void hienThiChiTietPhim(String slug) {
        binding.progressBar.setVisibility(View.VISIBLE);
        // Đảm bảo apiService được khởi tạo trước khi gọi hàm này
        Call<ChiTietPhim> call = apiService.getChiTietPhim(slug);
        call.enqueue(new Callback<ChiTietPhim>() {
            @Override
            public void onResponse(Call<ChiTietPhim> call, Response<ChiTietPhim> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy chi tiết phim từ phản hồi
                    ChiTietPhim.MovieItem movie = response.body().getMovie();  // Giả sử phản hồi trả về đối tượng Movie
                    checkPhim = movie.isChieurap();
                    Log.d("check Phim",String.valueOf(checkPhim));
                    // Hiển thị thông tin phim bằng ViewBinding
                    binding.textViewTitle.setText(movie.getName());
                    // Giới hạn số ký tự cho mô tả
                    final int MAX_CHAR_COUNT = 200;
                    String fullDescription = movie.getContent();
                    if (fullDescription.length() > MAX_CHAR_COUNT) {
                        String shortDescription = fullDescription.substring(0, MAX_CHAR_COUNT) + "...";
                        binding.textViewDescription.setText(shortDescription);
                        binding.buttonReadMore.setVisibility(View.VISIBLE);
                    } else {
                        binding.textViewDescription.setText(fullDescription);
                        binding.buttonReadMore.setVisibility(View.GONE);
                    }

                    // Sự kiện nhấn nút "Xem thêm" để hiển thị toàn bộ mô tả
                    binding.buttonReadMore.setOnClickListener(new View.OnClickListener() {
                        boolean isExpanded = false; // Biến trạng thái để kiểm tra xem có đang mở rộng hay không

                        @Override
                        public void onClick(View v) {
                            if (!isExpanded) {
                                binding.textViewDescription.setText(fullDescription);
                                binding.buttonReadMore.setText("Thu gọn");
                                isExpanded = true;
                            } else {
                                String shortDescription = fullDescription.substring(0, MAX_CHAR_COUNT) + "...";
                                binding.textViewDescription.setText(shortDescription);
                                binding.buttonReadMore.setText("Xem thêm");
                                isExpanded = false;
                            }
                        }
                    });
                    binding.textViewYear.setText(String.valueOf(movie.getYear()));
                    binding.textViewActors.setText(TextUtils.join(", ", movie.getActor()));
                    binding.textViewDirector.setText(TextUtils.join(", ", movie.getDirector()));
                    List<String> directores = movie.getDirector();
                    String directory = "";

                    // Kiểm tra nếu danh sách không null và không rỗng
                    if (directores != null && !directores.isEmpty()) {
                        // Sử dụng TextUtils để nối các chuỗi
                        directory = TextUtils.join(", ", directores);
                    }

                    // Gán giá trị vào TextView
                    binding.tvCountry.setText(directory);

                    List<ChiTietPhim.MovieItem.Country> countries = movie.getCountry();
                    if (countries != null && !countries.isEmpty()) {
                        // Duyệt qua danh sách quốc gia và ghép tên của chúng thành chuỗi
                        List<String> countryNames = new ArrayList<>();
                        for (ChiTietPhim.MovieItem.Country country : countries) {
                            countryNames.add(country.getName());
                        }
                        // Chuyển danh sách tên quốc gia thành chuỗi, ngăn cách bởi dấu phẩy
                        String countryText = TextUtils.join(", ", countryNames);
                        // Hiển thị chuỗi quốc gia lên TextView
                        binding.tvCountry.setText(countryText);
                    }

                    List<ChiTietPhim.MovieItem.Category> categories = movie.getCategory();
                    if (categories != null && !countries.isEmpty()) {
                        // Duyệt qua danh sách thể loại và ghép tên của chúng thành chuỗi
                        List<String> categoryNames = new ArrayList<>();
                        for (ChiTietPhim.MovieItem.Category category : categories) {
                            categoryNames.add(category.getName());
                        }
                        // Chuyển danh sách tên thể loại thành chuỗi, ngăn cách bởi dấu phẩy
                        String categoryText = TextUtils.join(", ", categoryNames);
                        // Hiển thị chuỗi thể loại lên TextView
                        binding.categoryName.setText(categoryText);
                    }

                    // Tải poster bằng Glide (ảnh poster và thumbnail)
                    Glide.with(ChiTietPhimActivity.this)
                            .load(movie.getThumbUrl())
                            .into(binding.imageViewthumburl);  // Sử dụng đúng ID binding

                    Glide.with(ChiTietPhimActivity.this)
                            .load(movie.getPosterUrl())
                            .into(binding.imageViewPoster);  // Sử dụng đúng ID binding

                    // Lấy danh sách các tập phim
                    List<ChiTietPhim.TapPhim> tapPhim = response.body().getEpisodes();
                    if (tapPhim != null  && !tapPhim.isEmpty()) {
                        // Lưu danh sách các tập phim
                        serverDataList.clear(); // Xóa danh sách cũ
                        for (ChiTietPhim.TapPhim episode : tapPhim) {
                            List<ChiTietPhim.TapPhim.DuLieuServer> data = episode.getServerData();
                            if (data != null) {
                                serverDataList.addAll(data); // Thêm tất cả các tập phim vào danh sách
                            }
                        }
                        tapPhimAdapter = new TapPhimAdapter(ChiTietPhimActivity.this, serverDataList);
                        // Cập nhật RecyclerView với danh sách tập phim
                        tapPhimAdapter.setRecyclerViewItemClickListener(new TapPhimAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                // Lấy thông tin chi tiết phim từ slug truyền đến màn hình chi tiết phim
                                Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                                ChiTietPhim.TapPhim.DuLieuServer tapphim = serverDataList.get(position);
                                intent.putExtra("movie_link", tapphim.getLinkM3u8());
                                intent.putExtra("slug", tapphim.getSlug());
                                view.getContext().startActivity(intent);
                            }
                        });

                        // Lấy link của tập đầu tiên
                        ChiTietPhim.TapPhim.DuLieuServer firstServerData = serverDataList.get(0);
                        if (firstServerData != null) {
                            movieLink = firstServerData.getLinkM3u8();
                            Log.d("MovieDetailActivity", "Link phim tập 1: " + movieLink);
                        }

                        tapPhimAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        binding.progressBar.setVisibility(View.GONE);
                        binding.scvChitiet.setVisibility(View.VISIBLE);
                        binding.imageViewthumburl.requestLayout();
                    } else {
                        Toast.makeText(ChiTietPhimActivity.this, "Không có tập phim nào", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                    }
                } else {
                    Toast.makeText(ChiTietPhimActivity.this, "Tải chi tiết phim thất bại", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            }

            @Override
            public void onFailure(Call<ChiTietPhim> call, Throwable t) {
                Toast.makeText(ChiTietPhimActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}