package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.adapter.BinhLuanPhimAdapter;
import com.example.flicks_now.adapter.TapPhimAdapter;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.api.ApiService;
import com.example.flicks_now.model.BinhLuanPhim;
import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.model.DSPhimYeuThich;
import com.example.flicks_now.model.DanhGiaPhim;
import com.example.flicks_now.model.LichSuPhim;
import com.example.flicks_now.model.TaiPhim;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityXemPhimBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class XemPhimActivity extends AppCompatActivity implements BinhLuanPhimAdapter.OnCommentDeleteListener {
    private ActivityXemPhimBinding binding; // Khai báo View Binding
    private ExoPlayer exoPlayer; // ExoPlayer để phát video
    private boolean isFullScreen = false;
    private TapPhimAdapter tapPhimAdapter;
    private List<ChiTietPhim.TapPhim.DuLieuServer> serverDataList = new ArrayList<>();
    private String movieLink;
    private ApiService apiService;
    private String movieSlug;
    private DatabaseReference favoritesRef;
    private String idUser;
    private String nameUser;
    private String emailUser;
    private int idLoaiND;
    private DatabaseReference usersRef;
    private String currentUserId;
    private LichSuPhim lichSuPhim;
    private BinhLuanPhimAdapter binhLuanPhimAdapter;
    private List<BinhLuanPhim> binhLuanPhimList = new ArrayList<>();
    private DSPhimYeuThich dsPhimYeuThich;
    private DanhGiaPhim danhGiaPhim;
    private LinearLayout.LayoutParams originalPlayerViewParams;
    private TaiPhim taiPhim;
    private DatabaseReference commentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemPhimBinding.inflate(getLayoutInflater()); // Khởi tạo View Binding
        setContentView(binding.getRoot()); // Đặt layout cho Activity
        apiService = ApiClient.getClient().create(ApiService.class);
        taiPhim = new TaiPhim(apiService, this);
        setControl();
        setEvent();

        // Lưu LayoutParams ban đầu
        originalPlayerViewParams = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();

        PlayerView.ControllerVisibilityListener visibilityListener = visibility -> {
            View btnFullScreen = binding.playerView.findViewById(R.id.btnFullScreen);

            if (visibility == View.VISIBLE) {
                btnFullScreen.setVisibility(View.VISIBLE);
            } else {
                btnFullScreen.setVisibility(View.GONE);
            }
        };


// Áp dụng listener vào PlayerView
        // Áp dụng listener vào PlayerView
        binding.playerView.setControllerVisibilityListener(visibilityListener);

    }

    public void setControl() {
        binding.rcvTapPhim.setLayoutManager(new GridLayoutManager(this, 2, RecyclerView.HORIZONTAL, false)); // Thiết lập RecyclerView
        // Khởi tạo Firebase Database
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites"); // Thay "favorites" bằng tên bảng của bạn
        binhLuanPhimAdapter = new BinhLuanPhimAdapter(this, binhLuanPhimList, this);
        binding.rvComments.setLayoutManager(new GridLayoutManager(this, 1));
        binding.rvComments.setAdapter(binhLuanPhimAdapter);
    }

    public void setEvent() {
        KhoiTaoPhim();
        // Thiết lập sự kiện cho nút toàn màn hình
        movieSlug = getIntent().getStringExtra("slug");

        binding.playerView.findViewById(R.id.btnFullScreen).setOnClickListener(v -> phongToPhim());
        apiService = ApiClient.getClient().create(ApiService.class);
        hienThiChiTietPhim();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        laythongtinUser();
        // Gọi hàm này sau khi người dùng nhấn vào phim hoặc sau khi thêm bình luận
        taiBinhLuan(this.movieSlug);
        // Giả sử bạn đã đăng nhập và lấy ID người dùng từ Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid(); // Lấy ID người dùng
        }
        lichSuPhim = new LichSuPhim(this);
        dsPhimYeuThich = new DSPhimYeuThich(this, binding, movieSlug);
        // Kiểm tra và cập nhật màu nút trái tim
        dsPhimYeuThich.kiemTraYeuThich();
        // Thêm sự kiện nhấn cho nút thêm vào danh sách yêu thích
        binding.btnAddToFavorites.setOnClickListener(v -> dsPhimYeuThich.themYeuThich());
        String movieSlug = this.movieSlug;
        // Xử lý khi người dùng đánh giá phim
        binding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                danhGiaPhim.luuDanhGia(movieSlug, idUser, rating);  // Lưu đánh giá
            }
        });
        // Khởi tạo đối tượng DanhGiaPhim
        danhGiaPhim = new DanhGiaPhim(this,binding,movieSlug);
        // Tính và hiển thị trung bình sao và số lượt đánh giá
        danhGiaPhim.tinhTrungBinhDanhGia();
        // Kiểm tra xem người dùng đã đánh giá phim hay chưa
        danhGiaPhim.kiemTraDanhGia();
        commentsRef = FirebaseDatabase.getInstance().getReference("Comments");
        // Thêm sự kiện nhấn cho nút bình luận
        binding.commentInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Kiểm tra xem người dùng có nhấn vào biểu tượng gửi (drawableEnd) không
                if (event.getRawX() >= (binding.commentInput.getRight() - binding.commentInput.getCompoundDrawables()[2].getBounds().width())) {
                    String comment = binding.commentInput.getText().toString();
                    if (!comment.isEmpty()) {
                        themBinhLuan(comment);// Gọi hàm thêm bình luận
                    }
                    return true; // Trả về true để xử lý sự kiện
                }
            }
            return false;
        });
        binding.btnDowload.setOnClickListener(v -> {
            String movieName = binding.tvMovieTitle.getText().toString();
            if (movieLink != null && !movieLink.isEmpty()) {
                // Hiện thông báo "Đang tải..."
                Toast.makeText(XemPhimActivity.this, "Đang tải...", Toast.LENGTH_SHORT).show();
                // Gọi phương thức download từ movieDownloader
                taiPhim.loadPosterAndDownloadMovie(movieSlug, movieLink, movieName);
            } else {
                Toast.makeText(XemPhimActivity.this, "Liên kết phim không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void KhoiTaoPhim() {
        movieLink = getIntent().getStringExtra("movie_link"); // Đã sửa để lấy lại movieLink
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            PlayerView playerView = binding.playerView; // Đảm bảo đây là PlayerView từ Media3
            playerView.setPlayer(exoPlayer); // Thiết lập player cho PlayerView
            playerView.setKeepScreenOn(true);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Toast.makeText(XemPhimActivity.this, "Phim lỗi vui lòng báo cáo cho admin hoặc xem phim khác: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Tạo MediaItem từ đường dẫn video
            MediaItem mediaItem = MediaItem.fromUri(movieLink);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
        }
    }
    // Hàm phát video
    private void phatPhim(String episodeLink) {
        if (exoPlayer != null) {
            MediaItem mediaItem = MediaItem.fromUri(episodeLink);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }
    // Hàm phong to
    private void phongToPhim() {
        if (isFullScreen) {
            // Quay về chế độ portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Hiện thanh trạng thái và thanh điều hướng
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            // Thiết lập chiều cao của PlayerView về 250dp
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
            binding.playerView.setLayoutParams(params);
            // Thoát fullscreen và khôi phục LayoutParams ban đầu
            binding.playerView.setLayoutParams(originalPlayerViewParams);
        } else {
            // Chuyển sang chế độ landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // Ẩn thanh trạng thái và thanh điều hướng
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            // Thiết lập chiều cao của PlayerView để chiếm toàn bộ chiều cao màn hình
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.playerView.setLayoutParams(params);
        }

        isFullScreen = !isFullScreen; // Đổi trạng thái fullscreen
    }


    private void hienThiChiTietPhim() {
        Call<ChiTietPhim> call = apiService.getChiTietPhim(movieSlug);
        call.enqueue(new Callback<ChiTietPhim>() {
            @Override
            public void onResponse(Call<ChiTietPhim> call, Response<ChiTietPhim> response) {
                // Xử lý kết quả trả về từ API
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy thông tin từ phản hồi
                    ChiTietPhim chiTietPhim = response.body();
                    List<ChiTietPhim.TapPhim> tapPhim = chiTietPhim.getEpisodes();
                    String tenPhim = chiTietPhim.getMovie().getName();
                    String tenTapPhim;

                    // Lấy tên tập phim từ Intent
                    if (getIntent().getBooleanExtra("lichsu", false)) {
                        tenTapPhim = getIntent().getStringExtra("episode");
                    } else {
                        tenTapPhim = getIntent().getStringExtra("episodeCurrent");
                    }

                    // Đặt tiêu đề phim và tên tập phim
                    binding.tvMovieTitle.setText(tenPhim + " - " + tenTapPhim);

                    // Thiết lập RecyclerView cho các tập phim
                    if (tapPhim != null && !tapPhim.isEmpty()) {
                        serverDataList.clear();

                        // Lấy danh sách các tập phim
                        for (ChiTietPhim.TapPhim episode : tapPhim) {
                            // Lấy danh sách các server
                            List<ChiTietPhim.TapPhim.DuLieuServer> data = episode.getServerData();
                            if (data != null) {
                                serverDataList.addAll(data);
                            }
                        }

                        tapPhimAdapter = new TapPhimAdapter(XemPhimActivity.this, serverDataList);

                        // Thiết lập click listener cho các tập phim
                        tapPhimAdapter.setRecyclerViewItemClickListener(new TapPhimAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                ChiTietPhim.TapPhim.DuLieuServer tapPhimDaChon = serverDataList.get(position);
                                String linkPhimMoi = tapPhimDaChon.getLinkM3u8();

                                // Hiển thị tên tập phim đang xem
                                binding.tvMovieTitle.setText(tenPhim + " - " + tapPhimDaChon.getName());
                                lichSuPhim.luuLichSuXem(movieSlug, tapPhimDaChon.getName(), serverDataList);
                                phatPhim(linkPhimMoi);
                            }
                        });

                        binding.rcvTapPhim.setAdapter(tapPhimAdapter);

                        // Tự động phát tập phim đầu tiên
                        movieLink = serverDataList.get(0).getLinkM3u8();
                        KhoiTaoPhim(); // Khởi tạo trình phát với tập đầu tiên
                    } else {
                        Toast.makeText(XemPhimActivity.this, "Không có tập phim nào", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChiTietPhim> call, Throwable t) {
                Toast.makeText(XemPhimActivity.this, "Không thể tải chi tiết phim", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void laythongtinUser() {
        // Lấy thông tin người dùng từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

    }
    private void themBinhLuan(String comment) {
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim

        // Kiểm tra nếu người dùng đã đăng nhập
        if (userId == null) {
            // Nếu chưa đăng nhập, hiển thị thông báo yêu cầu đăng nhập
            new AlertDialog.Builder(this)
                    .setTitle("Cần đăng nhập")
                    .setMessage("Bạn cần đăng nhập để bình luận. Bạn có muốn đăng nhập ngay?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Chuyển đến màn hình đăng nhập
                        Intent intent = new Intent(XemPhimActivity.this, DangNhapActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Không", null)
                    .show();
            return; // Kết thúc hàm nếu chưa đăng nhập
        }

        // Nếu đã đăng nhập, tiếp tục xử lý bình luận
        // Lấy tên người dùng
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String name = nameUser; // Lấy tên người dùng từ Firestore
                if (name == null) {
                    name = "Người dùng ẩn danh"; // Hoặc một tên mặc định
                }

                // Định dạng ngày giờ
                String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                // Tạo một đối tượng Comment
                BinhLuanPhim newComment = new BinhLuanPhim(userId, movieSlug, comment, System.currentTimeMillis(), name, formattedDate);

                // Tham chiếu đến bảng Comments
                commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

                // Lưu bình luận vào Firebase
                commentsRef.push().setValue(newComment)
                        .addOnSuccessListener(aVoid -> {
                            // Thêm bình luận vào adapter và cập nhật UI ngay lập tức
                            binhLuanPhimList.add(0, newComment);
                            binhLuanPhimAdapter.notifyItemInserted(0);
                            binhLuanPhimAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView

                            Toast.makeText(XemPhimActivity.this, "Bình luận đã được lưu!", Toast.LENGTH_SHORT).show();
                            // Xóa nội dung bình luận trong EditText nếu cần
                            binding.commentInput.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(XemPhimActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void xoaBinhLuan(int position) {
        // Lấy thông tin người dùng và phim
        String userId = binhLuanPhimAdapter.getCommentUserId(position);
        String movieSlug = this.movieSlug; // Slug của phim
        // Lấy nội dung bình luận để hiển thị trong hộp thoại xác nhận
        String commentText = binhLuanPhimAdapter.getCommentText(position); // Giả sử bạn đã có phương thức này trong adapter

        // Kiểm tra người dùng có quyền xóa bình luận
        if (userId.equals(idUser)) {
            // Tạo hộp thoại xác nhận
            new AlertDialog.Builder(XemPhimActivity.this)
                    .setTitle("Xóa bình luận")
                    .setMessage("Bạn có chắc chắn muốn xóa bình luận " + commentText + " không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

                        // Xác định bình luận cần xóa
                        commentsRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String commentUserId = snapshot.child("userId").getValue(String.class);
                                    if (commentUserId != null && commentUserId.equals(userId)) {
                                        // Xóa bình luận khỏi Firebase
                                        snapshot.getRef().removeValue()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(XemPhimActivity.this, "Bình luận đã được xóa!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(XemPhimActivity.this, "Lỗi khi xóa bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                        break; // Đã xóa bình luận, không cần tiếp tục tìm kiếm
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(XemPhimActivity.this, "Lỗi khi kiểm tra bình luận", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        dialog.dismiss(); // Đóng hộp thoại nếu người dùng không muốn xóa
                    })
                    .show(); // Hiển thị hộp thoại
        } else {
            Toast.makeText(XemPhimActivity.this, "Bạn không có quyền xóa bình luận này!", Toast.LENGTH_SHORT).show();
        }
    }

    private void taiBinhLuan(String movieSlug) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

        // Dọn dẹp danh sách bình luận trước khi thêm mới
        binhLuanPhimList.clear();

        // Sử dụng addValueEventListener để lắng nghe thay đổi
        commentsRef.orderByChild("slug").equalTo(movieSlug).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binhLuanPhimList.clear(); // Dọn dẹp danh sách mỗi lần dữ liệu thay đổi
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.child("userId").getValue(String.class);
                    String commentText = snapshot.child("commentText").getValue(String.class);
                    String userName = snapshot.child("userName").getValue(String.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    if (userId != null && commentText != null) {
                        // Định dạng ngày giờ
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        String formattedDate = sdf.format(new Date(timestamp));

                        // Tạo bình luận và thêm vào adapter
                        BinhLuanPhim comment = new BinhLuanPhim(userId, movieSlug, commentText, timestamp, userName, formattedDate);
                        binhLuanPhimList.add(0,comment);
                        binhLuanPhimAdapter.notifyItemInserted(0);
                    }
                }
                binhLuanPhimAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi khi tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();  // Giải phóng ExoPlayer khi Activity bị hủy
            exoPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause(); // Tạm dừng video khi Activity không còn hiển thị
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


}
