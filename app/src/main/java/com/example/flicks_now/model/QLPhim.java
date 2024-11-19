package com.example.flicks_now.model;


public class QLPhim {
    private String id_movie; // ID phim
    private String name; // Tên phim
    private String content; // Nội dung phim
    private String poster_url; // URL poster phim
    private String thumb_url; // URL thumbnail phim
    private String goi; // ID gói
    private String time; // Thời gian phim
    private String year; // Năm sản xuất
    private String tacGia; // Tác giả
    private String theLoai; // Thể loại
    private String dienVien; // Diễn viên
    private String quocGia; // Quốc gia
    private float rating; // Đánh giá
    private String ngayThemPhim; // Ngày thêm phim
    private String ngayCapNhat; // Ngày cập nhật phim
    private String movie_url; // URL video phim

    // Constructor có tham số
    public QLPhim(String id_movie, String name, String content, String poster_url, String thumb_url,
                String goi, String time, String year, String tacGia, String theLoai, String dienVien,
                String quocGia, float rating, String ngayThemPhim, String ngayCapNhat,
                String movie_url) {
        this.id_movie = id_movie;
        this.name = name;
        this.content = content;
        this.poster_url = poster_url;
        this.thumb_url = thumb_url;
        this.goi = goi;
        this.time = time;
        this.year = year;
        this.tacGia = tacGia;
        this.theLoai = theLoai;
        this.dienVien = dienVien;
        this.quocGia = quocGia;
        this.rating = rating;
        this.ngayThemPhim = ngayThemPhim;
        this.ngayCapNhat = ngayCapNhat;
        this.movie_url = movie_url; // Thêm movieUrl vào constructor
    }

    // Constructor không tham số
    public QLPhim() {
        this.id_movie = "";
        this.name = "";
        this.content = "";
        this.poster_url = "";
        this.thumb_url = "";
        this.goi = "0";
        this.time = "0";
        this.year = "0";
        this.tacGia = "";
        this.theLoai = "";
        this.dienVien = "";
        this.quocGia = "";
        this.rating = 0.0f;
        this.ngayThemPhim = "";
        this.ngayCapNhat = "";
        this.movie_url = ""; // Khởi tạo movieUrl
    }

    // Getter và Setter cho các thuộc tính
    public String getId_movie() {
        return id_movie;
    }

    public void setId_movie(String id_movie) {
        this.id_movie = id_movie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPoster_url() {
        return poster_url;
    }

    public void setPoster_url(String poster_url) {
        this.poster_url = poster_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getGoi() {
        return goi;
    }

    public void setGoi(String goi) {
        this.goi = goi;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTacGia() {
        return tacGia;
    }

    public void setTacGia(String tacGia) {
        this.tacGia = tacGia;
    }

    public String getTheLoai() {
        return theLoai;
    }

    public void setTheLoai(String theLoai) {
        this.theLoai = theLoai;
    }

    public String getDienVien() {
        return dienVien;
    }

    public void setDienVien(String dienVien) {
        this.dienVien = dienVien;
    }

    public String getQuocGia() {
        return quocGia;
    }

    public void setQuocGia(String quocGia) {
        this.quocGia = quocGia;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getNgayThemPhim() {
        return ngayThemPhim;
    }

    public void setNgayThemPhim(String ngayThemPhim) {
        this.ngayThemPhim = ngayThemPhim;
    }

    public String getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(String ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getMovie_url() {
        return movie_url; // Getter cho movieUrl
    }

    public void setMovie_url(String movie_url) {
        this.movie_url = movie_url; // Setter cho movieUrl
    }
}

