package com.example.flicks_now.response;

import com.example.flicks_now.model.Phim;

import java.util.List;

public class PhimResponse {
    private boolean status;
    private List<Phim> items;
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

    public List<Phim> getItems() {
        return items;
    }

    public void setItems(List<Phim> items) {
        this.items = items;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
