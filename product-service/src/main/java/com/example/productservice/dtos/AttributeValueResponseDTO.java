package com.example.productservice.dtos;

public class AttributeValueResponseDTO {

    private Long id;
    private String code;
    private String value;
    private Integer sortOrder;

    public AttributeValueResponseDTO() {
    }

    public AttributeValueResponseDTO(
            Long id,
            String code,
            String value,
            Integer sortOrder) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
