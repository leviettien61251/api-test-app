package com.example.apitestapp.services;

import java.util.List;

public class ApiCleanupRequest extends ApiSetupRequest {

    public ApiCleanupRequest(String name, String endpoint, String requestBody, List<String> expectedCodes, boolean required) {
        super(name, endpoint, requestBody, expectedCodes, required);
    }

    public ApiCleanupRequest(String name, String method, String endpoint, String requestBody, List<String> expectedCodes, boolean required) {
        super(name, method, endpoint, requestBody, expectedCodes, required);
    }

    public ApiCleanupRequest(String name, String endpoint, String requestBody, String expectedCode, boolean required) {
        super(name, endpoint, requestBody, expectedCode, required);
    }
}
