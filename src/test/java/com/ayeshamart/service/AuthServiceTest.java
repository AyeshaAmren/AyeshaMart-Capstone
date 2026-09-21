package com.ayeshamart.service;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.dto.LoginRequest;
import com.ayeshamart.dto.RegisterRequest;
import com.ayeshamart.exception.AuthenticationException;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDAO);
    }

    private RegisterRequest valid(String role) {
        return new RegisterRequest("Ayesha", "ayesha@example.com", "Secret1", "Secret1", role);
    }

    @Test
    void registersBuyerWithHashedPassword() throws Exception {
        when(userDAO.findByEmail("ayesha@example.com")).thenReturn(null);

        authService.register(valid("BUYER"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).create(captor.capture());
        User stored = captor.getValue();
        assertEquals("ayesha@example.com", stored.getEmail());
        assertEquals("BUYER", stored.getRole());
        assertNotEquals("Secret1", stored.getPasswordHash(), "password must never be stored in plaintext");
        assertTrue(BCrypt.checkpw("Secret1", stored.getPasswordHash()), "stored hash must verify with bcrypt");
    }

    @Test
    void registersSeller() throws Exception {
        when(userDAO.findByEmail("ayesha@example.com")).thenReturn(null);

        authService.register(valid("SELLER"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).create(captor.capture());
        assertEquals("SELLER", captor.getValue().getRole());
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        when(userDAO.findByEmail(anyString())).thenReturn(new User());

        assertThrows(ValidationException.class, () -> authService.register(valid("BUYER")));
        verify(userDAO, never()).create(any());
    }

    @Test
    void rejectsAdminRegistration() throws Exception {
        assertThrows(ValidationException.class, () -> authService.register(valid("ADMIN")));
        verify(userDAO, never()).create(any());
    }

    @Test
    void rejectsMismatchedPasswords() throws Exception {
        RegisterRequest request = new RegisterRequest("Ayesha", "ayesha@example.com", "Secret1", "Secret2", "BUYER");
        assertThrows(ValidationException.class, () -> authService.register(request));
    }

    @Test
    void rejectsBlankNameAndShortPassword() {
        assertThrows(ValidationException.class, () -> authService.register(
                new RegisterRequest("", "ayesha@example.com", "Abc123", "Abc123", "BUYER")));
        assertThrows(ValidationException.class, () -> authService.register(
                new RegisterRequest("Ayesha", "ayesha@example.com", "abc", "abc", "BUYER")));
    }

    @Test
    void rejectsInvalidEmail() {
        assertThrows(ValidationException.class, () -> authService.register(
                new RegisterRequest("Ayesha", "not-an-email", "Secret1", "Secret1", "BUYER")));
    }

    @Test
    void loginSucceedsWithCorrectPassword() throws Exception {
        String hash = BCrypt.hashpw("Buyer@123", BCrypt.gensalt());
        User existing = new User("Sara", "buyer@example.com", hash, "BUYER");
        existing.setId(5);
        when(userDAO.findByEmail("buyer@example.com")).thenReturn(existing);

        User loggedIn = authService.login(new LoginRequest("buyer@example.com", "Buyer@123"));

        assertNotNull(loggedIn);
        assertEquals(5, loggedIn.getId());
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        String hash = BCrypt.hashpw("RightPass1", BCrypt.gensalt());
        when(userDAO.findByEmail("buyer@example.com"))
                .thenReturn(new User("Sara", "buyer@example.com", hash, "BUYER"));

        assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginRequest("buyer@example.com", "WrongPass")));
    }

    @Test
    void loginFailsForUnknownEmail() throws Exception {
        when(userDAO.findByEmail(anyString())).thenReturn(null);

        assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginRequest("ghost@example.com", "whatever")));
    }
}