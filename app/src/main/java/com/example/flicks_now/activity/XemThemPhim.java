package com.example.flicks_now.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
    ;
    private boolean isLoading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemThemPhimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nhận dữ liệu từ Intent và lưu vào một biến duy nhất
        type = getIntent().getStringExtra("type");
        Log.d("XemThemPhim", "Received type: " + type);
        // Khởi tạo các danh sách và adapter
        DSPhimXemThem = new ArrayList<>();
        dsPhimAdapter = new DSPhimAdapter(this, DSPhimXemThem);
        seriesOphimList = new ArrayList<>();
        seriesAdapterOphim = new DSPhimAdapterOphim(this, seriesOphimList);
        binding.recyclerViewMovies.setLayoutManager(new GridLayoutManager(this, 3));
        // Tải dữ liệu ban đầu
        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Tải dữ liệu ban đầu
                    binding.recyclerViewMovies.setAdapter(dsPhimAdapter);
                } else if ("Ophim".equals(name)) {
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
                    // Tải dữ liệu ban đầu
                    loadXemThemPhimKKPhim(currentPage, type);
                } else if ("Ophim".equals(name)) {
                    loadXemThemPhimOPhim(currentPage, type);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(XemThemPhim.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, XemThemPhim.this); // Thêm XemThemPhim.this làm Context);
        // Nhận danh sách và loại phim từ Intent
        List<DSPhim> receivedSeriesKkphimList = getIntent().getParcelableArrayListExtra("seriesList");
        List<DSPhim> phimLeList = getIntent().getParcelableArrayListExtra("phimLe");
        List<DSPhim> tvShowList = getIntent().getParcelableArrayListExtra("tVshow");
        List<DSPhim> phimHoatHinhList = getIntent().getParcelableArrayListExtra("hoatHinh");
        // Nhận danh sách và loại phim từ Intent
        List<DSPhimAPiOphim> receivedSeriesOphimList = getIntent().getParcelableArrayListExtra("seriesList");
        List<DSPhimAPiOphim> phimLeOphimList = getIntent().getParcelableArrayListExtra("phimLe");
        List<DSPhimAPiOphim> tvShowOphimList = getIntent().getParcelableArrayListExtra("tVshow");
        List<DSPhimAPiOphim> phimHoatHinhOphimList = getIntent().getParcelableArrayListExtra("hoatHinh");

        // Tải dữ liệu ban đầu
        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Tải dữ liệu ban đầu
                    // Cập nhật dữ liệu dựa trên loại phim
                    if ("series".equals(type) && receivedSeriesKkphimList != null) {
                        DSPhimXemThem.addAll(receivedSeriesKkphimList);
                        dsPhimAdapter.notifyDataSetChanged(); // Cập nhật cho phim bộ
                        setTitle("Danh Sách Phim Bộ");
                    } else if ("movie".equals(type) && phimLeList != null) {
                        DSPhimXemThem.addAll(phimLeList);
                        dsPhimAdapter.notifyDataSetChanged(); // Cập nhật cho phim lẻ
                        setTitle("Danh Sách Phim Lẻ");
                    } else if ("hoathinh".equals(type) && phimHoatHinhList != null) {
                        DSPhimXemThem.addAll(phimHoatHinhList);
                        dsPhimAdapter.notifyDataSetChanged(); // Cập nhật cho phim hoạt hình
                        setTitle("Danh Sách Phim Hoạt Hình");
                    } else if (tvShowList != null) { // Chỉ cần kiểm tra tvShowList
                        DSPhimXemThem.addAll(tvShowList);
                        dsPhimAdapter.notifyDataSetChanged(); // Cập nhật cho chương trình truyền hình
                        setTitle("Danh Sách TV Show");
                    } else {
                        Log.e("AllMovieActivity", "All lists are null!");
                    }
                } else if ("Ophim".equals(name)) {
                    // Cập nhật dữ liệu dựa trên loại phim
                    if ("series".equals(type) && receivedSeriesOphimList != null) {
                        seriesOphimList.addAll(receivedSeriesOphimList);
                        seriesAdapterOphim.notifyDataSetChanged(); // Cập nhật cho phim bộ
                        setTitle("Danh Sách Phim Bộ");
                    } else if ("movie".equals(type) && phimLeOphimList != null) {
                        seriesOphimList.addAll(phimLeOphimList);
                        seriesAdapterOphim.notifyDataSetChanged(); // Cập nhật cho phim lẻ
                        setTitle("Danh Sách Phim Lẻ");
                    } else if ("hoathinh".equals(type) && phimHoatHinhOphimList != null) {
                        seriesOphimList.addAll(phimHoatHinhOphimList);
                        seriesAdapterOphim.notifyDataSetChanged(); // Cập nhật cho phim hoạt hình
                        setTitle("Danh Sách Phim Hoạt Hình");
                    } else if (tvShowList != null) { // Chỉ cần kiểm tra tvShowList
                        seriesOphimList.addAll(tvShowOphimList);
                        seriesAdapterOphim.notifyDataSetChanged(); // Cập nhật cho chương trình truyền hình
                        setTitle("Danh Sách TV Show");
                    } else {
                        Log.e("AllMovieActivity", "All lists are null!");
                    }
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
                        currentPage++;
                        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
                            @Override
                            public void onBaseUrlFetched(String name, String url) {
                                if ("Kkphim".equals(name)) {
                                    // Tải dữ liệu ban đầu
                                    loadXemThemPhimKKPhim(currentPage, type);
                                } else if ("Ophim".equals(name)) {
                                    loadXemThemPhimOPhim(currentPage, type);
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

    private void loadXemThemPhimKKPhim(int page, String type) {
        isLoading = true;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DSPhimResponse> call;
        switch (type) {
            case "phimle":
                call = apiService.getPhimLe(page);
                break;
            case "series":
                call = apiService.getSeries(page);
                break;
            case "hoathinh":
                call = apiService.getHoatHinh(page);
                break;
            case "tvshow":
                call = apiService.getTVShow(page);
                break;
            default:
                isLoading = false;
                return;
        }

        call.enqueue(new Callback<DSPhimResponse>() {
            @Override
            public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DSPhim> newMovies = response.body().getData().getItems();
                    if (newMovies != null) {
                        DSPhimXemThem.addAll(newMovies);
                        dsPhimAdapter.notifyDataSetChanged();
                    }
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                isLoading = false;
            }
        });
    }
    private void loadXemThemPhimOPhim(int page, String type) {
        isLoading = true;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DSPhimResponse> call;
        switch (type) {
            case "phimle":
                call = apiService.getPhimLe(page);
                break;
            case "series":
                call = apiService.getSeries(page);
                break;
            case "hoathinh":
                call = apiService.getHoatHinh(page);
                break;
            case "tvshow":
                call = apiService.getTVShow(page);
                break;
            default:
                isLoading = false;
                return;
        }

        call.enqueue(new Callback<DSPhimResponse>() {
            @Override
            public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DSPhim> newMovies = response.body().getData().getItems();
                    if (newMovies != null) {
                        DSPhimXemThem.addAll(newMovies);
                        dsPhimAdapter.notifyDataSetChanged();
                    }
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                isLoading = false;
            }
        });
    }
}

