package com.example.productservice.dtos;

import java.util.ArrayList;
import java.util.List;

public class ProductVariantRequestDTO {

    private String variantSku;
    private String barcode;

    private List<ProductMediaRequestDTO> media = new ArrayList<>();

    private List<ProductAttributeRequestDTO> attributes = new ArrayList<>();

    public ProductVariantRequestDTO() {
    }

    public ProductVariantRequestDTO(String variantSku, List<ProductMediaRequestDTO> media) {
        this.variantSku = variantSku;
        this.media = media;
    }

    public String getVariantSku() {
        return variantSku;
    }

    public void setVariantSku(String variantSku) {
        this.variantSku = variantSku;
    }

    public List<ProductMediaRequestDTO> getMedia() {
        return media;
    }

    public void setMedia(List<ProductMediaRequestDTO> media) {
        this.media = media;
    }

    public List<ProductAttributeRequestDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ProductAttributeRequestDTO> attributes) {
        this.attributes = attributes;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
