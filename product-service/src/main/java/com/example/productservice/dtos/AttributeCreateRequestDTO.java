package com.example.productservice.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class AttributeCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @Valid
    private List<AttributeValueCreateRequestDTO> attributeValues = new ArrayList<>();

    public AttributeCreateRequestDTO(String name, List<AttributeValueCreateRequestDTO> attributeValues) {
        this.name = name;
        this.attributeValues = attributeValues;
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
