package com.example.productservice.repositories.brands;

import com.example.productservice.entities.brand.Brands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brands,Long> {

    boolean existsByName(String brandName);

    boolean existsBySlug(String slug);
}
