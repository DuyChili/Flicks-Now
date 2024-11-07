package com.example.flicks_now.model;

public class User {
    private String firebaseKey;
    private String id_user;
    private String name;
    private String status;
    private String email;
    private Long id_loaiND;
    private boolean isVIP;
    private String goi;
    private boolean isChecked; // Thêm thuộc tính isChecked
    private long created_at;

    public User() {
    }

    public User(String firebaseKey,String id_user, String name, String status, String goi,Long id_loaiND, long created_at,String email) {
        this.firebaseKey = firebaseKey;
        this.id_user = id_user;
        this.name = name;
        this.status = status;
        this.goi = goi;
        this.id_loaiND = id_loaiND;
        this.created_at = created_at;
        this.email = email;
    }
    public User(String id_user, String name, String status, String goi) {
        this.id_user = id_user;
        this.name = name;
        this.status = status;
        this.goi = goi;
    }
    public String getGoi() {
        return goi;
    }

    public void setGoi(String goi) {
        this.goi = goi;
    }

    public User(String id_user, String name, String status, Long id_loaiND, boolean isVIP) {
        this.id_user = id_user;
        this.name = name;
        this.status = status;
        this.id_loaiND = id_loaiND;
        this.isVIP = isVIP;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public Long getId_loaiND() {
        return id_loaiND;
    }
    public void setId_loaiND(Long id_loaiND) {
        this.id_loaiND = id_loaiND;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVIP() {
        return isVIP;
    }
    public boolean isChecked() {
        return isChecked;
    }
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
    public long getCreated_at() {
        return created_at;
    }
    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }
}

