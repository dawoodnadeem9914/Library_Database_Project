package com.unimas.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** USERS table - system accounts (ADMIN / LIBRARIAN / STAFF / MEMBER) with lockout state. */
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "USER_SEQ", allocationSize = 1)
    @Column(name = "USER_ID")
    private Long id;

    @Size(max = 120)
    @Column(name = "FULL_NAME", length = 120)
    private String fullName;

    @NotBlank @Size(min = 3, max = 50)
    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;          // BCrypt hash

    @NotBlank @Email
    @Column(name = "EMAIL", nullable = false, length = 120)
    private String email;

    @NotBlank
    @Column(name = "ROLE", nullable = false, length = 20)
    private String role;              // ADMIN / LIBRARIAN / STAFF / MEMBER

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = Boolean.TRUE;

    @Column(name = "LAST_LOGIN")
    private LocalDateTime lastLogin;

    @Size(max = 500)
    @Column(name = "AVATAR_URL", length = 500)
    private String avatarUrl;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = Boolean.TRUE;
    }

    public User() { }
    public User(String username, String password, String email, String role) {
        this.username = username; this.password = password; this.email = email; this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
