package com.example.flicks_now.response;

import com.example.flicks_now.model.DSPhimAPiOphim;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DSResponseOphim {
    private String status;
    private String message;
    private SeriesData data;
    private TheLoaiData name;


    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public SeriesData getData() {
        return data;
    }
    public TheLoaiData getTheLoaiData() {
        return name;
    }

    public class SeriesData {
        @SerializedName("items")
        private List<DSPhimAPiOphim> items;

        public List<DSPhimAPiOphim> getItems() {
            return items;
        }
    }
    public class TheLoaiData {
        @SerializedName("name")
        private List<DSPhimAPiOphim.Category> name;
        public List<DSPhimAPiOphim.Category> getName() {
            return name;
        }
    }
}