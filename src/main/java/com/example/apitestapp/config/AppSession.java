package com.example.apitestapp.config;

import com.example.apitestapp.models.ClientMachine;
import com.example.apitestapp.models.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppSession {
    private static AppSession instance;

    private User currentUser;
    private ClientMachine clientMachine;

    private static final StringProperty username = new SimpleStringProperty("Sơn");
    private static final StringProperty role = new SimpleStringProperty("Tester");


    private AppSession() {
    }

    public static AppSession getInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public ClientMachine getClientMachine() {
        return clientMachine;
    }

    public void setClientMachine(ClientMachine clientMachine) {
        this.clientMachine = clientMachine;
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

