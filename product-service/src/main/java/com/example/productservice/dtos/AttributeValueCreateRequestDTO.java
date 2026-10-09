package com.example.productservice.dtos;

import jakarta.validation.constraints.NotBlank;

public class AttributeValueCreateRequestDTO {

    @NotBlank
    private String code;

    @NotBlank
    private String value;

    private Integer sortOrder;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
