package com.example.flicks_now.model;

public class ApiModel {
    private String id;
    private String name;
    private String url;

    public ApiModel() {
        // Constructor mặc định
    }

    public ApiModel(String id,String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}



