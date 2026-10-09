package com.example.productservice.dtos;

public class BrandFilterDTO {

    private String search;

    public BrandFilterDTO() {
    }

    public BrandFilterDTO(String search) {
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
