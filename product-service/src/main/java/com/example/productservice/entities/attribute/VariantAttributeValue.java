package com.example.productservice.entities.attribute;

import com.example.productservice.entities.BaseEntity;
import com.example.productservice.entities.product.ProductVariant;
import jakarta.persistence.*;

@Entity
@Table(name = "variant_attribute_value")
public class VariantAttributeValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_id")
    private AttributeValue attributeValue;

    public VariantAttributeValue() {
    }

    public VariantAttributeValue(Long id, ProductVariant productVariant, AttributeValue attributeValue) {
        this.id = id;
        this.productVariant = productVariant;
        this.attributeValue = attributeValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public AttributeValue getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(AttributeValue attributeValue) {
        this.attributeValue = attributeValue;
    }
}
