package com.ayeshamart.service;

import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Health endpoint tests (Phase 8): the check must report the real database
 * state through the same connection pool the DAOs use.
 */
class HealthServiceTest {

    @AfterEach
    void tearDown() {
        ConnectionManager.close();
    }

    @Test
    void reportsUpWhenDatabaseAnswers() throws Exception {
        HikariDataSource dataSource = TestDb.create("health-" + System.nanoTime());
        ConnectionManager.setDataSource(dataSource);

        Map<String, Object> result = new HealthService().check();

        assertEquals("ayeshamart", result.get("service"));
        assertEquals("UP", result.get("status"));
        assertEquals("UP", result.get("database"));
        assertEquals("UP", new HealthService().isDatabaseUp() ? "UP" : "DOWN");
    }

    @Test
    void reportsDegradedWhenDatabaseIsUnavailable() {
        ConnectionManager.close();

        Map<String, Object> result = new HealthService().check();

        assertEquals("DEGRADED", result.get("status"));
        assertEquals("DOWN", result.get("database"));
    }

    @Test
    void alwaysIncludesTimestamp() throws Exception {
        HikariDataSource dataSource = TestDb.create("health-" + System.nanoTime());
        ConnectionManager.setDataSource(dataSource);

        Map<String, Object> result = new HealthService().check();

        String timestamp = (String) result.get("timestamp");
        assertEquals(timestamp, result.get("timestamp"));
        assertEquals(true, timestamp != null && !timestamp.isBlank());
    }
}