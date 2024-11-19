package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flicks_now.databinding.ActivityChiTietPhimFirebaseBinding;
import com.example.flicks_now.model.LichSuPhim;
import com.example.flicks_now.model.QLPhim;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChiTietPhimFirebaseActivity extends AppCompatActivity {

    private ActivityChiTietPhimFirebaseBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference movieRef;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private int checkPhim = 0;
    private String movieLink;
    private List<QLPhim> serverDataList = new ArrayList<>();
    private LichSuPhim lichSuPhim;
    private String movieSlug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the binding
        binding = ActivityChiTietPhimFirebaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        laythongtinUser();
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();

        // Get movie slug from intent
        String movieSlug = getIntent().getStringExtra("slug");

        // Get reference to the movie in the Movies node using movieSlug as the key
        movieRef = database.getReference("Movies").child(movieSlug);

        // Fetch movie details from Firebase Realtime Database

        fetchMovieDetails();

    }

    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);
    }
    private void fetchMovieDetails() {


        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if movie data exists
                if (dataSnapshot.exists()) {
                    QLPhim movie = dataSnapshot.getValue(QLPhim.class);

                    if (movie != null) {
                        // Bind the movie data to the views using ViewBinding
                        binding.textViewTitle.setText(movie.getName());
                        binding.textViewYear.setText(movie.getYear());
                        binding.tvTime.setText(movie.getTime() + " phút");
                        binding.tvCountry.setText(movie.getQuocGia());
                        binding.textViewDirector.setText(movie.getTacGia());
                        binding.categoryName.setText(movie.getTheLoai());
                        binding.textViewActors.setText(movie.getDienVien());

                        //check goi phim
                        checkPhim = Integer.parseInt(movie.getGoi());

                        Picasso.get().load(movie.getPoster_url()).into(binding.imageViewPoster);
                        Picasso.get().load(movie.getThumb_url()).into(binding.imageViewthumburl);

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

                        binding.buttonReadMore.setOnClickListener(new View.OnClickListener() {
                            boolean isExpanded = false;

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
                        movieLink = movie.getMovie_url();
                        binding.ratingBar.setRating(movie.getRating());
                        binding.tvAverageRating.setText(String.format("( 0 / 0 lượt )", movie.getRating()));


                        binding.btnXemPhim.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (movieLink != null) {
                                    Log.d("Check",String.valueOf(checkPhim));
                                    Log.d("Check loại người dùng",String.valueOf(idLoaiND));
                                    if (idLoaiND == 0){
                                        if (checkPhim == 0){
                                            Log.d("Check",String.valueOf(checkPhim));
                                            Log.d("Check loại người dùng thường",String.valueOf(idLoaiND));
                                            Toast.makeText(view.getContext(), "Bạn là Thường", Toast.LENGTH_SHORT).show();
                                            String episodeCurrent = movie.getName();
                                            // Lưu lịch sử xem phim
                                            // Khởi động activity phát video
                                            Intent intent = new Intent(view.getContext(), XemPhimFirebaseActivity.class);
                                            intent.putExtra("movie_link", movieLink);  // Truyền link phim
                                            intent.putExtra("episodeCurrent", episodeCurrent);
                                            view.getContext().startActivity(intent);
                                        }else {
                                            Toast.makeText(view.getContext(), "Bạn cần mua vip để xem phim này", Toast.LENGTH_SHORT).show();
                                        }
                                    }else {
                                        Log.d("Check",String.valueOf(checkPhim));
                                        Log.d("Check loại người dùng vip",String.valueOf(idLoaiND));
                                        Toast.makeText(view.getContext(), "Bạn là Vip", Toast.LENGTH_SHORT).show();
                                        String episodeCurrent = movie.getName();
                                        // Lưu lịch sử xem phim

                                        // Khởi động activity phát video
                                        Intent intent = new Intent(view.getContext(), XemPhimFirebaseActivity.class);
                                        intent.putExtra("movie_link", movieLink);  // Truyền link phim
                                        intent.putExtra("episodeCurrent", episodeCurrent);
                                        view.getContext().startActivity(intent);
                                    }

                                } else {
                                    Toast.makeText(view.getContext(), "Link phim không khả dụng", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                } else {
                    Toast.makeText(ChiTietPhimFirebaseActivity.this, "Movie not found", Toast.LENGTH_SHORT).show();
                }

                // Đảm bảo setRefreshing(false) được gọi sau khi hoàn thành việc tải dữ liệu
                //swipeRefreshLayout.setRefreshing(false);
                //binding.progressBar.setVisibility(View.GONE); // Ẩn progress bar sau khi hoàn thành
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible database read failure
                Toast.makeText(ChiTietPhimFirebaseActivity.this, "Failed to load movie details", Toast.LENGTH_SHORT).show();
                // Đảm bảo setRefreshing(false) được gọi sau khi có lỗi
               // swipeRefreshLayout.setRefreshing(false);
                //binding.progressBar.setVisibility(View.GONE); // Ẩn progress bar sau khi có lỗi
            }
        });
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