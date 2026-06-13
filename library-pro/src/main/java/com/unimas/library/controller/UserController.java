package com.unimas.library.controller;

import com.unimas.library.dto.ManageUserForm;
import com.unimas.library.entity.User;
import com.unimas.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** User Management module - ADMIN only (enforced in SecurityConfig). */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String role,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("users", userService.search(keyword, role, status, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ManageUserForm());
        model.addAttribute("formAction", "/users");
        model.addAttribute("pageTitle", "Create User");
        return "users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ManageUserForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/users");
            model.addAttribute("pageTitle", "Create User");
            return "users/form";
        }
        try {
            userService.createByAdmin(form);
            ra.addFlashAttribute("success", "User " + form.getUsername() + " created.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User u = userService.findById(id);
        ManageUserForm form = new ManageUserForm();
        form.setId(u.getId());
        form.setFullName(u.getFullName());
        form.setUsername(u.getUsername());
        form.setEmail(u.getEmail());
        form.setRole(u.getRole());
        form.setActive(Boolean.TRUE.equals(u.getActive()));
        model.addAttribute("form", form);
        model.addAttribute("formAction", "/users/" + id);
        model.addAttribute("pageTitle", "Edit User");
        return "users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ManageUserForm form,
                         BindingResult result, Model model, Authentication auth, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/users/" + id);
            model.addAttribute("pageTitle", "Edit User");
            return "users/form";
        }
        try {
            userService.updateByAdmin(id, form, auth.getName());
            ra.addFlashAttribute("success", "User updated.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            userService.toggleActive(id, auth.getName());
            ra.addFlashAttribute("success", "User status changed.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam("newPassword") String newPassword,
                                RedirectAttributes ra) {
        try {
            userService.resetPassword(id, newPassword);
            ra.addFlashAttribute("success", "Password reset successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            userService.delete(id, auth.getName());
            ra.addFlashAttribute("success", "User deleted.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }
}
