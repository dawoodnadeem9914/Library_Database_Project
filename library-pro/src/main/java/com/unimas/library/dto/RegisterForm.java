package com.unimas.library.dto;

import jakarta.validation.constraints.*;

/** Registration form with password confirmation. */
public class RegisterForm {

    @NotBlank(message = "Full name is required") @Size(max = 120)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Only letters, digits, dot, dash and underscore")
    private String username;

    @NotBlank(message = "Email is required") @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
             message = "Must contain uppercase, lowercase, a number and a special character")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotBlank(message = "Please choose a role")
    @Pattern(regexp = "STAFF|MEMBER", message = "Role must be Staff or Member")
    private String role;

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
