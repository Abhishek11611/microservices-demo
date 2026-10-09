package com.example.productservice.entities.attribute;

import com.example.productservice.entities.BaseEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "attribute")
public class Attribute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name")
    private String name;

    @OneToMany(
            mappedBy = "attribute",
            cascade = CascadeType.ALL
    )
    List<AttributeValue> attributeValues;

    public Attribute() {
    }

    public Attribute(Long id, String code, String name, List<AttributeValue> attributeValues) {
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

    public List<AttributeValue> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<AttributeValue> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
