package com.example.productservice.dtos;

import jakarta.validation.constraints.NotBlank;

public class BrandsUpdateRequestDTO {

    @NotBlank(message = "brandCode is required")
    private String brandCode;
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "slug is required")
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
