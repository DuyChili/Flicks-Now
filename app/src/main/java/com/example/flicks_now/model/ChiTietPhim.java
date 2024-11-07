package com.example.flicks_now.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChiTietPhim {
    @SerializedName("status")
    private boolean status;

    @SerializedName("msg")
    private String msg;

    @SerializedName("movie")
    private MovieItem movie;

    @SerializedName("episodes")
    private List<TapPhim> episodes;

    public ChiTietPhim(String movieId, String movieTitle, String episodeCurrent, String posterUrl, String linkM3u8) {
        this.movie = new MovieItem();
        this.movie.setId(movieId);
        this.movie.setName(movieTitle);
        this.movie.setEpisodeCurrent(episodeCurrent);
        this.movie.setPosterUrl(posterUrl);
        this.movie.setThumbUrl(linkM3u8);
    }

    // Getters and setters

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MovieItem getMovie() {
        return movie;
    }

    public void setMovie(MovieItem movie) {
        this.movie = movie;
    }

    public List<TapPhim> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<TapPhim> episodes) {
        this.episodes = episodes;
    }

    public static class MovieItem {
        @SerializedName("tmdb")
        private Tmdb tmdb;

        @SerializedName("imdb")
        private Imdb imdb;

        @SerializedName("created")
        private Created created;

        @SerializedName("modified")
        private Modified modified;

        @SerializedName("_id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        @SerializedName("origin_name")
        private String originName;

        @SerializedName("content")
        private String content;

        @SerializedName("type")
        private String type;

        @SerializedName("status")
        private String status;

        @SerializedName("poster_url")
        private String posterUrl;

        @SerializedName("thumb_url")
        private String thumbUrl;

        @SerializedName("is_copyright")
        private boolean isCopyright;

        @SerializedName("sub_docquyen")
        private boolean subDocquyen;

        @SerializedName("chieurap")
        private boolean chieurap;

        @SerializedName("trailer_url")
        private String trailerUrl;

        @SerializedName("time")
        private String time;

        @SerializedName("episode_current")
        private String episodeCurrent;

        @SerializedName("episode_total")
        private String episodeTotal;

        @SerializedName("quality")
        private String quality;

        @SerializedName("lang")
        private String lang;

        @SerializedName("notify")
        private String notify;

        @SerializedName("showtimes")
        private String showtimes;

        @SerializedName("year")
        private int year;

        @SerializedName("view")
        private int view;

        @SerializedName("actor")
        private List<String> actor;

        @SerializedName("director")
        private List<String> director;

        @SerializedName("category")
        private List<Category> category;

        @SerializedName("country")
        private List<Country> country;

        // Getters and setters

        public Tmdb getTmdb() {
            return tmdb;
        }

        public void setTmdb(Tmdb tmdb) {
            this.tmdb = tmdb;
        }

        public Imdb getImdb() {
            return imdb;
        }

        public void setImdb(Imdb imdb) {
            this.imdb = imdb;
        }

        public Created getCreated() {
            return created;
        }

        public void setCreated(Created created) {
            this.created = created;
        }

        public Modified getModified() {
            return modified;
        }

        public void setModified(Modified modified) {
            this.modified = modified;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getOriginName() {
            return originName;
        }

        public void setOriginName(String originName) {
            this.originName = originName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPosterUrl() {
            return posterUrl;
        }

        public void setPosterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
        }

        public String getThumbUrl() {
            return thumbUrl;
        }

        public void setThumbUrl(String thumbUrl) {
            this.thumbUrl = thumbUrl;
        }

        public boolean isCopyright() {
            return isCopyright;
        }

        public void setCopyright(boolean copyright) {
            isCopyright = copyright;
        }

        public boolean isSubDocquyen() {
            return subDocquyen;
        }

        public void setSubDocquyen(boolean subDocquyen) {
            this.subDocquyen = subDocquyen;
        }

        public boolean isChieurap() {
            return chieurap;
        }

        public void setChieurap(boolean chieurap) {
            this.chieurap = chieurap;
        }

        public String getTrailerUrl() {
            return trailerUrl;
        }

        public void setTrailerUrl(String trailerUrl) {
            this.trailerUrl = trailerUrl;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getEpisodeCurrent() {
            return episodeCurrent;
        }

        public void setEpisodeCurrent(String episodeCurrent) {
            this.episodeCurrent = episodeCurrent;
        }

        public String getEpisodeTotal() {
            return episodeTotal;
        }

        public void setEpisodeTotal(String episodeTotal) {
            this.episodeTotal = episodeTotal;
        }

        public String getQuality() {
            return quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getNotify() {
            return notify;
        }

        public void setNotify(String notify) {
            this.notify = notify;
        }

        public String getShowtimes() {
            return showtimes;
        }

        public void setShowtimes(String showtimes) {
            this.showtimes = showtimes;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getView() {
            return view;
        }

        public void setView(int view) {
            this.view = view;
        }

        public List<String> getActor() {
            return actor;
        }

        public void setActor(List<String> actor) {
            this.actor = actor;
        }

        public List<String> getDirector() {
            return director;
        }

        public void setDirector(List<String> director) {
            this.director = director;
        }

        public List<Category> getCategory() {
            return category;
        }

        public void setCategory(List<Category> category) {
            this.category = category;
        }

        public List<Country> getCountry() {
            return country;
        }

        public void setCountry(List<Country> country) {
            this.country = country;
        }

        public static class Tmdb {
            @SerializedName("type")
            private String type;

            @SerializedName("id")
            private String id;

            @SerializedName("season")
            private int season;

            @SerializedName("vote_average")
            private double voteAverage;

            @SerializedName("vote_count")
            private int voteCount;

            // Getters and setters
        }

        public static class Imdb {
            @SerializedName("id")
            private String id;

            // Getters and setters
        }

        public static class Created {
            @SerializedName("time")
            private String time;

            public String getTime() {
                return time;
            }

            public void setTime(String time) {
                this.time = time;
            }
        }

        public static class Modified {
            @SerializedName("time")
            private String time;

            public String getTime() {
                return time;
            }

            public void setTime(String time) {
                this.time = time;
            }
        }

        public static class Category {
            @SerializedName("id")
            private String id;

            @SerializedName("name")
            private String name;

            @SerializedName("slug")
            private String slug;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSlug() {
                return slug;
            }

            public void setSlug(String slug) {
                this.slug = slug;
            }
        }

        public static class Country {
            @SerializedName("id")
            private String id;

            @SerializedName("name")
            private String name;

            @SerializedName("slug")
            private String slug;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSlug() {
                return slug;
            }

            public void setSlug(String slug) {
                this.slug = slug;
            }
        }
    }

    public static class TapPhim {
        @SerializedName("server_name")
        private String serverName;

        @SerializedName("server_data")
        private List<DuLieuServer> serverData;

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public List<DuLieuServer> getServerData() {
            return serverData;
        }

        public void setServerData(List<DuLieuServer> serverData) {
            this.serverData = serverData;
        }

        public static class DuLieuServer {
            @SerializedName("name")
            private String name;

            @SerializedName("slug")
            private String slug;

            @SerializedName("filename")
            private String filename;

            @SerializedName("link_embed")
            private String linkEmbed;

            @SerializedName("link_m3u8")
            private String linkM3u8;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSlug() {
                return slug;
            }

            public void setSlug(String slug) {
                this.slug = slug;
            }

            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public String getLinkEmbed() {
                return linkEmbed;
            }

            public void setLinkEmbed(String linkEmbed) {
                this.linkEmbed = linkEmbed;
            }

            public String getLinkM3u8() {
                return linkM3u8;
            }

            public void setLinkM3u8(String linkM3u8) {
                this.linkM3u8 = linkM3u8;
            }
        }
    }
}

