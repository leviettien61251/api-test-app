package com.example.apitestapp.services.flow;

import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;

public class InsertEdgeStatusScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/insert-edge-status";
    private static final String EDGE_ID = "Egde Status A04";
    private static final String MISSING_EDGE_ID = "Egde Status Missing Edge";

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            String requestBody,
                                            String expectedCode,
                                            String expectedStatus,
                                            boolean seedEdge) {
        return ApiTestScenario.builder()
                .scenario(name)
                .description(description)
                .setupRequests(seedEdge ? seedEdgeRequests() : List.of())
                .headers(FlowScenarioSupport.AUTH_HEADERS)
                .requestBody(requestBody)
                .expectedCode(expectedCode)
                .expectedStatus(expectedStatus)
                .build();
    }

    private static List<ApiSetupRequest> seedEdgeRequests() {
        return FlowScenarioSupport.edgeSetup(EDGE_ID);
    }

    private static String edgeStatusBody(String edgeIdValue, String occupancyRateValue) {
        return """
                {
                  "edge_id": %s,
                  "occupancy_rate": %s
                }
                """.formatted(edgeIdValue, occupancyRateValue);
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Insert edge status success", edgeStatusBody("\"" + EDGE_ID + "\"", "0.9"), "1000", "SUCCESS", true),
                scenario("Testcase 2", "request == null - MISSING_BODY", "null", "2006", "FAILURE", false),
                scenario("Testcase 3", "Missing edgeId - MISSING_PARAM", """
                        {
                          "occupancy_rate": 0.9
                        }
                        """, "2001", "FAILURE", false),
                scenario("Testcase 4", "Missing occupancyRate - MISSING_PARAM", """
                        {
                          "edge_id": "Egde Status A04"
                        }
                        """, "2001", "FAILURE", true),
                scenario("Testcase 5", "Invalid edgeId - INVALID_TYPE", edgeStatusBody("\"Egde Status A'04\"", "0.9"), "2002", "FAILURE", false),
                scenario("Testcase 6", "occupancyRate is not valid number - INVALID_TYPE", edgeStatusBody("\"" + EDGE_ID + "\"", "\"abc\""), "2002", "FAILURE", true),
                scenario("Testcase 7", "occupancyRate < 0 - INVALID_VALUE", edgeStatusBody("\"" + EDGE_ID + "\"", "-0.1"), "2003", "FAILURE", true),
                scenario("Testcase 8", "occupancyRate > 1 - INVALID_VALUE", edgeStatusBody("\"" + EDGE_ID + "\"", "1.1"), "2003", "FAILURE", true),
                scenario("Testcase 9", "edgeId does not exist in DB - EDGE_NOT_FOUND", edgeStatusBody("\"" + MISSING_EDGE_ID + "\"", "0.9"), "4003", "FAILURE", false)
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("POST /api/v1/flow/insert-edge-status")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/edge-status", "/api/v1/clean/edge"))
                .build();
    }
}
