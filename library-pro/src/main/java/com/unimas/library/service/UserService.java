package com.unimas.library.service;

import com.unimas.library.dto.ManageUserForm;
import com.unimas.library.dto.RegisterForm;
import com.unimas.library.dto.UserForm;
import com.unimas.library.entity.User;
import com.unimas.library.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bridges the USERS table to Spring Security and implements:
 *  - BCrypt password storage
 *  - account activation / deactivation
 *  - full admin user management (create / edit / delete / reset password / roles)
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== Spring Security ====================

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(), u.getPassword(),
                Boolean.TRUE.equals(u.getActive()),   // enabled (active flag)
                true, true, true,                     // accountNonExpired, credentialsNonExpired, accountNonLocked
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())));
    }

    /** Called by the login success listener - just stamps the last-login time. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loginSucceeded(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setLastLogin(LocalDateTime.now());
            userRepository.save(u);
        });
    }

    // ==================== Registration ====================

    public User register(RegisterForm form, String avatarUrl) {
        if (!form.passwordsMatch()) {
            throw new IllegalStateException("Passwords do not match.");
        }
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new IllegalStateException("Username \"" + form.getUsername() + "\" is already taken.");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalStateException("Email " + form.getEmail() + " is already registered.");
        }
        User user = new User(form.getUsername(),
                passwordEncoder.encode(form.getPassword()),
                form.getEmail(), form.getRole());
        user.setFullName(form.getFullName());
        user.setAvatarUrl(avatarUrl);
        User saved = userRepository.save(user);
        return saved;
    }

    // ==================== Admin user management ====================

    @Transactional(readOnly = true)
    public Page<User> search(String keyword, String role, String status, int page) {
        return userRepository.search(norm(keyword), norm(role), norm(status),
                PageRequest.of(Math.max(page, 0), 8, Sort.by("username").ascending()));
    }

    private static String norm(String s) {
        if (s == null) return null;
        String t = s.trim();
        return (t.isEmpty() || t.equalsIgnoreCase("null")) ? null : t;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    public User createByAdmin(ManageUserForm form) {
        if (form.getPassword() == null || form.getPassword().length() < 8) {
            throw new IllegalStateException("Password must be at least 8 characters.");
        }
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new IllegalStateException("Username \"" + form.getUsername() + "\" is already taken.");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalStateException("Email " + form.getEmail() + " is already registered.");
        }
        User user = new User(form.getUsername(),
                passwordEncoder.encode(form.getPassword()), form.getEmail(), form.getRole());
        user.setFullName(form.getFullName());
        user.setActive(form.isActive());
        User saved = userRepository.save(user);
        return saved;
    }

    public User updateByAdmin(Long id, ManageUserForm form, String actor) {
        User user = findById(id);
        userRepository.findByUsername(form.getUsername()).ifPresent(other -> {
            if (!other.getId().equals(id))
                throw new IllegalStateException("Username \"" + form.getUsername() + "\" is already taken.");
        });
        userRepository.findByEmail(form.getEmail()).ifPresent(other -> {
            if (!other.getId().equals(id))
                throw new IllegalStateException("Email " + form.getEmail() + " is already registered.");
        });
        if (user.getUsername().equals(actor) && !"ADMIN".equals(form.getRole())) {
            throw new IllegalStateException("You cannot remove your own administrator role.");
        }
        boolean roleChanged = !user.getRole().equals(form.getRole());
        user.setFullName(form.getFullName());
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        user.setActive(form.isActive());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            if (form.getPassword().length() < 8)
                throw new IllegalStateException("Password must be at least 8 characters.");
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        User saved = userRepository.save(user);
        return saved;
    }

    public void toggleActive(Long id, String actor) {
        User user = findById(id);
        if (user.getUsername().equals(actor)) {
            throw new IllegalStateException("You cannot deactivate your own account.");
        }
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        userRepository.save(user);
    }

    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalStateException("Password must be at least 8 characters.");
        }
        User user = findById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void delete(Long id, String actor) {
        User user = findById(id);
        if (user.getUsername().equals(actor)) {
            throw new IllegalStateException("You cannot delete your own account.");
        }
        if ("ADMIN".equals(user.getRole()) && userRepository.countByRole("ADMIN") <= 1) {
            throw new IllegalStateException("Cannot delete the last administrator account.");
        }
        userRepository.delete(user);
    }

    // ==================== misc ====================

    public User create(UserForm form) {   // kept for the DataSeeder (backward compatible)
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new IllegalStateException("Username already exists: " + form.getUsername());
        }
        User user = new User(form.getUsername(),
                passwordEncoder.encode(form.getPassword()),
                form.getEmail(), form.getRole());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username) { return userRepository.existsByUsername(username); }

}
