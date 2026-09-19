package com.example.productservice.entities.product;

import com.example.productservice.entities.BaseEntity;
import com.example.productservice.entities.brand.Brands;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "description",  columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id",nullable = false)
    private Brands brands;

    @OneToMany(mappedBy = "product")
    private List<ProductMedia> productMedia;

    @OneToMany(mappedBy = "product")
    private List<ProductVariant> productVariants;

    @OneToMany(mappedBy = "product")
    private List<ProductCategory> productCategories;


    public Product() {
    }

    public Product(Long id, String productCode, String name, String slug, String description, Brands brands, List<ProductMedia> productMedia, List<ProductVariant> productVariants, List<ProductCategory> productCategories) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.brands = brands;
        this.productMedia = productMedia;
        this.productVariants = productVariants;
        this.productCategories = productCategories;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
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

    public Brands getBrands() {
        return brands;
    }

    public void setBrands(Brands brands) {
        this.brands = brands;
    }

    public List<ProductMedia> getProductMedia() {
        return productMedia;
    }

    public void setProductMedia(List<ProductMedia> productMedia) {
        this.productMedia = productMedia;
    }

    public List<ProductVariant> getProductVariants() {
        return productVariants;
    }

    public void setProductVariants(List<ProductVariant> productVariants) {
        this.productVariants = productVariants;
    }

    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
    }
}
