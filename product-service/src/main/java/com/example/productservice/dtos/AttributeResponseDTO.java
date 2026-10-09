package com.example.productservice.dtos;

import java.util.List;

public class AttributeResponseDTO {

    private Long id;
    private String code;
    private String name;
    private List<AttributeValueResponseDTO> attributeValues;


    public AttributeResponseDTO() {
    }

    public AttributeResponseDTO(Long id, String code, String name, List<AttributeValueResponseDTO> attributeValues) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.attributeValues = attributeValues;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<AttributeValueResponseDTO> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<AttributeValueResponseDTO> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
