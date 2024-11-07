package com.example.flicks_now.response;

import com.example.flicks_now.model.DSPhim;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DSPhimResponse {
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
        private List<DSPhim> items;

        public List<DSPhim> getItems() {
            return items;
        }
    }
    public class TheLoaiData {
        @SerializedName("name")
        private List<DSPhim.Category> name;

        public List<DSPhim.Category> getName() {
            return name;
        }
    }
}
