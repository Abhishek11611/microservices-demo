package com.example.productservice.service.product;

import com.example.commoncore.exception.NotFoundException;
import com.example.productservice.dtos.ProductMediaRequestDTO;
import com.example.productservice.dtos.ProductRequestDTO;
import com.example.productservice.dtos.ProductVariantRequestDTO;
import com.example.productservice.entities.attribute.Attribute;
import com.example.productservice.entities.attribute.AttributeValue;
import com.example.productservice.entities.attribute.VariantAttributeValue;
import com.example.productservice.entities.brand.Brands;
import com.example.productservice.entities.product.*;
import com.example.productservice.exceptions.AlreadyExistsException;
import com.example.productservice.repositories.Attribute.AttributeRepository;
import com.example.productservice.repositories.Attribute.AttributeValueRepository;
import com.example.productservice.repositories.brands.BrandRepository;
import com.example.productservice.repositories.product.CategoryRepository;
import com.example.productservice.repositories.product.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class ProductServiceImpl implements ProductService{


    private final AttributeRepository attributeRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(AttributeRepository attributeRepository, BrandRepository brandRepository, ProductRepository productRepository, CategoryRepository categoryRepository, AttributeValueRepository attributeValueRepository, ObjectMapper objectMapper) {
        this.attributeRepository = attributeRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createProduct(ProductRequestDTO productRequestDTO) {

        if (productRepository.existsByProductCode(productRequestDTO.getProductCode())) {
            throw new AlreadyExistsException("Product Code Already Exists");
        }

        Product product = new Product();

        List<Category> categories = categoryRepository.findAllById(productRequestDTO.getCategoryIds());

        Brands brand = brandRepository.findByBrandCode(productRequestDTO.getBrandCode())
                .orElseThrow(() -> new NotFoundException("Brand Code Not Found"));

        if (categories.size() != productRequestDTO.getCategoryIds().stream().distinct().count()) {
            throw new NotFoundException("One or more categories were not found");
        }

        List<ProductCategory> productCategories = categories.stream()
                .map(c -> new ProductCategory(product, c)).toList();

        product.setName(productRequestDTO.getName());
        product.setProductCode(productRequestDTO.getProductCode());
        product.setSlug(productRequestDTO.getSlug());
        product.setDescription(productRequestDTO.getDescription());
        product.setProductCategories(productCategories);
        product.setBrands(brand);


        List<ProductVariant> productVariantList = productRequestDTO.getVariants().stream()
                .map(variant -> {
                    ProductVariant productVariant = new ProductVariant();
                    productVariant.setBarcode(variant.getBarcode());
                    productVariant.setSku(variant.getVariantSku());

                    List<ProductMedia> productMediaList = variant.getMedia().stream().map(
                            m -> {
                                ProductMedia productMedia = new ProductMedia(
                                        m.getMediaType(),
                                        m.getMediaUrl(),
                                        m.getAltText(),
                                        m.getSortOrder(),
                                        m.getPrimary() );

                                productMedia.setVariant(productVariant);

                                return productMedia;
                            }

                    ).toList();

                    productVariant.setProductMedia(productMediaList);

                    List<VariantAttributeValue> VariantAttributeValueList = variant.getAttributes().stream()
                            .map(a -> {
                                Attribute attribute = attributeRepository.findById(a.getAttributeId())
                                        .orElseThrow(() -> new NotFoundException("Attribute" + a.getAttributeId() + "Is Not Found"));

                                AttributeValue attributeValue = attributeValueRepository.findByIdAndAttribute(a.getAttributeValueId(), attribute)
                                        .orElseThrow(() -> new NotFoundException("Attribute Value " + a.getAttributeValueId() + " Is Not Found"));

                                VariantAttributeValue variantAttributeValue = new VariantAttributeValue();
                                variantAttributeValue.setAttributeValue(attributeValue);
                                variantAttributeValue.setProductVariant(productVariant);

                                return variantAttributeValue;

                            }).toList();

                    productVariant.setVariantAttributeValues(VariantAttributeValueList);

                    return productVariant;
                }).toList();

        product.setProductVariants(productVariantList);

      productRepository.save(product);

        return "Product Added SuccessFully";
    }
}
