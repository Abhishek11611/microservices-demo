package com.example.productservice.dtos;

import java.util.List;

public class PaginatedAPIResponse<T> {

    private String message;
    private boolean status;

    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalRecords;
    private int pageSize;
    private boolean lastPage;

    public PaginatedAPIResponse() {
    }

    public PaginatedAPIResponse(
            String message,
            boolean status,
            List<T> data,
            int currentPage,
            int totalPages,
            long totalRecords,
            int pageSize,
            boolean lastPage
    ) {
        this.message = message;
        this.status = status;
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalRecords = totalRecords;
        this.pageSize = pageSize;
        this.lastPage = lastPage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }
}

