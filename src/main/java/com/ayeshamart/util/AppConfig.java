package com.ayeshamart.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database configuration from db.properties.
 * Environment variables override file values, so the database
 * location can be moved (e.g. to a Railway persistent volume) without
 * changing code.
 */
public final class AppConfig {

    private static final String PROPERTIES_FILE = "/db.properties";

    private final Properties props;

    private AppConfig(Properties props) {
        this.props = props;
    }

    public static AppConfig load() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Cannot find db.properties on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load db.properties", e);
        }
        return new AppConfig(props);
    }

    private String get(String key, String envKey) {
        if (envKey != null) {
            String envValue = System.getenv(envKey);
            if (envValue != null && !envValue.isBlank()) {
                return envValue;
            }
        }
        return props.getProperty(key);
    }

    public String getJdbcUrl() {
        return get("db.url", "AYESHAMART_DB_URL");
    }

    public String getJdbcDriver() {
        return get("db.driver", null);
    }

    public String getJdbcUsername() {
        return get("db.username", "AYESHAMART_DB_USER");
    }

    public String getJdbcPassword() {
        return get("db.password", "AYESHAMART_DB_PASSWORD");
    }

    public int getPoolSize() {
        return Integer.parseInt(get("db.pool.size", null));
    }

    public int getH2TcpPort() {
        return Integer.parseInt(get("db.h2.tcp.port", "H2_TCP_PORT"));
    }

    public boolean isH2TcpAllowOthers() {
        return Boolean.parseBoolean(get("db.h2.tcp.allowOthers", null));
    }
}