package com.unimas.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Global exception handling.
 *
 * IMPORTANT: NoResourceFoundException (missing static files like favicon.ico,
 * missing avatars, etc.) must NEVER return an HTML page. If an HTML body is
 * written during a page load, it corrupts the DOM and breaks Bootstrap modal
 * JavaScript initialization — causing the "screen goes dull / nothing works"
 * symptom on the Users delete modal.
 *
 * The fix: set status 404 and write zero bytes. Do NOT call sendError() because
 * that triggers Tomcat's error dispatch which re-routes through /error and
 * renders an HTML page anyway.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Missing static resources (favicon.ico, avatar images, etc.)
     * Return an empty 404 — no HTML body — so the browser handles it silently.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void noResourceFound(NoResourceFoundException ex,
                                HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentLength(0);
        response.getOutputStream().flush();
    }

    /** Bad IDs in URLs (book/member/loan not found) -> 404 page. */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(IllegalArgumentException ex, Model model) {
        log.warn("Not found: {}", ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    /** Spring Security access violations -> 403 page. */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied(AccessDeniedException ex, Model model) {
        log.warn("Access denied: {}", ex.getMessage());
        return "error/403";
    }

    /** Database connectivity / SQL problems -> database error page. */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String databaseError(DataAccessException ex, Model model) {
        log.error("Database error", ex);
        return "error/database";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String uploadTooLarge(MaxUploadSizeExceededException ex, Model model) {
        log.warn("Upload too large: {}", ex.getMessage());
        model.addAttribute("message", "The uploaded file is too large (maximum 5 MB).");
        return "error/500";
    }

    /** Anything else -> 500 page. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String serverError(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("message", "An unexpected error occurred.");
        return "error/500";
    }
}
