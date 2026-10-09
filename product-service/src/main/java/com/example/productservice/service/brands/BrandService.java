package com.example.productservice.service.brands;

import com.example.productservice.dtos.*;
import com.example.productservice.entities.brand.Brands;

public interface BrandService {

   String createBrand(BrandsRequestDTO brandsRequestDTO);

   String updateBrand(BrandsUpdateRequestDTO brandsUpdateRequestDTO);

   PaginatedAPIResponse<BrandResponseDTO> getBrands(BrandFilterDTO filterDTO, Integer page, Integer size);

}
