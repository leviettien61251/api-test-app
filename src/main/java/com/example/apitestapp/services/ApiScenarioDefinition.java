package com.example.apitestapp.services;

import java.util.List;

public class ApiScenarioDefinition {
    private final String collectionName;
    private final String moduleName;
    private final String apiLabel;
    private final String endpoint;
    private final String sampleRequestBody;
    private final List<ApiTestScenario> scenarios;

    public ApiScenarioDefinition(String collectionName,
                                 String moduleName,
                                 String apiLabel,
                                 String endpoint,
                                 String sampleRequestBody,
                                 List<ApiTestScenario> scenarios) {
        this.collectionName = collectionName;
        this.moduleName = moduleName;
        this.apiLabel = apiLabel;
        this.endpoint = endpoint;
        this.sampleRequestBody = sampleRequestBody;
        this.scenarios = scenarios == null ? List.of() : List.copyOf(scenarios);
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
}
