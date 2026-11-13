package com.example.productservice.entities.admin;

import com.example.productservice.enums.SettingValueType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_setting")
public class AdminSetting {

    @Id
    @Column(name = "setting_key", length = 100)
    private String settingKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb",
            nullable = false)
    private JsonNode value;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    private SettingValueType valueType;

    @Column(name = "module", nullable = false)
    private String module;

    @Column(name = "description")
    private String description;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public AdminSetting() {
    }

    public AdminSetting(String settingKey, JsonNode value, SettingValueType valueType, String module, String description, Integer version, String updatedBy, OffsetDateTime updatedAt) {
        this.settingKey = settingKey;
        this.value = value;
        this.valueType = valueType;
        this.module = module;
        this.description = description;
        this.version = version;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
