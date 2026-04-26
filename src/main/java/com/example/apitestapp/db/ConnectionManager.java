package com.example.apitestapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {
    private static ConnectionManager instance;
    private String url = "jdbc:postgresql://localhost:5432/hospital_test";
    Properties props = new Properties();
    Connection conn = null;

    private ConnectionManager() {
        loadConfig();
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    private void loadConfig() {
        props.setProperty("user", "postgres");
        props.setProperty("password", "123456789");
    }

    public Connection getConnection() throws SQLException {

        try {
            conn = DriverManager.getConnection(url, props);

        } catch (SQLException e) {
            throw new SQLException("Failed to get connection from pool", e);
        }
        return conn;
    }

    public void closeConnection() throws SQLException {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Failed to close connection", e);
        }
    }
}
