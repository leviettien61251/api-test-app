package com.example.apitestapp.services;

import lombok.Builder;

import java.util.List;

@Builder
public class ApiTestScenario {
    private final String scenario;
    private final String description;
    private final List<ApiSetupRequest> setupRequests;
    private final String requestBody;
    private final String expectedCode;
    private final String expectedStatus;

    public ApiTestScenario(String scenario, String description, String requestBody, String expectedCode, String expectedStatus) {
        this(scenario, description, List.of(), requestBody, expectedCode, expectedStatus);
    }

    public ApiTestScenario(String scenario,
                           String description,
                           List<ApiSetupRequest> setupRequests,
                           String requestBody,
                           String expectedCode,
                           String expectedStatus) {
        this.scenario = scenario;
        this.description = description;
        this.setupRequests = setupRequests == null ? List.of() : List.copyOf(setupRequests);
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

    public List<ApiSetupRequest> getSetupRequests() {
        return setupRequests;
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
