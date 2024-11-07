package com.example.flicks_now.model;

public class BinhLuanPhim {
    public String userId;
    public String slug;
    public String commentText;
    public long timestamp;
    private String userName; // Thêm trường tên người dùng
    private String formattedDate; // Thêm trường ngày giờ định dạng
    private String commentId;

    // Constructor không đối số và có đối số
    public BinhLuanPhim() {}

    // Constructor
    public BinhLuanPhim(String userId, String movieSlug, String commentText, long timestamp, String userName, String formattedDate) {
        this.userId = userId;
        this.slug = movieSlug;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.userName = userName;
        this.formattedDate = formattedDate;
    }
    // Các getter và setter cho các trường dữ liệu
    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }


    public String getSlug() {
        return slug;
    }

    public void setSlug(String userName) {
        this.slug = userName;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getCommentId() {
        return commentId;
    }
}
