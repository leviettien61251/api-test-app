package com.example.apitestapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionManager {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/api_test_app";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "123456789";

    private static final ConnectionManager INSTANCE = new ConnectionManager();

    private final String url;
    private final Properties properties;

    private ConnectionManager() {
        this.url = resolveUrl();
        this.properties = resolveProperties();
    }

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    private static String resolveUrl() {
        String value = System.getProperty("app.db.url");
        if (value == null || value.isBlank()) {
            value = System.getenv("APP_DB_URL");
        }
        return normalizeOrDefault(value, DEFAULT_URL);
    }

    private static Properties resolveProperties() {
        Properties props = new Properties();
        props.setProperty("user", resolveValue("app.db.user", "APP_DB_USER", DEFAULT_USER));
        props.setProperty("password", resolveValue("app.db.password", "APP_DB_PASSWORD", DEFAULT_PASSWORD));
        return props;
    }

    private static String resolveValue(String systemPropertyKey, String envKey, String defaultValue) {
        String value = System.getProperty(systemPropertyKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(envKey);
        }
        return normalizeOrDefault(value, defaultValue);
    }

    private static String normalizeOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            throw new SQLException("Failed to open database connection", e);
        }
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    public String getUrl() {
        return url;
    }
}
