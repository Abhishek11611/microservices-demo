package com.example.productservice.service.admin;

import com.example.productservice.entities.admin.AdminSetting;
import com.example.productservice.repositories.admin.AdminSettingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SettingsService {

    private final AdminSettingRepository repository;

    private final ObjectMapper objectMapper;

    private final AtomicReference<Map<String, JsonNode>> cache =
            new AtomicReference<>(Map.of());

    public SettingsService(
            AdminSettingRepository repository,
            ObjectMapper objectMapper) {

        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    // Application startup
//    @EventListener(ApplicationReadyEvent.class)
//    public void initialize() {
//
//        reload();
//    }

    // Database -> Cache
    public synchronized void reload() {

        System.out.println("Starting admin settings cache reload");

        List<AdminSetting> settings = repository.findAll();

        Map<String, JsonNode> fresh = new HashMap<>();

        for (AdminSetting setting : settings) {

            fresh.put(
                    setting.getSettingKey(),
                    setting.getValue()
            );
        }

        cache.set(Collections.unmodifiableMap(fresh));

        System.out.println("Admin settings cache loaded successfully. Total keys: {}"+
                fresh.size()
        );

        System.out.println("Cached setting keys: {}"+ fresh.keySet());
    }

    // Read integer
    public int getInt(String key, int defaultValue) {

        JsonNode value = cache.get().get(key);

        return value == null ? defaultValue : value.asInt();
    }

    // Read String
    public String getString(String key, String defaultValue) {

        JsonNode value = cache.get().get(key);

        return value == null ? defaultValue : value.asText();
    }

    // Read Boolean
    public boolean getBoolean(String key, boolean defaultValue) {

        JsonNode value = cache.get().get(key);

        return value == null ? defaultValue : value.asBoolean();
    }

    // Read List
    public <T> List<T> getList(
            String key,
            Class<T> elementType) {

        JsonNode value = cache.get().get(key);

        if (value == null || !value.isArray()) {
            return List.of();
        }

        return objectMapper.convertValue(
                value,
                objectMapper.getTypeFactory()
                        .constructCollectionType(
                                List.class,
                                elementType
                        )
        );
    }

    // Cache size
    public int getCacheSize() {

        return cache.get().size();
    }

    // Check key
    public boolean containsKey(String key) {

        return cache.get().containsKey(key);
    }

    // Get raw cached value
    public JsonNode getValue(String key) {

        return cache.get().get(key);
    }
}

