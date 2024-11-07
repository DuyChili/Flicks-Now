package com.example.flicks_now.api;
import com.example.flicks_now.model.ChiTietPhim;
import com.example.flicks_now.response.DSPhimResponse;
import com.example.flicks_now.response.PhimResponse;
import com.example.flicks_now.response.DSResponseOphim;
import com.example.flicks_now.response.PhimResponseOphim;


import okhttp3.ResponseBody;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.Url;

public interface ApiService {

    @GET("danh-sach/phim-moi-cap-nhat")
    Call<PhimResponse> getMovies(@Query("page") int page);
    @GET("v1/api/danh-sach/phim-bo")
    Call<DSPhimResponse> getSeries(@Query("page") int page);
    @GET("v1/api/danh-sach/tv-shows")
    Call<DSPhimResponse> getTVShow(@Query("page") int page);

    @GET("v1/api/danh-sach/phim-le")
    Call<DSPhimResponse> getPhimLe(@Query("page") int page);

    @GET("v1/api/danh-sach/hoat-hinh")
    Call<DSPhimResponse> getHoatHinh(@Query("page") int page);

    @GET("phim/{slug}")
    Call<ChiTietPhim> getChiTietPhim(@Path("slug") String slug);

    @GET("v1/api/tim-kiem")
    Call<DSPhimResponse> searchMovies(@Query("keyword") String keyword, @Query("limit") int limit);

    // Thêm các phương thức API khác
    @GET("v1/api/tim-kiem")
    Call<DSResponseOphim> searchMoviesOphim(@Query("keyword") String keyword, @Query("limit") int limit);

    @GET("v1/api/danh-sach/phim-bo")
    Call<DSResponseOphim> getSeriesOphim(@Query("page") int page);

    @GET("v1/api/danh-sach/tv-shows")
    Call<DSResponseOphim> getTVShowOphim(@Query("page") int page);

    @GET("v1/api/danh-sach/phim-le")
    Call<DSResponseOphim> getPhimLeOphim(@Query("page") int page);

    @GET("v1/api/danh-sach/hoat-hinh")
    Call<DSResponseOphim> getHoatHinhOphim(@Query("page") int page);
    @GET("danh-sach/phim-moi-cap-nhat")
    Call<PhimResponseOphim> getMoviesOphim(@Query("page") int page);

    @GET("v1/api/quoc-gia/{slug}")
    Call<DSPhimResponse> getQuocGiaKKPhim(@Path("slug") String slug,@Query("page") int page);

    @GET("v1/api/the-loai/{slug}")
    Call<DSPhimResponse> getTheLoaiKKPhim(@Path("slug") String slug, @Query("page") int page);

    @GET("v1/api/quoc-gia/{slug}")
    Call<DSResponseOphim> getQuocGiaOPhim(@Path("slug") String slug,@Query("page") int page);

    @GET("v1/api/the-loai/{slug}")
    Call<DSResponseOphim> getTheLoaiOPhim(@Path("slug") String slug, @Query("page") int page);


    @GET
    Call<ResponseBody> downloadMovie(@Url String movieLink);
}
