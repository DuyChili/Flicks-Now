package com.example.flicks_now.model;

public class TruyCap {
    private String id_user;
    private String thoigiantruycap;

    public TruyCap() {
        // Constructor rỗng cần thiết cho Firebase
    }

    public TruyCap(String id_user, String thoigiantruycap) {
        this.id_user = id_user;
        this.thoigiantruycap = thoigiantruycap;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getThoigiantruycap() {
        return thoigiantruycap;
    }

    public void setThoigiantruycap(String thoigiantruycap) {
        this.thoigiantruycap = thoigiantruycap;
    }
}

