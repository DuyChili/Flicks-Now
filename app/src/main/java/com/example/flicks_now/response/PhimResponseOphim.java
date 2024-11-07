package com.example.flicks_now.response;


import com.example.flicks_now.model.PhimAPiOphim;

import java.util.List;

public class PhimResponseOphim {
    private boolean status;
    private List<PhimAPiOphim> items;
    private Pagination pagination;

    public static class Pagination {
        private int totalItems;
        private int totalItemsPerPage;
        private int currentPage;
        private int totalPages;

        // Getters and Setters
    }

    // Getters and Setters for MovieResponse
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<PhimAPiOphim> getItems() {
        return items;
    }

    public void setItems(List<PhimAPiOphim> items) {
        this.items = items;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
