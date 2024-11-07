package com.example.flicks_now.model;

public class PhimYeuThich {
    private String id_user; // ID của phim
    private String slug; // Slug phim

    public PhimYeuThich() {
        // Cần một constructor rỗng cho Firebase
    }

    public PhimYeuThich(String id_user, String slug) {
        this.id_user = id_user;
        this.slug = slug;
    }

    // Getters và setters

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
