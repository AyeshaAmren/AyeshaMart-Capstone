package com.ayeshamart.service;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.dto.LoginRequest;
import com.ayeshamart.dto.RegisterRequest;
import com.ayeshamart.exception.AuthenticationException;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Business logic for registration and login.
 * All validation happens here (server side), passwords are hashed
 * with jBCrypt, and ADMIN can never be chosen during public registration.
 */
public class AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDAO userDAO;

    public AuthService() {
        this(new UserDAO());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(RegisterRequest request) throws SQLException {
        String name = request.getName();
        String email = request.getEmail();
        String password = request.getPassword();
        String confirm = request.getConfirmPassword();
        String role = request.getRole();

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required");
        }
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        if (confirm == null || !confirm.equals(password)) {
            throw new ValidationException("Passwords do not match");
        }
        if (!"BUYER".equals(role) && !"SELLER".equals(role)) {
            throw new ValidationException("Role must be BUYER or SELLER");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userDAO.findByEmail(normalizedEmail) != null) {
            throw new ValidationException("Email is already registered");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(name.trim(), normalizedEmail, hash, role);
        return userDAO.create(user);
    }

    public User login(LoginRequest request) throws SQLException {
        String email = request.getEmail().trim().toLowerCase();

        User user = userDAO.findByEmail(email);
        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }
        return user;
    }
}