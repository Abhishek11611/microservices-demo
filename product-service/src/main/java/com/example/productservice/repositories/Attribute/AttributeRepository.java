package com.example.productservice.repositories.Attribute;

import com.example.productservice.entities.attribute.Attribute;
import com.example.productservice.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute,Long> {

    boolean existsByNameAndStatus(String name, Status status);

}
