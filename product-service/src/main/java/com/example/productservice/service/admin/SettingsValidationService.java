package com.example.productservice.service.admin;

import com.example.productservice.enums.SettingValueType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class SettingsValidationService {

    public void validate(JsonNode value, SettingValueType valueType) {

        if (value == null || value.isNull()) {
            throw new IllegalArgumentException(
                    "Setting value cannot be null"
            );
        }

        boolean valid = switch (valueType) {

            case NUMBER ->
                    value.isNumber();

            case STRING ->
                    value.isTextual();

            case BOOLEAN ->
                    value.isBoolean();

            case LIST ->
                    value.isArray();

            case OBJECT ->
                    value.isObject();
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid value type. Expected: " + valueType
            );
        }
    }
}
