package com.ayeshamart.dao;

import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data access for the users table. All SQL uses PreparedStatement.
 */
public class UserDAO {

    private static final String INSERT =
            "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID =
            "SELECT id, name, email, password_hash, role, created_at FROM users WHERE id = ?";
    private static final String SELECT_BY_EMAIL =
            "SELECT id, name, email, password_hash, role, created_at FROM users WHERE email = ?";

    public User create(User user) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        }
    }

    public User findById(long id) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EMAIL)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    // ------------------------------ admin (Phase 7) ------------------------------

    /**
     * All users, newest first. Optional text search matches name or email;
     * only a very small projection is returned by the admin service.
     */
    public List<User> findAllWithSearch(String query) throws SQLException {
        String q = query == null ? null : query.trim();
        boolean hasQuery = q != null && !q.isEmpty();

        StringBuilder sql = new StringBuilder(
                "SELECT id, name, email, password_hash, role, created_at FROM users ");
        if (hasQuery) {
            sql.append("WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? ");
        }
        sql.append("ORDER BY created_at DESC, id DESC");
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (hasQuery) {
                String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
                statement.setString(1, like);
                statement.setString(2, like);
            }
            return mapList(statement);
        }
    }

    private List<User> mapList(PreparedStatement statement) throws SQLException {
        List<User> users = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
        }
        return users;
    }

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM users");
    }

    public int countByRole(String role) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE role = ?")) {
            statement.setString(1, role);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(resultSet.getString("role"));
        user.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
        return user;
    }
}