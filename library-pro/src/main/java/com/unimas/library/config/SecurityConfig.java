package com.unimas.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Role matrix (CSRF protection stays ON - Thymeleaf injects tokens automatically):
 *
 *  ROLE_ADMIN     - everything: books/members/loans CRUD, deletes, loan date edits,
 *                   user management, settings, audit logs, reports
 *  ROLE_LIBRARIAN - legacy senior-staff role: manage books/members/loans (no deletes,
 *                   no users/settings)
 *  ROLE_STAFF     - dashboard, borrow/return, add/edit members, view reports & exports
 *  ROLE_MEMBER    - dashboard + read-only catalogue + own profile only
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Sends deactivated accounts to a dedicated message on the login page. */
    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String target = (exception instanceof DisabledException)
                    ? "/login?error=disabled" : "/login?error";
            response.sendRedirect(request.getContextPath() + target);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationFailureHandler loginFailureHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // public
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**",
                                 "/webjars/**", "/error", "/access-denied").permitAll()
                .requestMatchers("/login", "/register", "/forgot-password",
                                 "/api/username-available").permitAll()

                // ADMIN only
                .requestMatchers("/settings/**", "/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,
                        "/books/*/delete", "/members/*/delete", "/loans/*/delete").hasRole("ADMIN")
                .requestMatchers("/loans/*/edit").hasRole("ADMIN")

                // borrow / return: staff level and up
                .requestMatchers("/loans/new", "/loans/*/return").hasAnyRole("ADMIN", "LIBRARIAN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/loans").hasAnyRole("ADMIN", "LIBRARIAN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/loans/*").hasRole("ADMIN")   // loan date edits
                .requestMatchers("/loans/**").hasAnyRole("ADMIN", "LIBRARIAN", "STAFF")

                // book management: librarian and admin (members/staff may browse the catalogue)
                .requestMatchers("/books/new", "/books/*/edit").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.POST, "/books", "/books/*").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers("/books/export/**").hasAnyRole("ADMIN", "LIBRARIAN", "STAFF")

                // member records: staff level and up
                .requestMatchers("/members/**").hasAnyRole("ADMIN", "LIBRARIAN", "STAFF")

                // reports: staff level and up
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "LIBRARIAN", "STAFF")

                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(loginFailureHandler)
                .permitAll())
            .rememberMe(remember -> remember
                .key("unimas-library-remember-key")
                .tokenValiditySeconds(14 * 24 * 60 * 60))
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?expired"))
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied"))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll());
        return http.build();
    }
}
