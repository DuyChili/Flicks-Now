package com.example.flicks_now.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.adapter.DSPhimAdapter;
import com.example.flicks_now.adapter.DSPhimAdapterOphim;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.api.ApiService;
import com.example.flicks_now.databinding.ActivityXemThemPhimBinding;
import com.example.flicks_now.model.DSPhim;
import com.example.flicks_now.model.DSPhimAPiOphim;
import com.example.flicks_now.response.DSPhimResponse;
import com.example.flicks_now.response.DSResponseOphim;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XemThemPhim extends AppCompatActivity {
    private ActivityXemThemPhimBinding binding;
    private int currentPage = 1; // Trang hiện tại
    private String type;
    private List<DSPhim> DSPhimXemThem;
    private DSPhimAdapter dsPhimAdapter;
    private DSPhimAdapterOphim seriesAdapterOphim;
    private List<DSPhimAPiOphim> seriesOphimList;
    private boolean isLoading = false;
    private ApiService apiService;
    private String theloai;
    private String quocgia;
    private  boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemThemPhimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách Phim"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }

        // Nhận dữ liệu từ Intent và lưu vào một biến duy nhất
        type = getIntent().getStringExtra("type");
        theloai = getIntent().getStringExtra("theloai");
        quocgia = getIntent().getStringExtra("quocgia");
        Log.d("XemThemPhim", "Received type: " + theloai);
        Log.d("XemThemPhim", "Received type: " + quocgia);
        // Khởi tạo các danh sách và adapter
        DSPhimXemThem = new ArrayList<>();
        dsPhimAdapter = new DSPhimAdapter(this, DSPhimXemThem);
        seriesOphimList = new ArrayList<>();
        seriesAdapterOphim = new DSPhimAdapterOphim(this, seriesOphimList);
        binding.recyclerViewMovies.setLayoutManager(new GridLayoutManager(this, 3));

        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Tải dữ liệu ban đầu
                    dsPhimAdapter = new DSPhimAdapter(XemThemPhim.this, DSPhimXemThem); // Adapter cho các loại phim khác
                    binding.recyclerViewMovies.setAdapter(dsPhimAdapter); // Sử dụng dsPhimAdapter
                } else if ("Ophim".equals(name)) {
                    seriesAdapterOphim = new DSPhimAdapterOphim(XemThemPhim.this, seriesOphimList);
                    binding.recyclerViewMovies.setAdapter(seriesAdapterOphim);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(XemThemPhim.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, XemThemPhim.this); // Thêm XemThemPhim.this làm Context);

        // Tải dữ liệu ban đầu
        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    loadXemThemPhimKKPhim(currentPage, type, theloai, quocgia);
                } else if ("Ophim".equals(name)) {
                    loadXemThemPhimOPhim(currentPage, type, theloai, quocgia);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(XemThemPhim.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, XemThemPhim.this); // Thêm XemThemPhim.this làm Context);
        // Thêm listener cuộn để tải thêm dữ liệu khi đến cuối danh sách
        binding.recyclerViewMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null && !isLoading) {
                    // Số lượng mục hiện tại đang hiển thị trên màn hình.
                    int visibleItemCount = layoutManager.getChildCount();
                    //Tổng số lượng mục trong danh sách.
                    int totalItemCount = layoutManager.getItemCount();
                    //Vị trí của mục đầu tiên đang được hiển thị.
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
                            @Override
                            public void onBaseUrlFetched(String name, String url) {
                                currentPage++;
                                if ("Kkphim".equals(name)) {
                                    loadXemThemPhimKKPhim(currentPage, type, theloai, quocgia);
                                } else if ("Ophim".equals(name)) {
                                    loadXemThemPhimOPhim(currentPage, type, theloai, quocgia);
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(XemThemPhim.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }, XemThemPhim.this); // Thêm XemThemPhim.this làm Context);
                    }
                }
            }
        });
    }

    private void loadXemThemPhimKKPhim(int page, String type, String theLoaiSlug, String quocGiaSlug) {
        isLoading = true;  // Đánh dấu bắt đầu tải dữ liệu
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DSPhimResponse> call = null;

        // Kiểm tra và gọi API lấy dữ liệu theo thể loại hoặc quốc gia
        if (theLoaiSlug != null && !theLoaiSlug.isEmpty()) {
            call = apiService.getTheLoaiKKPhim(theLoaiSlug, page);  // Thể loại
        } else if (quocGiaSlug != null && !quocGiaSlug.isEmpty()) {
            call = apiService.getQuocGiaKKPhim(quocGiaSlug, page);  // Quốc gia
        } else {
            // Kiểm tra nếu type không phải thể loại hoặc quốc gia
            switch (type) {
                case "movie":
                    call = apiService.getPhimLe(page);
                    break;
                case "series":
                    call = apiService.getSeries(page);
                    break;
                case "hoathinh":
                    call = apiService.getHoatHinh(page);
                    break;
                case "tvShow":
                    call = apiService.getTVShow(page);
                    break;
                default:
                    isLoading = false;
                    return;
            }
        }

        if (call != null) {
            call.enqueue(new Callback<DSPhimResponse>() {
                @Override
                public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DSPhimXemThem.addAll(response.body().getData().getItems());
                        dsPhimAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("XemThemPhim", "Phản hồi API không thành công: " + response.message());
                    }
                    isLoading = false;
                }

                @Override
                public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                    isLoading = false;
                    Log.e("loadXemThemPhimKKPhim", t.getMessage());
                }
            });
        }
    }


    private void loadXemThemPhimOPhim(int page, String type, String theLoaiSlug, String quocGiaSlug) {
        isLoading = true;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DSResponseOphim> call = null;
        // Kiểm tra và gọi API lấy dữ liệu theo thể loại hoặc quốc gia
        if (theLoaiSlug != null && !theLoaiSlug.isEmpty()) {
            call = apiService.getTheLoaiOPhim(theLoaiSlug, page);  // Thể loại
        } else if (quocGiaSlug != null && !quocGiaSlug.isEmpty()) {
            call = apiService.getQuocGiaOPhim(quocGiaSlug, page);  // Quốc gia
        } else {
            // Kiểm tra nếu type không phải thể loại hoặc quốc gia
            switch (type) {
                case "movie":
                    call = apiService.getPhimLeOphim(page);
                    break;
                case "series":
                    call = apiService.getSeriesOphim(page);
                    break;
                case "hoathinh":
                    call = apiService.getHoatHinhOphim(page);
                    break;
                case "tvShow":
                    call = apiService.getTVShowOphim(page);
                    break;

                default:
                    isLoading = false;
                    return;
            }
        }

        if (call != null) {
            call.enqueue(new Callback<DSResponseOphim>() {
                @Override
                public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        seriesOphimList.addAll(response.body().getData().getItems());
                        seriesAdapterOphim.notifyDataSetChanged();
                    } else {
                        Log.e("XemThemPhim", "Phản hồi API không thành công: " + response.message());
                    }
                    isLoading = false;
                }

                @Override
                public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                    isLoading = false;
                    Log.e("loadXemThemPhimOPhim", t.getMessage());
                }
            });
        }
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}

