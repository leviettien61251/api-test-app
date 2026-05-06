package com.example.apitestapp.config;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class AppSession {
    private static final StringProperty username = new SimpleStringProperty("Sơn");
    private static final StringProperty role = new SimpleStringProperty("Tester");

    private AppSession() {
    }

    public static StringProperty usernameProperty() {
        return username;
    }

    public static String getUsername() {
        return username.get();
    }

    public static void setUsername(String value) {
        username.set(value == null || value.isBlank() ? "User" : value.trim());
    }

    public static StringProperty roleProperty() {
        return role;
    }

    public static String getRole() {
        return role.get();
    }

    public static void setRole(String value) {
        role.set(value == null || value.isBlank() ? "Tester" : value.trim());
    }

    public static void clear() {
        setUsername("User");
        setRole("Tester");
    }
}

