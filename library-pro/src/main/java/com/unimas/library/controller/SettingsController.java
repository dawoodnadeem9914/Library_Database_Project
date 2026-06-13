package com.unimas.library.controller;

import com.unimas.library.entity.Setting;
import com.unimas.library.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Settings module - ADMIN only (enforced in SecurityConfig). */
@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("setting", settingsService.get());
        return "settings";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("setting") Setting setting,
                       BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "settings";
        }
        settingsService.update(setting);
        ra.addFlashAttribute("success", "Settings saved successfully.");
        return "redirect:/settings";
    }
}
