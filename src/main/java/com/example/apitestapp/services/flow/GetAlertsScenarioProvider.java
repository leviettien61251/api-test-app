package com.example.apitestapp.services.flow;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiSetupRequest;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetAlertsScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/get_alerts";
    private static final String CURRENT_EDGE = "Egde Alerts Current";
    private static final String BLOCKED_EDGE = "Egde Alerts Blocked";

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Get alerts success", seedAlertsData(), FlowScenarioSupport.AUTH_HEADERS, Map.of("current_edge", CURRENT_EDGE), "1000", "SUCCESS"),
                scenario("Testcase 2", "Missing token - MISSING_PARAM", List.of(), Map.of("Authorization", ""), Map.of("current_edge", CURRENT_EDGE), "2001", "FAILURE"),
                scenario("Testcase 3", "Missing current_edge - MISSING_PARAM", List.of(), FlowScenarioSupport.AUTH_HEADERS, Map.of(), "2001", "FAILURE"),
                scenario("Testcase 4", "current_edge has multiple values - INVALID_TYPE", List.of(), FlowScenarioSupport.AUTH_HEADERS, Map.of("current_edge[]", CURRENT_EDGE), "2002", "FAILURE"),
                scenario("Testcase 5", "current_edge contains single quote - INVALID_TYPE", List.of(), FlowScenarioSupport.AUTH_HEADERS, Map.of("current_edge", "Egde Alerts'Current"), "2002", "FAILURE"),
                scenario("Testcase 6", "current_edge does not exist - EDGE_NOT_FOUND", List.of(), FlowScenarioSupport.AUTH_HEADERS, Map.of("current_edge", "Egde Alerts Missing"), "4003", "FAILURE")
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("GET /api/v1/flow/get_alerts")
                .endpoint(ENDPOINT)
                .sampleRequestBody(null)
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/edge-status", "/api/v1/clean/edge"))
                .build();
    }

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            List<ApiSetupRequest> setupRequests,
                                            Map<String, String> headers,
                                            Map<String, String> queryParams,
                                            String expectedCode,
                                            String expectedStatus) {
        return ApiTestScenario.builder()
                .scenario(name)
                .description(description)
                .setupRequests(setupRequests)
                .headers(headers)
                .queryParams(queryParams)
                .expectedCode(expectedCode)
                .expectedStatus(expectedStatus)
                .build();
    }

    private static List<ApiSetupRequest> seedAlertsData() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.addAll(FlowScenarioSupport.cleanSetup("/api/v1/clean/edge-status", "/api/v1/clean/edge"));
        setupRequests.addAll(FlowScenarioSupport.edgeSetup(CURRENT_EDGE));
        setupRequests.addAll(FlowScenarioSupport.edgeSetup(BLOCKED_EDGE));
        setupRequests.add(FlowScenarioSupport.edgeStatusSetup(BLOCKED_EDGE, "1.0"));
        return setupRequests;
    }
}
