package com.ayeshamart.util;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Central access point for the pooled DataSource.
 * The DatabaseListener installs the HikariCP DataSource at
 * application startup; every DAO obtains connections from here.
 */
public final class ConnectionManager {

    private static volatile HikariDataSource dataSource;

    private ConnectionManager() {
    }

    public static void setDataSource(HikariDataSource ds) {
        dataSource = ds;
    }

    public static HikariDataSource getDataSource() {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            throw new IllegalStateException("Database not initialized. Check DatabaseListener.");
        }
        return ds;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void close() {
        HikariDataSource ds = dataSource;
        dataSource = null;
        if (ds != null) {
            ds.close();
        }
    }
}