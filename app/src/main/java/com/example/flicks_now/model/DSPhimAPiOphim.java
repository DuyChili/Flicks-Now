package com.example.flicks_now.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DSPhimAPiOphim implements DSPhimAPI {
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
    private boolean chieurap;
    private boolean sub_docquyen;
    private String time;

    // Thêm trường này để lấy APP_DOMAIN_CDN_IMAGE
    private static final String APP_DOMAIN_CDN_IMAGE = "https://img.ophim.live/uploads/movies/";

    private Modified modified;
    private List<Category> category;
    private List<Country> country;

    // Constructor
    public DSPhimAPiOphim(String _id, String name, String slug, String origin_name, String poster_url, String thumb_url,
                          String episode_current, String quality, String lang, int year,
                          boolean chieurap, boolean sub_docquyen, String time,
                          Modified modified, List<Category> category, List<Country> country) {
        this._id = _id;
        this.name = name;
        this.slug = slug;
        this.origin_name = origin_name;
        this.poster_url = poster_url;
        this.thumb_url = thumb_url;
        this.episode_current = episode_current;
        this.quality = quality;
        this.lang = lang;
        this.year = year;
        this.chieurap = chieurap;
        this.sub_docquyen = sub_docquyen;
        this.time = time;
        this.modified = modified;
        this.category = category;
        this.country = country;
    }

    // Getters
    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
    public String getPoster_url() {
        return poster_url;
    }
    public String getPosterUrl() {
        return APP_DOMAIN_CDN_IMAGE + getPoster_url(); // Kết hợp để tạo URL đầy đủ
    }

    public String getThumbUrl() {
        return APP_DOMAIN_CDN_IMAGE + thumb_url; // Kết hợp để tạo URL đầy đủ
    }

    public int getYear() {
        return year;
    }

    public boolean isChieurap() {
        return chieurap;
    }

    public boolean isSubDocquyen() {
        return sub_docquyen;
    }

    public String getTime() {
        return time;
    }

    public List<Category> getCategory() {
        return category;
    }

    public List<Country> getCountry() {
        return country;
    }

    // Implement Parcelable
    protected DSPhimAPiOphim(Parcel in) {
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
        chieurap = in.readByte() != 0;
        sub_docquyen = in.readByte() != 0;
        time = in.readString();
        modified = in.readParcelable(Modified.class.getClassLoader());
        category = new ArrayList<>();
        in.readList(category, Category.class.getClassLoader());
        country = new ArrayList<>();
        in.readList(country, Country.class.getClassLoader());
    }

    // Nested classes for Modified, Category, and Country (same as before)
    public static class Modified implements Parcelable {
        private String time;

        public Modified(String time) {
            this.time = time;
        }

        protected Modified(Parcel in) {
            time = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(time);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Modified> CREATOR = new Creator<Modified>() {
            @Override
            public Modified createFromParcel(Parcel in) {
                return new Modified(in);
            }

            @Override
            public Modified[] newArray(int size) {
                return new Modified[size];
            }
        };

        public String getTime() {
            return time;
        }
    }

    public static class Category implements Parcelable {
        private String id;
        private String name;
        private String slug;

        public Category(String id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }

        protected Category(Parcel in) {
            id = in.readString();
            name = in.readString();
            slug = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(slug);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Category> CREATOR = new Creator<Category>() {
            @Override
            public Category createFromParcel(Parcel in) {
                return new Category(in);
            }

            @Override
            public Category[] newArray(int size) {
                return new Category[size];
            }
        };

        // Getters
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }
    public static class Country implements Parcelable {
        private String id;
        private String name;
        private String slug;

        public Country(String id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }

        protected Country(Parcel in) {
            id = in.readString();
            name = in.readString();
            slug = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(slug);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Country> CREATOR = new Creator<Country>() {
            @Override
            public Country createFromParcel(Parcel in) {
                return new Country(in);
            }

            @Override
            public Country[] newArray(int size) {
                return new Country[size];
            }
        };

        // Getters
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }

}

