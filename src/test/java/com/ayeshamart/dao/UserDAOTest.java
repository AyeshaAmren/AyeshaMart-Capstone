package com.ayeshamart.dao;

import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDAOTest {

    private static UserDAO userDAO;

    @BeforeAll
    static void init() throws Exception {
        ConnectionManager.setDataSource(TestDb.create("userdaotest"));
        userDAO = new UserDAO();
    }

    @AfterAll
    static void cleanup() {
        ConnectionManager.close();
    }

    @Test
    void createGeneratesIdAndPersists() throws Exception {
        User user = userDAO.create(new User("Test User", "test1@example.com", "hash", "BUYER"));

        assertTrue(user.getId() > 0);
        User found = userDAO.findById(user.getId());
        assertNotNull(found);
        assertEquals("Test User", found.getName());
        assertEquals("test1@example.com", found.getEmail());
        assertEquals("BUYER", found.getRole());
        assertEquals("hash", found.getPasswordHash());
    }

    @Test
    void findByEmailReturnsUserAndNullForUnknown() throws Exception {
        User user = userDAO.create(new User("Mail User", "mail@example.com", "hash", "SELLER"));

        User found = userDAO.findByEmail("mail@example.com");
        assertNotNull(found);
        assertEquals(user.getId(), found.getId());

        assertNull(userDAO.findByEmail("nobody@example.com"));
    }

    @Test
    void duplicateEmailViolatesUniqueConstraint() throws Exception {
        userDAO.create(new User("First", "dup@example.com", "hash", "BUYER"));

        assertThrows(SQLException.class,
                () -> userDAO.create(new User("Second", "dup@example.com", "hash", "SELLER")));
    }
}