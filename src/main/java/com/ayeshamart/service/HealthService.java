package com.ayeshamart.service;

import com.ayeshamart.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Liveness check for the application and its database (Phase 8).
 *
 * <p>{@link #check()} performs a real {@code SELECT 1} through the HikariCP
 * pool so a "database UP" answer means the same connection path the DAOs use
 * actually answers. The {@code /api/v1/health} endpoint serializes the result
 * as JSON. Nothing sensitive (credentials, queries, payloads) is ever
 * included or logged here.
 */
public class HealthService {

    public static final String STATUS_UP = "UP";
    public static final String STATUS_DOWN = "DOWN";
    public static final String STATUS_DEGRADED = "DEGRADED";

    public Map<String, Object> check() {
        boolean databaseUp = isDatabaseUp();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "ayeshamart");
        result.put("status", databaseUp ? STATUS_UP : STATUS_DEGRADED);
        result.put("database", databaseUp ? STATUS_UP : STATUS_DOWN);
        result.put("timestamp", Instant.now().toString());
        return result;
    }

    /** Visible for testing - true only when SELECT 1 succeeds through the pool. */
    boolean isDatabaseUp() {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) == 1;
        } catch (Exception e) {
            return false;
        }
    }
}