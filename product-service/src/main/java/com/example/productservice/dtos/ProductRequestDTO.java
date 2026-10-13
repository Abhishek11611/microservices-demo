package com.example.productservice.dtos;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

public class ProductRequestDTO {

    private String name;
    private String productCode;
    private String slug;
    private String description;
    @NotBlank(message = "brandCode is required")
    private String brandCode;
    @NotEmpty(message = "Category IDs are required")
    private List<@NotNull @Positive Long> categoryIds;
    private List<ProductVariantRequestDTO> variants = new ArrayList<>();

    public ProductRequestDTO() {
    }

    public ProductRequestDTO(String name, String productCode, String slug, String description, String brandCode, List<Long> categoryIds, List<ProductVariantRequestDTO> variants) {
        this.name = name;
        this.productCode = productCode;
        this.slug = slug;
        this.description = description;
        this.brandCode = brandCode;
        this.categoryIds = categoryIds;
        this.variants = variants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
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

    public String getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<ProductVariantRequestDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantRequestDTO> variants) {
        this.variants = variants;
    }
}
