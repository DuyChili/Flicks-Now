package com.example.flicks_now.model;

public class TrangThai {
    public int id_trangThai;
    public String name_trangThai;

    public TrangThai() {
        // Default constructor required for calls to DataSnapshot.getValue(TrangThai.class)
    }

    public TrangThai(int id_trangThai, String name_trangThai) {
        this.id_trangThai = id_trangThai;
        this.name_trangThai = name_trangThai;
    }
}
