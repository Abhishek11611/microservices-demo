package com.example.productservice.service.attribute;

import com.example.productservice.dtos.AttributeCreateRequestDTO;
import com.example.productservice.dtos.AttributeResponseDTO;

public interface AttributeService {

  AttributeResponseDTO createAttribute(AttributeCreateRequestDTO attributeCreateRequestDTO);

}
