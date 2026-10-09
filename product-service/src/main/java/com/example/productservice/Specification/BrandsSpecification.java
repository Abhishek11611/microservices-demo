package com.example.productservice.Specification;

import com.example.productservice.dtos.BrandFilterDTO;
import com.example.productservice.entities.brand.Brands;
import org.springframework.data.jpa.domain.Specification;

public class BrandsSpecification {

    public static Specification<Brands> getSpecification(
            BrandFilterDTO filterDTO) {

        return (root, query, cb) -> cb.and(

                smartSearch(filterDTO.getSearch())
                        .toPredicate(root, query, cb)

        );
    }

    public static Specification<Brands> smartSearch(String search) {

        if (search == null || search.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String value = search.trim().toLowerCase();

        return (root, query, cb) -> cb.or(

                cb.like(
                        cb.lower(root.get("brandCode")),
                        "%" + value + "%"
                ),

                cb.like(
                        cb.lower(root.get("name")),
                        "%" + value + "%"
                ),

                cb.like(
                        cb.lower(root.get("slug")),
                        "%" + value + "%"
                ),

                cb.like(
                        cb.lower(root.get("description")),
                        "%" + value + "%"
                )
        );
    }
}
