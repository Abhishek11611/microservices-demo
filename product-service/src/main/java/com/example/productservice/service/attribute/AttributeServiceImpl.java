package com.example.productservice.service.attribute;

import com.example.productservice.dtos.AttributeCreateRequestDTO;
import com.example.productservice.dtos.AttributeResponseDTO;
import com.example.productservice.dtos.AttributeValueResponseDTO;
import com.example.productservice.entities.attribute.Attribute;
import com.example.productservice.entities.attribute.AttributeValue;
import com.example.productservice.enums.Status;
import com.example.productservice.exceptions.AlreadyExistsException;
import com.example.productservice.repositories.Attribute.AttributeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttributeServiceImpl implements AttributeService{

    private final AttributeRepository attributeRepository;

    public AttributeServiceImpl(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    @Override
    @Transactional
    public AttributeResponseDTO createAttribute(AttributeCreateRequestDTO attributeCreateRequestDTO) {

        if (attributeCreateRequestDTO.getName()!= null && attributeRepository.existsByNameAndStatus(attributeCreateRequestDTO.getName(), Status.ACTIVE)){
            throw new AlreadyExistsException("Attribute Already Present");
        }

        Attribute attribute = new Attribute();

        attribute.setName(attributeCreateRequestDTO.getName());
        attribute.setCode(attributeCreateRequestDTO.getName().toLowerCase());

        List<AttributeValue> attributeValueList =
                 attributeCreateRequestDTO.getAttributeValues()
                .stream()
                .map(attributeValueDTO -> {
                    AttributeValue attributeValue = new AttributeValue();
                    attributeValue.setValue(attributeValueDTO.getValue());
                    attributeValue.setCode(attributeValueDTO.getValue().toLowerCase());
                    attributeValue.setSortOrder(attributeValueDTO.getSortOrder());
                    attributeValue.setAttribute(attribute);
                    return attributeValue;
                }).toList();

        attribute.setAttributeValues(attributeValueList);
        Attribute savedAttribute = attributeRepository.save(attribute);

        List<AttributeValueResponseDTO> attributeValueResponseDTO = savedAttribute.getAttributeValues().stream()
                .map(attributeValue -> {
                    return new AttributeValueResponseDTO(attributeValue.getId(),
                            attributeValue.getCode(), attributeValue.getValue(), attributeValue.getSortOrder());
                }).toList();

        return new AttributeResponseDTO(
                savedAttribute.getId(),savedAttribute.getCode(),savedAttribute.getName(),
                attributeValueResponseDTO
        );
    }
}
