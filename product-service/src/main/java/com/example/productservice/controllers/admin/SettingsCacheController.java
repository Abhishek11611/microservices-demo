package com.example.productservice.controllers.admin;

import com.example.productservice.service.admin.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings-cache")
public class SettingsCacheController {

    private final SettingsService settingsService;

    public SettingsCacheController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCache() {

        Map<String, Object> response = new HashMap<>();

        response.put("cacheSize", settingsService.getCacheSize());

        response.put("threshold", settingsService.getInt(
                        "claims.loss_ratio_threshold", 70));

        response.put("stallHours",
                settingsService.getInt(
                        "recruitment.stall_nudge_hours", 48));

        response.put("containsThreshold",
                settingsService.containsKey(
                        "claims.loss_ratio_threshold"));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reload")
    public ResponseEntity<String> reloadCache() {

        settingsService.reload();

        return ResponseEntity.ok(
                "Cache reloaded successfully"
        );
    }
}
