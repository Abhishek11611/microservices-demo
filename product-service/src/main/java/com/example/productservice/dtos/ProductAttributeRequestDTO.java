package com.example.productservice.dtos;

public class ProductAttributeRequestDTO {

    private Long attributeId;

    private Long attributeValueId;

    public ProductAttributeRequestDTO() {
    }

    public ProductAttributeRequestDTO(Long attributeId, Long attributeValueId) {
        this.attributeId = attributeId;
        this.attributeValueId = attributeValueId;
    }

    public ProductAttributeRequestDTO(Long attributeId) {
        this.attributeId = attributeId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public Long getAttributeValueId() {
        return attributeValueId;
    }

    public void setAttributeValueId(Long attributeValueId) {
        this.attributeValueId = attributeValueId;
    }
}
