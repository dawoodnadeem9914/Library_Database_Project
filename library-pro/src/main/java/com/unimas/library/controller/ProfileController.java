package com.unimas.library.controller;

import com.unimas.library.entity.Member;
import com.unimas.library.entity.User;
import com.unimas.library.repository.MemberRepository;
import com.unimas.library.service.FileStorageService;
import com.unimas.library.service.LoanService;
import com.unimas.library.service.UserService;
import com.unimas.library.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/** "My Profile" - every signed-in user can view AND edit their own account. */
@Controller
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final LoanService loanService;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService,
                             UserRepository userRepository,
                             MemberRepository memberRepository,
                             LoanService loanService,
                             FileStorageService fileStorageService,
                             PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.loanService = loanService;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName());
        model.addAttribute("user", user);

        Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
        model.addAttribute("member", member);
        if (member != null) {
            model.addAttribute("myLoans", member.getLoans());
            model.addAttribute("activeLoanCount", loanService.countActiveForMember(member.getId()));
            model.addAttribute("totalFines", loanService.totalFines(member.getLoans()));
        } else {
            model.addAttribute("myLoans", List.of());
            model.addAttribute("activeLoanCount", 0L);
            model.addAttribute("totalFines", BigDecimal.ZERO);
        }
        return "profile";
    }

    /** Update full name and/or email */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String fullName,
                                @RequestParam String email,
                                Authentication auth,
                                RedirectAttributes ra) {
        User user = userService.findByUsername(auth.getName());

        // Check email not taken by someone else
        userRepository.findByEmail(email).ifPresent(other -> {
            if (!other.getId().equals(user.getId()))
                throw new IllegalStateException("Email " + email + " is already in use.");
        });

        user.setFullName(fullName != null && !fullName.isBlank() ? fullName.trim() : null);
        user.setEmail(email.trim());
        userRepository.save(user);
        ra.addFlashAttribute("success", "Profile updated successfully.");
        return "redirect:/profile";
    }

    /** Upload / change avatar photo */
    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
                               Authentication auth,
                               RedirectAttributes ra) {
        User user = userService.findByUsername(auth.getName());
        try {
            String url = fileStorageService.store(avatar, "avatars");
            if (url != null) {
                user.setAvatarUrl(url);
                userRepository.save(user);
                ra.addFlashAttribute("success", "Profile photo updated.");
            }
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    /** Change own password */
    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        User user = userService.findByUsername(auth.getName());

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/profile";
        }
        if (newPassword.length() < 8) {
            ra.addFlashAttribute("error", "New password must be at least 8 characters.");
            return "redirect:/profile";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        ra.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/profile";
    }
}
