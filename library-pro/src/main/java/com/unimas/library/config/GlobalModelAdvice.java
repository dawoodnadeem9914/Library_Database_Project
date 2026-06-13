package com.unimas.library.config;

import com.unimas.library.entity.Setting;
import com.unimas.library.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects system settings into every view (library name, theme).
 * Falls back to defaults if the database is unreachable so error
 * pages can still render.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAdvice.class);

    private final SettingsService settingsService;

    public GlobalModelAdvice(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @ModelAttribute("appSettings")
    public Setting appSettings() {
        try {
            return settingsService.get();
        } catch (Exception e) {
            log.warn("Could not load settings ({}); using defaults", e.getMessage());
            return new Setting();
        }
    }
}
