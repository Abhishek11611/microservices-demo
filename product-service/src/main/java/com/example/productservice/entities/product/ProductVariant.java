package com.example.productservice.entities.product;

import com.example.productservice.entities.BaseEntity;
import com.example.productservice.entities.attribute.VariantAttributeValue;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "barcode")
    private String barcode;

    @OneToMany(mappedBy = "variant")
    private List<ProductMedia> productMedia;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL)
    private List<VariantAttributeValue> variantAttributeValues;

    public ProductVariant() {
    }

    public ProductVariant(Long id, String sku, Product product, String barcode, List<ProductMedia> productMedia) {
        this.id = id;
        this.sku = sku;
        this.product = product;
        this.barcode = barcode;
        this.productMedia = productMedia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public List<ProductMedia> getProductMedia() {
        return productMedia;
    }

    public void setProductMedia(List<ProductMedia> productMedia) {
        this.productMedia = productMedia;
    }

    public List<VariantAttributeValue> getVariantAttributeValues() {
        return variantAttributeValues;
    }

    public void setVariantAttributeValues(List<VariantAttributeValue> variantAttributeValues) {
        this.variantAttributeValues = variantAttributeValues;
    }
}
