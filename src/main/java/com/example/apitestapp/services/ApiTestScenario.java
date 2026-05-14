package com.example.apitestapp.services;

public class ApiTestScenario {
    private final String scenario;
    private final String description;
    private final String requestBody;
    private final String expectedCode;
    private final String expectedStatus;

    public ApiTestScenario(String scenario, String description, String requestBody, String expectedCode, String expectedStatus) {
        this.scenario = scenario;
        this.description = description;
        this.requestBody = requestBody;
        this.expectedCode = expectedCode;
        this.expectedStatus = expectedStatus;
    }

    public String getScenario() {
        return scenario;
    }

    public String getDescription() {
        return description;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getExpectedCode() {
        return expectedCode;
    }

    public String getExpectedStatus() {
        return expectedStatus;
    }

    public String getDisplayName() {
        if (description == null || description.isBlank()) {
            return scenario;
        }
        return scenario + " - " + description;
    }
}
