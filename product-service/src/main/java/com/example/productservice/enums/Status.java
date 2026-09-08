package com.example.productservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum Status {
    ACTIVE('Y'),
    INACTIVE('N');

    private final Character value;

    Status(Character value) {
        this.value = value;
    }
    @JsonValue
    public Character getValue() {
        return value;
    }
    @JsonCreator
    public static Status fromValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        for (Status status : Status.values()) {
            if (status.getValue().toString().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }

    @Converter(autoApply = true)
    public static class StatusConverter implements AttributeConverter<Status, Character> {
        @Override
        public Character convertToDatabaseColumn(Status status) {
            return status != null ? status.getValue() : null;
        }

        @Override
        public Status convertToEntityAttribute(Character dbData) {
            return dbData != null ? Status.fromValue(dbData.toString()) : null;
        }
    }
}

