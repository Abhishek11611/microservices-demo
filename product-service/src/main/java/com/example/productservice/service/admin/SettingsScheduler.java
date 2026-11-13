package com.example.productservice.service.admin;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SettingsScheduler {

    private final SettingsService settingsService;

    public SettingsScheduler(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

//    @Scheduled(fixedDelay = 600000)
    public void refreshSettings() {

        System.out.println("Scheduled settings refresh started");

        settingsService.reload();
    }
}
