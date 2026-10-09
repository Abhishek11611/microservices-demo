package com.example.productservice.controllers.brands;

import com.example.commoncore.dto.BaseAPIResponse;
import com.example.productservice.dtos.*;
import com.example.productservice.entities.brand.Brands;
import com.example.productservice.service.brands.BrandService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/brand")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/create")
    public ResponseEntity<BaseAPIResponse<String>> createBrand(@RequestBody BrandsRequestDTO brandsRequestDTO) {
        String createdBrandResponse = brandService.createBrand(brandsRequestDTO);
        BaseAPIResponse<String> response = new BaseAPIResponse<>(createdBrandResponse,"Brand Created successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<BaseAPIResponse<String>> updateBrand(@RequestBody BrandsUpdateRequestDTO brandsUpdateRequestDTO) {
        String updateBrandResponse = brandService.updateBrand(brandsUpdateRequestDTO);
        BaseAPIResponse<String> response = new BaseAPIResponse<>(updateBrandResponse,"Brand Updated successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedAPIResponse<BrandResponseDTO>> getBrands(BrandFilterDTO filterDTO,
                                                                            @RequestParam(defaultValue = "0") Integer page,
                                                                            @RequestParam(defaultValue = "10") Integer size
    ) {

        PaginatedAPIResponse<BrandResponseDTO> brands = brandService.getBrands(filterDTO, page, size);

        return new ResponseEntity<>(brands,HttpStatus.OK);
    }

}
