package com.ayeshamart.listener;

import com.ayeshamart.util.AppConfig;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.DatabaseInitializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;

/**
 * Bootstraps the database when Tomcat starts and tears it down on stop:
 * 1. starts the H2 TCP server (server mode),
 * 2. builds the HikariCP connection pool,
 * 3. runs schema + seed scripts through DatabaseInitializer,
 * 4. publishes the DataSource on the ServletContext and ConnectionManager,
 * 5. closes the pool and stops the H2 server when Tomcat shuts down.
 */
@WebListener
public class DatabaseListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(DatabaseListener.class);

    private HikariDataSource dataSource;
    private Server h2Server;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            AppConfig config = AppConfig.load();
            h2Server = startH2Server(config);
            dataSource = createDataSource(config);
            DatabaseInitializer.initialize(dataSource);

            ConnectionManager.setDataSource(dataSource);

            ServletContext context = event.getServletContext();
            context.setAttribute("datasource", dataSource);
            context.setAttribute("dbConfig", config);

            log.info("AyeshaMart database ready at {}", config.getJdbcUrl());
        } catch (Exception e) {
            log.error("Failed to initialize AyeshaMart database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            ConnectionManager.close();
        } catch (Exception e) {
            log.warn("Error while closing connection pool", e);
        }
        try {
            if (h2Server != null && h2Server.isRunning(false)) {
                h2Server.stop();
                log.info("H2 server stopped");
            }
        } catch (Exception e) {
            log.warn("Error while stopping H2 server", e);
        }
    }

    private Server startH2Server(AppConfig config) throws SQLException {
        Server server = Server.createTcpServer(
                "-tcpPort", String.valueOf(config.getH2TcpPort()),
                "-tcpAllowOthers",
                "-ifNotExists").start();
        log.info("H2 TCP server started on port {}", config.getH2TcpPort());
        return server;
    }

    private HikariDataSource createDataSource(AppConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setDriverClassName(config.getJdbcDriver());
        hikariConfig.setUsername(config.getJdbcUsername());
        hikariConfig.setPassword(config.getJdbcPassword());
        hikariConfig.setMaximumPoolSize(config.getPoolSize());
        hikariConfig.setPoolName("AyeshaMartPool");
        hikariConfig.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(hikariConfig);
    }
}