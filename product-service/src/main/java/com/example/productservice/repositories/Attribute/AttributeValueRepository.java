package com.example.productservice.repositories.Attribute;

import com.example.productservice.entities.attribute.Attribute;
import com.example.productservice.entities.attribute.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValue,Long> {

    Optional<AttributeValue> findByIdAndAttribute(Long AttributeId , Attribute attribute);

}
