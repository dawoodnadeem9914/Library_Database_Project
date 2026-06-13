package com.unimas.library.dto;

import jakarta.validation.constraints.*;

/** Form object for creating system users (keeps raw password out of the entity). */
public class UserForm {

    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
