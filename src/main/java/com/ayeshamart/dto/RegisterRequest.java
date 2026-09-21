package com.ayeshamart.dto;

/**
 * Payload of the registration form.
 * Raw values are validated inside AuthService.
 */
public class RegisterRequest {

    private final String name;
    private final String email;
    private final String password;
    private final String confirmPassword;
    private final String role;

    public RegisterRequest(String name, String email, String password, String confirmPassword, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getRole() {
        return role;
    }
}