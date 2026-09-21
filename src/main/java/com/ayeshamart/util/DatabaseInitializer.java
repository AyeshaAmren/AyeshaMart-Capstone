package com.ayeshamart.util;

import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the H2 database on first startup:
 * 1. executes schema.sql (CREATE TABLE IF NOT EXISTS -> idempotent),
 * 2. executes seed.sql ONLY when the users table is empty,
 *    so persistent data created by the app is never overwritten.
 */
public final class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final String SCHEMA = "/schema.sql";
    private static final String SEED = "/seed.sql";

    private DatabaseInitializer() {
    }

    public static void initialize(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            log.info("Running schema.sql");
            runScript(connection, SCHEMA);

            if (isUsersTableEmpty(connection)) {
                log.info("Users table empty - running seed.sql");
                runScript(connection, SEED);
            } else {
                log.info("Users table already has data - skipping seed.sql");
            }
        }
    }

    private static boolean isUsersTableEmpty(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            resultSet.next();
            return resultSet.getInt(1) == 0;
        }
    }

    private static void runScript(Connection connection, String resource) throws SQLException {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + resource);
            }
            RunScript.execute(connection, new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new SQLException("Failed to read SQL script " + resource, e);
        }
    }
}