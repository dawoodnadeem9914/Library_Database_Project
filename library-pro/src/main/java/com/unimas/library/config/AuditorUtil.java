package com.unimas.library.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Resolves the current username for entity auditing columns. */
public final class AuditorUtil {
    private AuditorUtil() { }

    public static String currentUser() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            return (a != null && a.isAuthenticated()) ? a.getName() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}
