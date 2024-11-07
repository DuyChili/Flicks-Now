package com.example.flicks_now.model;

public class Goi {
    private int id;
    private String Type;

    public Goi(int id, String Type) {
        this.id = id;
        this.Type = Type;
    }

    public int getId() {
        return id;
    }
    // Constructor mặc định (bắt buộc phải có)
    public Goi() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }
}
