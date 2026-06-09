package com.example.apitestapp.services.flow;

import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetEdgeStatusScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/edge_status";
    private static final String EDGE_ID = "Egde Status Density";
    private static final String EDGE_NO_DENSITY = "Egde Status No Density";

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            List<ApiSetupRequest> setupRequests,
                                            Map<String, String> queryParams,
                                            String expectedCode,
                                            String expectedStatus) {
        return ApiTestScenario.builder()
                .scenario(name)
                .description(description)
                .setupRequests(setupRequests)
                .headers(FlowScenarioSupport.AUTH_HEADERS)
                .queryParams(queryParams)
                .expectedCode(expectedCode)
                .expectedStatus(expectedStatus)
                .build();
    }

    private static List<ApiSetupRequest> seedEdgeDensityData() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.addAll(FlowScenarioSupport.cleanSetup("/api/v1/clean/edge-density", "/api/v1/clean/edge"));
        setupRequests.addAll(FlowScenarioSupport.edgeSetup(EDGE_ID));
        setupRequests.add(FlowScenarioSupport.edgeDensitySetup(EDGE_ID, "150", "50%"));
        return setupRequests;
    }

    private static List<ApiSetupRequest> seedEdgeOnly() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.addAll(FlowScenarioSupport.cleanSetup("/api/v1/clean/edge-density", "/api/v1/clean/edge"));
        setupRequests.addAll(FlowScenarioSupport.edgeSetup(EDGE_NO_DENSITY));
        return setupRequests;
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Get edge status success", seedEdgeDensityData(), Map.of("edge_id", EDGE_ID), "1000", "SUCCESS"),
                scenario("Testcase 2", "Missing edge_id - MISSING_PARAM", List.of(), Map.of(), "2001", "FAILURE"),
                scenario("Testcase 3", "edge_id has multiple values - INVALID_TYPE", List.of(), Map.of("edge_id[]", EDGE_ID), "2002", "FAILURE"),
                scenario("Testcase 4", "edge_id contains single quote - INVALID_TYPE", List.of(), Map.of("edge_id", "Egde Status'Density"), "2002", "FAILURE"),
                scenario("Testcase 5", "edge_id does not exist - EDGE_NOT_FOUND", List.of(), Map.of("edge_id", "Egde Status Missing"), "4003", "FAILURE"),
                scenario("Testcase 6", "Edge exists but density is unavailable", seedEdgeOnly(), Map.of("edge_id", EDGE_NO_DENSITY), "6002", "FAILURE")
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("GET /api/v1/flow/edge_status")
                .endpoint(ENDPOINT)
                .sampleRequestBody(null)
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/edge-density", "/api/v1/clean/edge"))
                .build();
    }
}
