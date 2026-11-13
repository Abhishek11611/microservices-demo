package com.example.productservice.dtos.admin;

import com.example.productservice.enums.SettingValueType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminSettingRequest {

    @NotBlank
    private String settingKey;

    @NotNull
    private JsonNode value;

    @NotNull
    private SettingValueType valueType;

    public AdminSettingRequest() {
    }

    public AdminSettingRequest(String settingKey, JsonNode value, SettingValueType valueType) {
        this.settingKey = settingKey;
        this.value = value;
        this.valueType = valueType;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public JsonNode getValue() {
        return value;
    }

    public void setValue(JsonNode value) {
        this.value = value;
    }

    public SettingValueType getValueType() {
        return valueType;
    }

    public void setValueType(SettingValueType valueType) {
        this.valueType = valueType;
    }
}
