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
import java.util.ArrayList;
import java.util.List;

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
            migrateOrderStatus(connection);

            if (isUsersTableEmpty(connection)) {
                log.info("Users table empty - running seed.sql");
                runScript(connection, SEED);
            } else {
                log.info("Users table already has data - skipping seed.sql");
            }
        }
    }

    /**
     * Phase 5/6 migration: orders previously allowed only
     * PLACED/SHIPPED/DELIVERED/CANCELLED. Checkout creates orders with
     * status PENDING and the seller workflow adds CONFIRMED, so any
     * existing CHECK constraint on the orders table that does not already
     * permit CONFIRMED is dropped and replaced by the canonical set.
     * The constraint name is never guessed (auto-names like CONSTRAINT_8B);
     * it is read from information_schema so the migration is idempotent.
     */
    private static void migrateOrderStatus(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            List<String> toDrop = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT tc.constraint_name "
                            + "FROM information_schema.table_constraints tc "
                            + "JOIN information_schema.check_constraints cc "
                            + "  ON cc.constraint_name = tc.constraint_name "
                            + " AND cc.constraint_schema = tc.constraint_schema "
                            + "WHERE tc.table_schema = 'PUBLIC' "
                            + "  AND tc.table_name = 'ORDERS' "
                            + "  AND tc.constraint_type = 'CHECK' "
                            + "  AND cc.check_clause LIKE '%STATUS%' "
                            + "  AND UPPER(cc.check_clause) NOT LIKE '%CONFIRMED%'")) {
                while (resultSet.next()) {
                    toDrop.add(resultSet.getString(1));
                }
            }

            for (String constraint : toDrop) {
                log.info("Dropping old orders status CHECK constraint {}", constraint);
                statement.execute("ALTER TABLE orders DROP CONSTRAINT " + constraint);
            }

            if (!toDrop.isEmpty()) {
                statement.execute("ALTER TABLE orders ADD CONSTRAINT orders_status_check "
                        + "CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'))");
            }
        } catch (SQLException e) {
            log.warn("Could not migrate orders status check (may already be correct)", e);
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