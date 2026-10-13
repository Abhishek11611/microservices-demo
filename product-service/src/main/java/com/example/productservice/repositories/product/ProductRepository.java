package com.example.productservice.repositories.product;

import com.example.productservice.entities.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {

    boolean existsByProductCode(String productCode);

}
