package com.example.apitestapp.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TestCaseRowModel {

    private final BooleanProperty selected = new SimpleBooleanProperty(true);
    private final StringProperty name;
    private final StringProperty input;
    private final StringProperty expected;
    private final StringProperty status;
    private final StringProperty result;
    private final String endpoint;
    private final String requestBody;

    public TestCaseRowModel(String name, String input, String expected, String endpoint, String requestBody) {
        this.name = new SimpleStringProperty(name);
        this.input = new SimpleStringProperty(input);
        this.expected = new SimpleStringProperty(expected);
        this.status = new SimpleStringProperty("-");
        this.result = new SimpleStringProperty("⚪ Chờ");
        this.endpoint = endpoint;
        this.requestBody = requestBody;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty inputProperty() {
        return input;
    }

    public StringProperty expectedProperty() {
        return expected;
    }

    public String getExpected() {
        return expected.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public StringProperty resultProperty() {
        return result;
    }

    public void setResult(String value) {
        result.set(value);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRequestBody() {
        return requestBody;
    }
}
