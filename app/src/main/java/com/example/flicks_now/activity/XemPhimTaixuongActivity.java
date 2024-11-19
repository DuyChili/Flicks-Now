package com.example.flicks_now.activity;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.example.flicks_now.databinding.ActivityPlayDownloadedMovieBinding;
import com.example.flicks_now.R;

import java.io.File;

@OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
public class XemPhimTaixuongActivity extends AppCompatActivity {
    private ActivityPlayDownloadedMovieBinding binding;
    private ExoPlayer player;
    private boolean isFullScreen = false;
    private LinearLayout.LayoutParams originalPlayerViewParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayDownloadedMovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy tên phim từ Intent
        String movieName = getIntent().getStringExtra("movie_name");

        if (movieName == null) {
            Log.e("PlayDownloadedMovieActivity", "Tên phim bị null");
            Toast.makeText(this, "Không tìm thấy tên phim!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có tên phim
            return;
        }

        Log.d("PlayDownloadedMovieActivity", "Tên phim: " + movieName);

        // Lấy thư mục phim dựa trên tên phim
        File movieDir = getMovieFile(movieName);

        // Kiểm tra nếu thư mục tồn tại và có chứa file playlist .m3u8
        File m3u8File = new File(movieDir, "playlist.m3u8");
        if (m3u8File.exists()) {
            Log.d("PlayDownloadedMovieActivity", "Đang phát từ file playlist: " + m3u8File.getAbsolutePath());

            // Cấu hình ExoPlayer để phát HLS
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            player = new ExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

            // Liên kết player với PlayerView
            binding.playerView.setPlayer(player);

            // Sử dụng HlsMediaSource để phát tệp HLS
            Uri hlsUri = Uri.fromFile(m3u8File);
            HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(
                    new DefaultDataSource.Factory(this))
                    .createMediaSource(MediaItem.fromUri(hlsUri));

            // Chuẩn bị và phát
            player.setMediaSource(hlsMediaSource);
            player.prepare();
            player.play();
        } else {
            Log.e("PlayDownloadedMovieActivity", "Không tìm thấy tệp playlist .m3u8");
            Toast.makeText(this, "Không tìm thấy tệp playlist để phát phim!", Toast.LENGTH_SHORT).show();
            finish();
        }
        binding.playerView.findViewById(R.id.btnFullScreen).setOnClickListener(v -> phongToPhim());
        // Lưu LayoutParams ban đầu
        originalPlayerViewParams = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();
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

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    // Hàm lấy thư mục chứa phim dựa trên tên phim
    public File getMovieFile(String movieName) {
        File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies/" + movieName);

        if (!movieDir.exists()) {
            boolean created = movieDir.mkdirs();
            if (!created) {
                Log.e("PlayDownloadedMovieActivity", "Không thể tạo thư mục lưu phim");
            }
        }
        return movieDir;
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