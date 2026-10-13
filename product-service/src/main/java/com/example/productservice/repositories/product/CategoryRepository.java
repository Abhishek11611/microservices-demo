package com.example.productservice.repositories.product;

import com.example.productservice.entities.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {

}
