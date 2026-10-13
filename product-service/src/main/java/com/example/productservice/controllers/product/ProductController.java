package com.example.productservice.controllers.product;

import com.example.commoncore.dto.BaseAPIResponse;
import com.example.productservice.dtos.BrandsRequestDTO;
import com.example.productservice.dtos.ProductRequestDTO;
import com.example.productservice.service.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<BaseAPIResponse<String>> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {

        String product = productService.createProduct(productRequestDTO);
        BaseAPIResponse<String> response = new BaseAPIResponse<>(product,"Product Created successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
