package com.example.productservice.controllers.Attribute;

import com.example.commoncore.dto.BaseAPIResponse;
import com.example.productservice.dtos.AttributeCreateRequestDTO;
import com.example.productservice.dtos.AttributeResponseDTO;
import com.example.productservice.dtos.BrandsRequestDTO;
import com.example.productservice.service.attribute.AttributeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/attribute")
public class AttributeController {

    private final AttributeService attributeService;

    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @PostMapping("/create")
    public ResponseEntity<BaseAPIResponse<AttributeResponseDTO>> createAttribute(@RequestBody AttributeCreateRequestDTO attributeCreateRequestDTO) {
        AttributeResponseDTO attribute = attributeService.createAttribute(attributeCreateRequestDTO);
        BaseAPIResponse<AttributeResponseDTO> response = new BaseAPIResponse<>(attribute,"Attribute Created successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
