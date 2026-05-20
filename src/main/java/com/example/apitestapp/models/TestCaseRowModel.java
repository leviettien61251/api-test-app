package com.example.apitestapp.models;

import com.example.apitestapp.services.ApiTestScenario;
import com.example.apitestapp.services.ApiCleanupRequest;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestCaseRowModel {

    private final BooleanProperty selected = new SimpleBooleanProperty(true);
    private final StringProperty name;
    private final StringProperty input;
    private final StringProperty expected;
    private final StringProperty status;
    private final StringProperty result;
    private final String endpoint;
    private final String method;
    private final Map<String, String> headers;
    private final String requestBody;
    private final ApiTestScenario scenario;
    private final String userTestCaseId;
    private final List<ApiCleanupRequest> cleanupRequests;

    public TestCaseRowModel(String name, String input, String expected, String endpoint, String requestBody) {
        this(name, input, expected, endpoint, "POST", requestBody, null);
    }

    public TestCaseRowModel(String name, String input, String expected, String endpoint, String requestBody, ApiTestScenario scenario) {
        this(name, input, expected, endpoint, "POST", requestBody, scenario);
    }

    public TestCaseRowModel(String name, String input, String expected, String endpoint, String method, String requestBody, ApiTestScenario scenario) {
        this(name, input, expected, endpoint, method, Map.of(), requestBody, scenario);
    }

    public TestCaseRowModel(String name,
                            String input,
                            String expected,
                            String endpoint,
                            String method,
                            Map<String, String> headers,
                            String requestBody,
                            ApiTestScenario scenario) {
        this(name, input, expected, endpoint, method, headers, requestBody, scenario, null);
    }

    public TestCaseRowModel(String name,
                            String input,
                            String expected,
                            String endpoint,
                            String method,
                            Map<String, String> headers,
                            String requestBody,
                            ApiTestScenario scenario,
                            String userTestCaseId) {
        this(name, input, expected, endpoint, method, headers, requestBody, scenario, userTestCaseId, List.of());
    }

    public TestCaseRowModel(String name,
                            String input,
                            String expected,
                            String endpoint,
                            String method,
                            Map<String, String> headers,
                            String requestBody,
                            ApiTestScenario scenario,
                            String userTestCaseId,
                            List<ApiCleanupRequest> cleanupRequests) {
        this.name = new SimpleStringProperty(name);
        this.input = new SimpleStringProperty(input);
        this.expected = new SimpleStringProperty(expected);
        this.status = new SimpleStringProperty("-");
        this.result = new SimpleStringProperty("⚪ Chờ");
        this.endpoint = endpoint;
        this.method = method == null || method.isBlank() ? "POST" : method.trim().toUpperCase();
        this.headers = headers == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.requestBody = requestBody;
        this.scenario = scenario;
        this.userTestCaseId = userTestCaseId;
        this.cleanupRequests = cleanupRequests == null ? List.of() : List.copyOf(cleanupRequests);
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

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public ApiTestScenario getScenario() {
        return scenario;
    }

    public String getUserTestCaseId() {
        return userTestCaseId;
    }

    public boolean isUserCreated() {
        return userTestCaseId != null && !userTestCaseId.isBlank();
    }

    public List<ApiCleanupRequest> getCleanupRequests() {
        return cleanupRequests;
    }
}
