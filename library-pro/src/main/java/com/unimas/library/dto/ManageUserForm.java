package com.unimas.library.dto;

import jakarta.validation.constraints.*;

/** Admin user-management form (create + edit; password optional on edit). */
public class ManageUserForm {

    private Long id;

    @NotBlank(message = "Full name is required") @Size(max = 120)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Only letters, digits, dot, dash and underscore")
    private String username;

    @NotBlank(message = "Email is required") @Email
    private String email;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|LIBRARIAN|STAFF|MEMBER")
    private String role;

    private boolean active = true;

    /** Required on create; blank on edit = keep current password. */
    private String password;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
