package com.example.apitestapp.services;

import java.util.List;

public class ApiScenarioDefinition {
    private final String collectionName;
    private final String moduleName;
    private final String apiLabel;
    private final String endpoint;
    private final String sampleRequestBody;
    private final List<ApiTestScenario> scenarios;
    private final List<ApiCleanupRequest> cleanupRequests;

    public ApiScenarioDefinition(String collectionName,
                                 String moduleName,
                                 String apiLabel,
                                 String endpoint,
                                 String sampleRequestBody,
                                 List<ApiTestScenario> scenarios) {
        this(collectionName, moduleName, apiLabel, endpoint, sampleRequestBody, scenarios, List.of());
    }

    public ApiScenarioDefinition(String collectionName,
                                 String moduleName,
                                 String apiLabel,
                                 String endpoint,
                                 String sampleRequestBody,
                                 List<ApiTestScenario> scenarios,
                                 List<ApiCleanupRequest> cleanupRequests) {
        this.collectionName = collectionName;
        this.moduleName = moduleName;
        this.apiLabel = apiLabel;
        this.endpoint = endpoint;
        this.sampleRequestBody = sampleRequestBody;
        this.scenarios = scenarios == null ? List.of() : List.copyOf(scenarios);
        this.cleanupRequests = cleanupRequests == null ? List.of() : List.copyOf(cleanupRequests);
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getApiLabel() {
        return apiLabel;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getSampleRequestBody() {
        return sampleRequestBody;
    }

    public List<ApiTestScenario> getScenarios() {
        return scenarios;
    }

    public List<ApiCleanupRequest> getCleanupRequests() {
        return cleanupRequests;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String collectionName;
        private String moduleName;
        private String apiLabel;
        private String endpoint;
        private String sampleRequestBody;
        private List<ApiTestScenario> scenarios = List.of();
        private List<ApiCleanupRequest> cleanupRequests = List.of();

        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        public Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public Builder apiLabel(String apiLabel) {
            this.apiLabel = apiLabel;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder sampleRequestBody(String sampleRequestBody) {
            this.sampleRequestBody = sampleRequestBody;
            return this;
        }

        public Builder scenarios(List<ApiTestScenario> scenarios) {
            this.scenarios = scenarios;
            return this;
        }

        public Builder cleanupRequests(List<ApiCleanupRequest> cleanupRequests) {
            this.cleanupRequests = cleanupRequests;
            return this;
        }

        public ApiScenarioDefinition build() {
            return new ApiScenarioDefinition(
                    collectionName,
                    moduleName,
                    apiLabel,
                    endpoint,
                    sampleRequestBody,
                    scenarios,
                    cleanupRequests
            );
        }
    }
}
