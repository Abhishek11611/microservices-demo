package com.example.productservice.service.product;

import com.example.commoncore.exception.NotFoundException;
import com.example.productservice.dtos.ProductAttributeRequestDTO;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {


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

    @Transactional
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

                    ProductVariant productVariant = buildProductVariant(variant, product);

                            List<ProductMedia> mediaList = variant.getMedia().stream()
                                    .map(media ->
                                            buildProductMedia(productVariant, product, media)).toList();

                            List<VariantAttributeValue> attributeValueList = variant.getAttributes().stream()
                                    .map(attribute ->
                                            buildVariantAttributeValue(productVariant, attribute)).toList();

                            productVariant.setProductMedia(mediaList);
                            productVariant.setVariantAttributeValues(attributeValueList);

                            return productVariant;
                        }
                ).toList();

        product.setProductVariants(productVariantList);

        productRepository.save(product);

        return "Product Added SuccessFully";
    }

    private ProductMedia buildProductMedia(ProductVariant productVariant, Product product, ProductMediaRequestDTO mediaRequestDTO) {

        ProductMedia productMedia = new ProductMedia(
                mediaRequestDTO.getMediaType(),
                mediaRequestDTO.getMediaUrl(),
                mediaRequestDTO.getAltText(),
                mediaRequestDTO.getSortOrder(),
                mediaRequestDTO.getPrimary());

        productMedia.setVariant(productVariant);
        productMedia.setProduct(product);

        return productMedia;
    }

    private ProductVariant buildProductVariant(ProductVariantRequestDTO variant, Product product) {

        ProductVariant productVariant = new ProductVariant();
        productVariant.setBarcode(variant.getBarcode());
        productVariant.setSku(variant.getVariantSku());
        productVariant.setProduct(product);

        return productVariant;
    }

    private VariantAttributeValue buildVariantAttributeValue(ProductVariant productVariant, ProductAttributeRequestDTO attributeRequestDTO) {

        Attribute attribute = attributeRepository.findById(attributeRequestDTO.getAttributeId())
                .orElseThrow(() -> new NotFoundException("Attribute" + attributeRequestDTO.getAttributeId() + "Is Not Found"));

        AttributeValue attributeValue = attributeValueRepository.findByIdAndAttribute(attributeRequestDTO.getAttributeValueId(), attribute)
                .orElseThrow(() -> new NotFoundException("Attribute Value " + attributeRequestDTO.getAttributeValueId() + " Is Not Found"));

        VariantAttributeValue variantAttributeValue = new VariantAttributeValue();
        variantAttributeValue.setAttributeValue(attributeValue);
        variantAttributeValue.setProductVariant(productVariant);

        return variantAttributeValue;

    }
}
