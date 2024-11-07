package com.example.flicks_now.model;

public class ThongBao {
    private String idThongBao;
    private String title;
    private String time;
    private String content;
    private String id_user;
    private long createdAt;

    public ThongBao() {
    }

    // Constructor
    public ThongBao(String idThongBao, String id_user, String title, String time, String content) {
        this.idThongBao = idThongBao;
        this.title = title;
        this.time = time;
        this.content = content;
        this.id_user = id_user;
    }

    // Getter và setter
    public String getIdThongBao() {
        return idThongBao;
    }
    public void setIdThongBao(String idThongBao) {
        this.idThongBao = idThongBao;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }
    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }
    public long getCreatedAt() {
        return createdAt;
    }
}

