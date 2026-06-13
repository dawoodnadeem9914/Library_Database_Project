package com.unimas.library.controller;

import com.unimas.library.dto.RegisterForm;
import com.unimas.library.service.FileStorageService;
import com.unimas.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/** Login, registration (with avatar upload) and live username availability. */
@Controller
public class AuthController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public AuthController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    // ==================== Registration ====================

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form,
                           BindingResult result,
                           @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                           RedirectAttributes ra) {
        if (form.getPassword() != null && form.getConfirmPassword() != null
                && !form.passwordsMatch()) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (result.hasErrors()) {
            return "register";
        }
        try {
            String avatarUrl = fileStorageService.store(avatarFile, "avatars");
            userService.register(form, avatarUrl);
        } catch (IllegalStateException e) {
            result.reject("registerError", e.getMessage());
            return "register";
        }
        ra.addFlashAttribute("registered", true);
        return "redirect:/login?registered";
    }

    /** Live username availability for the registration page. */
    @GetMapping("/api/username-available")
    @ResponseBody
    public Map<String, Boolean> usernameAvailable(@RequestParam("u") String username) {
        boolean ok = username != null && username.length() >= 3 && !userService.exists(username.trim());
        return Map.of("available", ok);
    }

    // ==================== Forgot password (contact admin) ====================

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
}
