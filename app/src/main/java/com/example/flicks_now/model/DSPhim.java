package com.example.flicks_now.model;
import android.os.Parcel;

import java.util.List;

public class DSPhim implements DSPhimAPI {
    private String _id;
    private String name;
    private String slug;
    private String origin_name;
    private String poster_url;
    private String thumb_url;
    private String episode_current;
    private String quality;
    private String lang;
    private int year;
    // Thêm trường này để lấy APP_DOMAIN_CDN_IMAGE
    private static final String APP_DOMAIN_CDN_IMAGE = "https://phimimg.com/";
    private Modified modified;
    private List<Category> category;
    private List<Country> country;


    protected DSPhim(Parcel in) {
        _id = in.readString();
        name = in.readString();
        slug = in.readString();
        origin_name = in.readString();
        poster_url = in.readString();
        thumb_url = in.readString();
        episode_current = in.readString();
        quality = in.readString();
        lang = in.readString();
        year = in.readInt();
    }

    public String getSlug() {
        return slug;
    }

    public List<Category> getCategory() {
        return category;
    }

    public List<Country> getCountry() {
        return country;
    }

    public static class Modified {
        private String time;

        // Getter and Setter
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public static class Category {
        private String name;
        private String slug;

        // Getters and Setters

        public Category(String name, String slug) {
            this.name = name;
            this.slug = slug;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }

    public static class Country {
        private String id;
        private String name;
        private String slug;

        // Getters and Setters
    }

    // Getters and Setters for Series
    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoster_url() {
        return poster_url;
    }

    public String getPosterUrl() {
        return APP_DOMAIN_CDN_IMAGE + poster_url; // Kết hợp để tạo URL đầy đủ
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
