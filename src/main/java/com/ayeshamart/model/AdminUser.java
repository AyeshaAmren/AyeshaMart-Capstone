package com.ayeshamart.model;

import java.time.LocalDateTime;

/**
 * Read-only user view for the Admin "Manage Users" page (Phase 7).
 *
 * <p>Deliberately has no password field, so a password hash can never
 * leak into an admin page by accident.
 */
public class AdminUser {

    private long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}