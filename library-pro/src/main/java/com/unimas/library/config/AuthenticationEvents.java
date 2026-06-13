package com.unimas.library.config;

import com.unimas.library.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** On a successful login, stamp the user's last-login time. */
@Component
public class AuthenticationEvents {

    private final UserService userService;

    public AuthenticationEvents(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        userService.loginSucceeded(event.getAuthentication().getName());
    }
}
