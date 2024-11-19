package com.example.flicks_now.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Series implements Parcelable {
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

    // Constructor
    public Series(String _id, String name, String slug, String origin_name, String poster_url, String thumb_url,
                  String episode_current, String quality, String lang, int year,
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

    public String getThumb_url() {
        return thumb_url;
    }

    public String getPosterUrl() {
        return APP_DOMAIN_CDN_IMAGE + getPoster_url(); // Kết hợp để tạo URL đầy đủ
    }

    public int getYear() {
        return year;
    }

    public List<Category> getCategory() {
        return category;
    }

    public List<Country> getCountry() {
        return country;
    }

    // Implement Parcelable
    protected Series(Parcel in) {
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

        // Read modified
        modified = in.readParcelable(Modified.class.getClassLoader());

        // Read categories
        category = new ArrayList<>();
        in.readList(category, Category.class.getClassLoader());

        // Read countries
        country = new ArrayList<>();
        in.readList(country, Country.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(name);
        dest.writeString(slug);
        dest.writeString(origin_name);
        dest.writeString(poster_url);
        dest.writeString(thumb_url);
        dest.writeString(episode_current);
        dest.writeString(quality);
        dest.writeString(lang);
        dest.writeInt(year);

        // Write modified
        dest.writeParcelable(modified, flags);

        // Write categories
        dest.writeList(category);

        // Write countries
        dest.writeList(country);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Series> CREATOR = new Creator<Series>() {
        @Override
        public Series createFromParcel(Parcel in) {
            return new Series(in);
        }

        @Override
        public Series[] newArray(int size) {
            return new Series[size];
        }
    };

    // Class Modified, Category, Country cũng cần implement Parcelable nếu bạn muốn truyền chúng
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

        // Getter
        public String getTime() {
            return time;
        }
    }

    public static class Category implements Parcelable {
        private String name;
        private String slug;

        public Category(String name, String slug) {
            this.name = name;
            this.slug = slug;
        }

        protected Category(Parcel in) {
            name = in.readString();
            slug = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
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
