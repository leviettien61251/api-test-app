package com.example.apitestapp.models.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiSetupRequest {
    private final String name;
    private final String method;
    private final String endpoint;
    private final String requestBody;
    private final Map<String, String> headers;
    private final List<String> expectedCodes;
    private final boolean required;
    private final List<ApiResponseVariable> responseVariables;

    public ApiSetupRequest(String name, String endpoint, String requestBody, List<String> expectedCodes, boolean required) {
        this(name, "POST", endpoint, requestBody, expectedCodes, required);
    }

    public ApiSetupRequest(String name, String method, String endpoint, String requestBody, List<String> expectedCodes, boolean required) {
        this(name, method, endpoint, requestBody, Map.of(), expectedCodes, required, List.of());
    }

    public ApiSetupRequest(String name, String method, String endpoint, String requestBody, Map<String, String> headers, List<String> expectedCodes, boolean required) {
        this(name, method, endpoint, requestBody, headers, expectedCodes, required, List.of());
    }

    public ApiSetupRequest(String name,
                           String method,
                           String endpoint,
                           String requestBody,
                           List<String> expectedCodes,
                           boolean required,
                           List<ApiResponseVariable> responseVariables) {
        this(name, method, endpoint, requestBody, Map.of(), expectedCodes, required, responseVariables);
    }

    public ApiSetupRequest(String name,
                           String method,
                           String endpoint,
                           String requestBody,
                           Map<String, String> headers,
                           List<String> expectedCodes,
                           boolean required,
                           List<ApiResponseVariable> responseVariables) {
        this.name = name;
        this.method = method == null || method.isBlank() ? "POST" : method.trim().toUpperCase();
        this.endpoint = endpoint;
        this.requestBody = requestBody;
        this.headers = headers == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.expectedCodes = expectedCodes == null ? List.of() : List.copyOf(expectedCodes);
        this.required = required;
        this.responseVariables = responseVariables == null ? List.of() : List.copyOf(responseVariables);
    }

    public ApiSetupRequest(String name, String endpoint, String requestBody, String expectedCode, boolean required) {
        this(name, endpoint, requestBody, expectedCode == null ? List.of() : List.of(expectedCode), required);
    }

    public static Builder builder() {
        return new Builder();
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public List<String> getExpectedCodes() {
        return expectedCodes;
    }

    public boolean isRequired() {
        return required;
    }

    public List<ApiResponseVariable> getResponseVariables() {
        return responseVariables;
    }

    public boolean accepts(String responseCode) {
        return expectedCodes.isEmpty() || expectedCodes.contains(responseCode);
    }

    public static final class Builder {
        private String name;
        private String method;
        private String endpoint;
        private String requestBody;
        private Map<String, String> headers = Map.of();
        private List<String> expectedCodes = List.of();
        private boolean required;
        private List<ApiResponseVariable> responseVariables = List.of();

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder expectedCodes(List<String> expectedCodes) {
            this.expectedCodes = expectedCodes;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder responseVariables(List<ApiResponseVariable> responseVariables) {
            this.responseVariables = responseVariables;
            return this;
        }

        public ApiSetupRequest build() {
            return new ApiSetupRequest(name, method, endpoint, requestBody, headers, expectedCodes, required, responseVariables);
        }
    }
}
