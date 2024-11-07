package com.example.flicks_now.model;

public class HoTro {
    private String name;
    private String description;
    private String time;
    private String imageUrl;
    private String userId;

    public HoTro() {
    }
    public HoTro(String name, String description, String userId,String time) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.time = time;
    }

    public HoTro(String name, String description, String imageUrl,String time,String userId) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.time = time;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public String getTime() {
        return time;
    }
    public String getUserId() {
        return userId;
    }


}
