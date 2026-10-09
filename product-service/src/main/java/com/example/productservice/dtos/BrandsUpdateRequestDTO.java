package com.example.productservice.dtos;

public class BrandsUpdateRequestDTO {

    private String brandCode;
    private String name;
    private String slug;
    private String description;

    public BrandsUpdateRequestDTO() {
    }

    public BrandsUpdateRequestDTO(String brandCode, String name, String slug, String description) {
        this.brandCode = brandCode;
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
