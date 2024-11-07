package com.example.flicks_now.model;

public class LoaiNguoiDung {
    private int id;
    private String type;

    // Constructor không tham số (yêu cầu bởi Firebase)
    public LoaiNguoiDung() {
    }

    // Constructor có tham số
    public LoaiNguoiDung(int id, String type) {
        this.id = id;
        this.type = type;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

