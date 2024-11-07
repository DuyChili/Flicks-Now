package com.example.flicks_now.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.flicks_now.api.ApiService;
import com.example.flicks_now.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaiPhim {
    private static final int MAX_RETRY_COUNT = 3;
    private static final int MAX_RECURSIVE_DEPTH = 5;
    private int recursiveDepth = 0;
    private OkHttpClient okHttpClient;
    private ApiService apiService;
    private Context context;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static final int NOTIFICATION_ID = 1;
    private String movieName;


    public TaiPhim(ApiService apiService, Context context) {
        this.apiService = apiService;
        this.context = context;
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Tăng thời gian connect timeout
                .readTimeout(60, TimeUnit.SECONDS)    // Tăng thời gian read timeout
                .writeTimeout(60, TimeUnit.SECONDS)   // Tăng thời gian write timeout
                .retryOnConnectionFailure(true)       // Tự động retry khi lỗi kết nối
                .build();
        createNotificationChannel();
        // Khởi tạo NotificationManager và NotificationBuilder
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "download_channel";
            String channelName = "Download Notifications";
            String channelDescription = "Notifications for download progress";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            // Khởi tạo NotificationManager trước khi gọi createNotificationChannel
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.e("MovieDownloader", "NotificationManager is null");
            }
        }
    }
    public void loadPosterAndDownloadMovie(String movieSlug, String movieLink, String movieName) {
        this.movieName = movieName;
        Call<ChiTietPhim> call = apiService.getChiTietPhim(movieSlug);
        call.enqueue(new Callback<ChiTietPhim>() {
            @Override
            public void onResponse(Call<ChiTietPhim> call, Response<ChiTietPhim> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChiTietPhim movieDetails = response.body();
                    String posterUrl = movieDetails.getMovie().getPosterUrl();
                    Log.d("Poster URL", "Poster link: " + posterUrl);
                    // Tải poster
                    downloadPoster(posterUrl, movieName, () -> {
                        // Sau khi tải poster xong, tiến hành tải phim
                        downloadMovie(movieLink, movieName);
                    });
                }
            }

            @Override
            public void onFailure(Call<ChiTietPhim> call, Throwable t) {
                Toast.makeText(context, "Failed to load movie details", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void downloadPoster(String posterUrl, String movieName, Runnable onSuccess) {
        Call<ResponseBody> call = apiService.downloadMovie(posterUrl); // Sử dụng API để tải poster
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        File movieDir = getMovieFile(movieName); // Lấy thư mục phim
                        File posterFile = new File(movieDir, movieName + "_poster.jpg"); // Đặt tên file poster
                        try (InputStream inputStream = response.body().byteStream();
                             FileOutputStream outputStream = new FileOutputStream(posterFile)) {

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            Log.d("DownloadPoster", "Poster đã được lưu tại: " + posterFile.getAbsolutePath());
                            onSuccess.run(); // Gọi hàm để tiếp tục tải phim sau khi poster tải xong
                        }
                    } catch (IOException e) {
                        Log.e("DownloadPoster", "Lỗi khi lưu poster: " + e.getMessage());
                    }
                } else {
                    Log.e("DownloadPoster", "Tải poster thất bại: " + posterUrl);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("DownloadPoster", "Lỗi khi tải poster: " + t.getMessage());
            }
        });
    }
    private void createNotificationBuilder() {
        notificationBuilder = new NotificationCompat.Builder(context, "download_channel")
                .setContentTitle("Đang Tải: " + movieName) // Sử dụng biến instance
                .setSmallIcon(R.drawable.ic_download) // Bạn cần có icon này trong drawable
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true); // Thông báo sẽ không thể bị xóa
    }
    private void downloadMovie(String m3u8Link, String movieName) {
        createNotificationBuilder();
        if (recursiveDepth > MAX_RECURSIVE_DEPTH) { // Giới hạn đệ quy
            Toast.makeText(context, "Quá nhiều tệp m3u8 con, tải không thành công!", Toast.LENGTH_LONG).show();
            return;
        }

        recursiveDepth++; // Tăng độ sâu mỗi khi đệ quy
        Call<ResponseBody> call = apiService.downloadMovie(m3u8Link);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(context, "Tải file m3u8 không thành công!", Toast.LENGTH_LONG).show();
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    List<String> tsLinks = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        if (line.endsWith(".m3u8")) {
                            // Nếu là m3u8 con, tải đệ quy
                            String subM3U8Link = line.startsWith("http") ? line : m3u8Link.substring(0, m3u8Link.lastIndexOf("/") + 1) + line;
                            downloadMovie(subM3U8Link, movieName); // Gọi đệ quy để tải tệp con
                            return;
                        }
                        if (line.endsWith(".ts")) {
                            tsLinks.add(line); // Thêm link .ts vào danh sách
                        }
                    }

                    if (tsLinks.isEmpty()) {
                        Toast.makeText(context, "Không tìm thấy link .ts trong file m3u8!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Tải các file .ts tuần tự
                    downloadAllTsFilesSequentially(tsLinks, m3u8Link, movieName);

                } catch (IOException e) {
                    Toast.makeText(context, "Lỗi khi phân tích file m3u8: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi khi tải file m3u8: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public File getMovieFile(String movieName) {
        // Lấy thư mục Movies riêng của ứng dụng
        File movieDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies/" + movieName);

        if (!movieDir.exists()) {
            boolean created = movieDir.mkdirs();
            if (!created) {
                Log.e("XemPhimActivity", "Không thể tạo thư mục lưu phim");
            }
        }
        return movieDir;
    }

    private void downloadAllTsFilesSequentially(List<String> tsLinks, String m3u8Link, String movieName) {
        List<File> tsFiles = new ArrayList<>();
        int totalTsFiles = tsLinks.size(); // Tổng số file TS
        notificationBuilder.setContentText("0/" + totalTsFiles); // Cập nhật thông báo

        // Bắt đầu quá trình tải các file .ts với lần thử đầu tiên
        downloadTsFile(tsLinks, m3u8Link, movieName, 0, tsFiles, 0, totalTsFiles);
    }

    private void downloadTsFile(List<String> tsLinks, String m3u8Link, String movieName, int index, List<File> tsFiles, int retryCount, int totalTsFiles) {
        if (index >= tsLinks.size()) {
            createM3U8Playlist(getMovieFile(movieName), tsFiles); // Tạo tệp .m3u8 sau khi tải xong tất cả file .ts
            mergeTsFiles(tsFiles, movieName); // Ghép file khi tải xong tất cả file .ts
            notificationBuilder.setContentText("Tải hoàn tất!").setOngoing(false); // Cập nhật thông báo
            notificationManager.notify(1, notificationBuilder.build()); // Cập nhật thông báo
            return;
        }

        String tsLink = tsLinks.get(index);
        String tsFullLink = tsLink.startsWith("http") ? tsLink : m3u8Link.substring(0, m3u8Link.lastIndexOf("/") + 1) + tsLink;
        tsFullLink = tsFullLink.replace("hls//", "hls/");

        final String finalTsFullLink = tsFullLink;

        Call<ResponseBody> call = apiService.downloadMovie(finalTsFullLink);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("downloadTsFile", "Lỗi khi tải file .ts: " + finalTsFullLink);

                    if (retryCount < MAX_RETRY_COUNT) {
                        Log.d("downloadTsFile", "Đang thử lại lần thứ " + (retryCount + 1) + " cho file: " + finalTsFullLink);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            downloadTsFile(tsLinks, m3u8Link, movieName, index, tsFiles, retryCount + 1, totalTsFiles);
                        }, 2000);
                    } else {
                        Log.e("downloadTsFile", "Bỏ qua file sau " + MAX_RETRY_COUNT + " lần thử lại: " + finalTsFullLink);
                        downloadTsFile(tsLinks, m3u8Link, movieName, index + 1, tsFiles, 0, totalTsFiles); // Tiếp tục file tiếp theo
                    }
                    return;
                }

                File movieDir = getMovieFile(movieName);
                File tsFile = new File(movieDir, movieName + "_" + tsLink.substring(tsLink.lastIndexOf("/") + 1));

                if (!tsFiles.contains(tsFile)) {
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream outputStream = new FileOutputStream(tsFile)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        tsFiles.add(tsFile); // Thêm file đã tải vào danh sách

                        // Cập nhật thanh tiến trình
                        int progress = (index + 1) * 100 / totalTsFiles; // Tính toán tiến trình
                        notificationBuilder.setProgress(100, progress, false); // Cập nhật thanh tiến trình
                        notificationBuilder.setContentText((index + 1) + "/" + totalTsFiles); // Cập nhật thông báo
                        notificationManager.notify(1, notificationBuilder.build()); // Cập nhật thông báo

                    } catch (IOException e) {
                        Log.e("downloadTsFile", "Lỗi ghi file .ts: " + e.getMessage());
                    }
                } else {
                    Log.d("downloadTsFile", "File đã tồn tại, bỏ qua: " + tsFile.getName());
                }

                // Tải tiếp file tiếp theo
                downloadTsFile(tsLinks, m3u8Link, movieName, index + 1, tsFiles, 0, totalTsFiles); // Reset retryCount khi tải file mới
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("downloadTsFile", "Lỗi khi tải file .ts: " + t.getMessage());

                if (retryCount < MAX_RETRY_COUNT) {
                    Log.d("downloadTsFile", "Đang thử lại lần thứ " + (retryCount + 1) + " cho file: " + finalTsFullLink);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        downloadTsFile(tsLinks, m3u8Link, movieName, index, tsFiles, retryCount + 1, totalTsFiles);
                    }, 2000);
                } else {
                    Log.e("downloadTsFile", "Bỏ qua file sau " + MAX_RETRY_COUNT + " lần thử lại: " + finalTsFullLink);
                    downloadTsFile(tsLinks, m3u8Link, movieName, index + 1, tsFiles, 0, totalTsFiles); // Tiếp tục file tiếp theo
                }
            }
        });
    }

    private void createM3U8Playlist(File movieDir, List<File> tsFiles) {
        File m3u8File = new File(movieDir, "playlist.m3u8");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(m3u8File))) {
            writer.write("#EXTM3U\n");
            writer.write("#EXT-X-VERSION:3\n");
            writer.write("#EXT-X-TARGETDURATION:10\n");
            writer.write("#EXT-X-MEDIA-SEQUENCE:0\n");

            for (File tsFile : tsFiles) {
                // Gọi hàm để lấy thời gian thực tế cho từng file .ts
                double duration = getTsFileDuration(tsFile); // Thay thế bằng cách lấy thời gian thực tế

                writer.write("#EXTINF:" + duration + ",\n");
                writer.write(tsFile.getName() + "\n");
            }

            writer.write("#EXT-X-ENDLIST\n");
            writer.flush();
            Log.d("createM3U8Playlist", "Đã tạo file playlist.m3u8 tại: " + m3u8File.getAbsolutePath());
        } catch (IOException e) {
            Log.e("PlayDownloadedMovieActivity", "Lỗi khi tạo file playlist.m3u8", e);
        }
    }

    // Giả lập hàm để lấy độ dài file .ts (cần được điều chỉnh theo thực tế)
    private double getTsFileDuration(File tsFile) {
        // Giả sử mỗi file .ts có độ dài 2 giây, thay thế bằng cách thực tế để lấy thời gian
        return 3.0; // Thay thế bằng logic thực tế để xác định độ dài
    }

    private void mergeTsFiles(List<File> tsFiles, String movieName) {
        File mergedFile = getMovieFile(movieName); // Tạo file đích cho phim đã ghép

        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            for (File tsFile : tsFiles) {
                try (FileInputStream fis = new FileInputStream(tsFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead); // Ghi dữ liệu của file .ts vào file hợp nhất
                    }
                }
            }
            // Cập nhật Notification khi tải hoàn tất
            notificationBuilder.setContentText("Download complete!")
                    .setProgress(0, 0, false)
                    .setOngoing(false); // Cho phép người dùng xóa thông báo
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            Toast.makeText(context, "Đã ghép file thành công: " + mergedFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, "Lỗi khi ghép file .ts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

