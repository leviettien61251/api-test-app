package com.example.apitestapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    private String id;
    private String apiEndpointId;
    private String name;
    private String description;

    // JSONB fields — dùng Map<String, Object> để linh hoạt
    @Builder.Default
    private Map<String, Object> requestHeaders = Map.of();

    private Map<String, Object> requestBody;

    @Builder.Default
    private Map<String, Object> queryParams = Map.of();

    @Builder.Default
    private Map<String, Object> pathParams = Map.of();

    private Integer expectedStatusCode;
    private Map<String, Object> expectedResponseSchema;
    private Map<String, Object> expectedResponseBody;

    @Builder.Default
    private Map<String, Object> expectedHeaders = Map.of();

    @Builder.Default
    private String failureStrategy = "STOP_ON_FAIL";  // STOP_ON_FAIL | CONTINUE

    @Builder.Default
    private Integer timeoutMs = 30000;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private Integer retryDelayMs = 1000;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
    private Date updatedAt;
}
