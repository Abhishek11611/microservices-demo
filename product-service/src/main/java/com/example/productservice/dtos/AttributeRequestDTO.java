package com.example.productservice.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class AttributeRequestDTO {

    @NotBlank
    private String code;

    private String name;

    @Valid
    private List<AttributeValueCreateRequestDTO> attributeValues = new ArrayList<>();

    public AttributeRequestDTO() {
    }

    public AttributeRequestDTO(String code, String name, List<AttributeValueCreateRequestDTO> attributeValues) {
        this.code = code;
        this.name = name;
        this.attributeValues = attributeValues;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AttributeValueCreateRequestDTO> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<AttributeValueCreateRequestDTO> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
