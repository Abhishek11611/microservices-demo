package com.example.productservice.service.brands;

import com.example.productservice.dtos.BrandsRequestDTO;
import com.example.productservice.entities.brand.Brands;
import com.example.productservice.exceptions.AlreadyExistsException;
import com.example.productservice.repositories.brands.BrandRepository;

public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    public BrandServiceImpl(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }


    @Override
    public String createBrand(BrandsRequestDTO brandsRequestDTO) {

        boolean isBrandExists = brandRepository.existsByName(brandsRequestDTO.getName());

        if (brandsRequestDTO.getName() != null && isBrandExists){
            throw new AlreadyExistsException("Brand Already Exists");
        }

        if (brandsRequestDTO.getSlug() != null && brandRepository.existsBySlug(brandsRequestDTO.getSlug())){
            throw new AlreadyExistsException("Slug Already Exists");
        }

        Brands brands = new Brands();

        brands.setName(brandsRequestDTO.getName());
        brands.setBrandCode(brandsRequestDTO.getName().toUpperCase()+ "-01");
        brands.setDescription(brandsRequestDTO.getDescription());
        brands.setSlug(brandsRequestDTO.getSlug());

        brandRepository.save(brands);

        return "Brand Added SuccessFully";
    }
}
