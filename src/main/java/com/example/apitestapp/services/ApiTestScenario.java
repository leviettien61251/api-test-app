package com.example.apitestapp.services;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiTestScenario {
    private final String scenario;
    private final String description;
    private final List<ApiSetupRequest> setupRequests;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String requestBody;
    private final String expectedCode;
    private final String expectedStatus;

    public ApiTestScenario(String scenario, String description, String requestBody, String expectedCode, String expectedStatus) {
        this(scenario, description, List.of(), Map.of(), Map.of(), requestBody, expectedCode, expectedStatus);
    }

    public ApiTestScenario(String scenario,
                           String description,
                           List<ApiSetupRequest> setupRequests,
                           String requestBody,
                           String expectedCode,
                           String expectedStatus) {
        this(scenario, description, setupRequests, Map.of(), Map.of(), requestBody, expectedCode, expectedStatus);
    }

    public ApiTestScenario(String scenario,
                           String description,
                           List<ApiSetupRequest> setupRequests,
                           Map<String, String> queryParams,
                           String requestBody,
                           String expectedCode,
                           String expectedStatus) {
        this(scenario, description, setupRequests, Map.of(), queryParams, requestBody, expectedCode, expectedStatus);
    }

    public ApiTestScenario(String scenario,
                           String description,
                           List<ApiSetupRequest> setupRequests,
                           Map<String, String> headers,
                           Map<String, String> queryParams,
                           String requestBody,
                           String expectedCode,
                           String expectedStatus) {
        this.scenario = scenario;
        this.description = description;
        this.setupRequests = setupRequests == null ? List.of() : List.copyOf(setupRequests);
        this.headers = headers == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.queryParams = queryParams == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(queryParams));
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String scenario;
        private String description;
        private List<ApiSetupRequest> setupRequests = List.of();
        private Map<String, String> headers = Map.of();
        private Map<String, String> queryParams = Map.of();
        private String requestBody;
        private String expectedCode;
        private String expectedStatus;

        public Builder scenario(String scenario) {
            this.scenario = scenario;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder setupRequests(List<ApiSetupRequest> setupRequests) {
            this.setupRequests = setupRequests;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder queryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder expectedCode(String expectedCode) {
            this.expectedCode = expectedCode;
            return this;
        }

        public Builder expectedStatus(String expectedStatus) {
            this.expectedStatus = expectedStatus;
            return this;
        }

        public ApiTestScenario build() {
            return new ApiTestScenario(scenario, description, setupRequests, headers, queryParams, requestBody, expectedCode, expectedStatus);
        }
    }
}
