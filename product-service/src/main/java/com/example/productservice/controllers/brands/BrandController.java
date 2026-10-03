package com.example.productservice.controllers.brands;

import com.example.commoncore.dto.BaseAPIResponse;
import com.example.productservice.dtos.BrandsRequestDTO;
import com.example.productservice.service.brands.BrandService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/brand")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/create")
    public ResponseEntity<BaseAPIResponse<String>> sendOtp(@RequestBody BrandsRequestDTO brandsRequestDTO) {
        String createdBrandResponse = brandService.createBrand(brandsRequestDTO);
        BaseAPIResponse<String> response = new BaseAPIResponse<>(createdBrandResponse,"Brand Created successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
