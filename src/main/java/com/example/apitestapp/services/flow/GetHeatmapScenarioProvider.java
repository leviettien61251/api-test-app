package com.example.apitestapp.services.flow;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiSetupRequest;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetHeatmapScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/get_heatmap";
    private static final String ROUTE_ID = "R_GET_HEATMAP";

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Get heatmap success", seedHeatmapData(), Map.of("route_id", ROUTE_ID), "1000", "SUCCESS"),
                scenario("Testcase 2", "Missing route_id - MISSING_PARAM", List.of(), Map.of(), "2001", "FAILURE"),
                scenario("Testcase 3", "route_id has multiple values - INVALID_TYPE", List.of(), Map.of("route_id[]", ROUTE_ID), "2002", "FAILURE"),
                scenario("Testcase 4", "route_id contains single quote - INVALID_TYPE", List.of(), Map.of("route_id", "R_GET'HEATMAP"), "2002", "FAILURE"),
                scenario("Testcase 5", "Route does not exist - PATH_NOT_FOUND", List.of(), Map.of("route_id", "R_GET_HEATMAP_MISSING"), "5003", "FAILURE")
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("GET /api/v1/flow/get_heatmap")
                .endpoint(ENDPOINT)
                .sampleRequestBody(null)
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/heatmap-data"))
                .build();
    }

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

    private static List<ApiSetupRequest> seedHeatmapData() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.addAll(FlowScenarioSupport.cleanSetup("/api/v1/clean/heatmap-data", "/api/v1/clean/route"));
        setupRequests.addAll(FlowScenarioSupport.routeSetup(ROUTE_ID));
        setupRequests.add(FlowScenarioSupport.heatmapSetup(ROUTE_ID, "10.0", "20.0", "0.5", "Normal", "5.0"));
        setupRequests.add(FlowScenarioSupport.heatmapSetup(ROUTE_ID, "11.0", "21.0", "0.8", "Crowded", "6.0"));
        return setupRequests;
    }
}
