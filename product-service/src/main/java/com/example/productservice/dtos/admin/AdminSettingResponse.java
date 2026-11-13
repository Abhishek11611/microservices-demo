package com.example.productservice.dtos.admin;

import com.example.productservice.enums.SettingValueType;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record AdminSettingResponse(

        String settingKey,

        JsonNode value,

        SettingValueType valueType,

        String module,

        String description,

        Integer version,

        String updatedBy,

        OffsetDateTime updatedAt

) {}
