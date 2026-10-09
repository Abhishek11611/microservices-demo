package com.example.productservice.service.brands;

import com.example.productservice.Specification.BrandsSpecification;
import com.example.productservice.dtos.*;
import com.example.productservice.entities.brand.Brands;
import com.example.productservice.exceptions.AlreadyExistsException;
import com.example.productservice.exceptions.NotFoundException;
import com.example.productservice.repositories.brands.BrandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

    @Override
    public String updateBrand(BrandsUpdateRequestDTO brandsUpdateRequestDTO) {

        if (brandsUpdateRequestDTO.getBrandCode() == null){
            throw new NotFoundException("Please Enter Brand Code");
        }

        if (brandsUpdateRequestDTO.getBrandCode() != null &&
                !brandRepository.existsByBrandCode(brandsUpdateRequestDTO.getBrandCode())){
            throw new NotFoundException("Brand Code Not Found");
        }

        Brands brand = brandRepository.findByBrandCode(brandsUpdateRequestDTO.getBrandCode());

        brand.setName(brandsUpdateRequestDTO.getName());
        brand.setDescription(brandsUpdateRequestDTO.getDescription());

        brandRepository.save(brand);
        return "Brand Updated SuccessFully";
    }

    public PaginatedAPIResponse<BrandResponseDTO> getBrands(BrandFilterDTO filterDTO, Integer page, Integer size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Specification<Brands> specification =
                BrandsSpecification.getSpecification(filterDTO);

        Page<Brands> brandsPage =
                brandRepository.findAll(specification, pageable);

        List<BrandResponseDTO> data = brandsPage.getContent()
                .stream()
                .map(brand -> new BrandResponseDTO(
                        brand.getBrandCode(),
                        brand.getName(),
                        brand.getSlug(),
                        brand.getDescription()
                ))
                .toList();

        return new PaginatedAPIResponse<>(
                "Brands fetched successfully",
                true,
                data,
                brandsPage.getNumber(),
                brandsPage.getTotalPages(),
                brandsPage.getTotalElements(),
                brandsPage.getSize(),
                brandsPage.isLast()
        );
    }



}
