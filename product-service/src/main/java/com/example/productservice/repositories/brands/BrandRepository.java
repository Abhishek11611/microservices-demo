package com.example.productservice.repositories.brands;

import com.example.productservice.entities.brand.Brands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brands,Long>, JpaSpecificationExecutor<Brands> {

    boolean existsByName(String brandName);

    boolean existsBySlug(String slug);

    boolean existsByBrandCode(String brandCode);

    Optional<Brands> findByBrandCode(String brandCode);
}
