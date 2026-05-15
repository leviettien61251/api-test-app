package com.example.apitestapp.services;

import java.util.List;

public class ApiSetupRequest {
    private final String name;
    private final String method;
    private final String endpoint;
    private final String requestBody;
    private final List<String> expectedCodes;
    private final boolean required;

    public ApiSetupRequest(String name, String endpoint, String requestBody, List<String> expectedCodes, boolean required) {
        this(name, "POST", endpoint, requestBody, expectedCodes, required);
    }

    public ApiSetupRequest(String name, String method, String endpoint, String requestBody, List<String> expectedCodes, boolean required) {
        this.name = name;
        this.method = method == null || method.isBlank() ? "POST" : method.trim().toUpperCase();
        this.endpoint = endpoint;
        this.requestBody = requestBody;
        this.expectedCodes = expectedCodes == null ? List.of() : List.copyOf(expectedCodes);
        this.required = required;
    }

    public ApiSetupRequest(String name, String endpoint, String requestBody, String expectedCode, boolean required) {
        this(name, endpoint, requestBody, expectedCode == null ? List.of() : List.of(expectedCode), required);
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public List<String> getExpectedCodes() {
        return expectedCodes;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean accepts(String responseCode) {
        return expectedCodes.isEmpty() || expectedCodes.contains(responseCode);
    }
}
