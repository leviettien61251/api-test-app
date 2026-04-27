package com.example.apitestapp.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Role {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;

    public Role() {
        this.id = new SimpleStringProperty("");
        this.name = new SimpleStringProperty("");
    }

    private Role(Builder builder) {
        this.id = new SimpleStringProperty(builder.id);
        this.name = new SimpleStringProperty(builder.name);
    }

    public String getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public static class Builder {
        // Required fields
        private String id;
        private String name;

        public Builder(String id) {
            this.id = id;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Role build() {
            Role role = new Role();
            role.id.set(id);
            role.name.set(name);
            return role;
        }
    }
}
