package com.unimas.library.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Replaces the Whitelabel Error Page for errors raised outside controllers
 * (bad URLs, filter-level failures). Routes by HTTP status code.
 */
@Controller
public class ErrorPageController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorPageController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        int code = status != null ? Integer.parseInt(status.toString()) : 500;
        log.warn("Error {} for URI {}", code, uri);
        return switch (code) {
            case 404 -> "error/404";
            case 403 -> "error/403";
            default -> "error/500";
        };
    }

    /** Spring Security redirects here when a role check fails. */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
