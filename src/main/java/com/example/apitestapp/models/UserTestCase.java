package com.example.apitestapp.models;

import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiPayloadAssertion;
import com.example.apitestapp.services.ApiSetupRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserTestCase {
    private String id;
    private String userId;
    private String suiteId;
    private String ownerName;
    private String apiLabel;
    private String name;
    private String description;
    private String method;
    private String endpoint;
    private Map<String, String> requestHeaders = new LinkedHashMap<>();
    private Map<String, List<String>> queryParams = new LinkedHashMap<>();
    private Map<String, String> pathParams = new LinkedHashMap<>();
    private String requestBody;
    private List<ApiSetupRequest> setupRequests = List.of();
    private List<ApiCleanupRequest> cleanupRequests = List.of();
    private List<ApiPayloadAssertion> payloadAssertions = List.of();
    private String expectedResponseBody;
    private int expectedStatusCode;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getApiLabel() {
        return apiLabel;
    }

    public void setApiLabel(String apiLabel) {
        this.apiLabel = apiLabel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders == null ? new LinkedHashMap<>() : new LinkedHashMap<>(requestHeaders);
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(queryParams);
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(pathParams);
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public List<ApiSetupRequest> getSetupRequests() {
        return setupRequests;
    }

    public void setSetupRequests(List<ApiSetupRequest> setupRequests) {
        this.setupRequests = setupRequests == null ? List.of() : List.copyOf(setupRequests);
    }

    public List<ApiCleanupRequest> getCleanupRequests() {
        return cleanupRequests;
    }

    public void setCleanupRequests(List<ApiCleanupRequest> cleanupRequests) {
        this.cleanupRequests = cleanupRequests == null ? List.of() : List.copyOf(cleanupRequests);
    }

    public List<ApiPayloadAssertion> getPayloadAssertions() {
        return payloadAssertions;
    }

    public void setPayloadAssertions(List<ApiPayloadAssertion> payloadAssertions) {
        this.payloadAssertions = payloadAssertions == null ? List.of() : List.copyOf(payloadAssertions);
    }

    public String getExpectedResponseBody() {
        return expectedResponseBody;
    }

    public void setExpectedResponseBody(String expectedResponseBody) {
        this.expectedResponseBody = expectedResponseBody;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
