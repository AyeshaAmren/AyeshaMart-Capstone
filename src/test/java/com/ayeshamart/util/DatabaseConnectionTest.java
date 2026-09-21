package com.ayeshamart.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the full connectivity chain HikariCP -> JDBC -> H2 (server mode).
 * Runs SELECT 1 through the pool. Uses a dedicated TCP port (9093) so it
 * does not clash with the running application on port 9092.
 */
class DatabaseConnectionTest {

    private static Server h2Server;
    private static HikariDataSource dataSource;

    @BeforeAll
    static void startDatabase() throws Exception {
        h2Server = Server.createTcpServer("-tcpPort", "9093", "-tcpAllowOthers", "-ifNotExists").start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:tcp://localhost:9093/./target/testdb/connectivity");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(2);
        config.setPoolName("AyeshaMartTestPool");
        dataSource = new HikariDataSource(config);
    }

    @AfterAll
    static void stopDatabase() throws Exception {
        if (dataSource != null) {
            dataSource.close();
        }
        if (h2Server != null) {
            h2Server.stop();
        }
    }

    @Test
    void connectsThroughPoolAndRunsSelectOne() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt(1));
        }
    }

    @Test
    void poolIsRunningWithConfiguredSize() {
        assertTrue(dataSource.isRunning());
        assertEquals(2, dataSource.getMaximumPoolSize());
    }
}