package com.ayeshamart.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.RunScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

/**
 * Builds an isolated in-memory H2 database for DAO tests and loads
 * the real schema.sql, so tests exercise the exact production schema.
 */
public final class TestDb {

    private TestDb() {
    }

    public static HikariDataSource create(String dbName) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(4);

        HikariDataSource dataSource = new HikariDataSource(config);
        try (Connection connection = dataSource.getConnection();
             InputStream in = TestDb.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql not found on classpath");
            }
            RunScript.execute(connection, new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return dataSource;
    }
}