package com.example.apitestapp.services.flow;

import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;

public class InsertEdgeDensityScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/insert-edge-density";
    private static final String EDGE_ID = "Egde A6";
    private static final String MISSING_EDGE_ID = "Egde Density Missing Edge";

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

    private static String edgeDensityBody(String edgeIdValue, String currentCountValue, String fillPercentageValue) {
        return """
                {
                  "edge_id": %s,
                  "current_count": %s,
                  "fill_percentage": %s
                }
                """.formatted(edgeIdValue, currentCountValue, fillPercentageValue);
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Insert edge density success", edgeDensityBody("\"" + EDGE_ID + "\"", "150", "\"50%\""), "1000", "SUCCESS", true),
                scenario("Testcase 2", "request == null - MISSING_BODY", "null", "2006", "FAILURE", false),
                scenario("Testcase 3", "Missing edgeId - MISSING_PARAM", """
                        {
                          "current_count": 150,
                          "fill_percentage": "50%"
                        }
                        """, "2001", "FAILURE", false),
                scenario("Testcase 4", "Missing currentCount - MISSING_PARAM", """
                        {
                          "edge_id": "Egde A6",
                          "fill_percentage": "50%"
                        }
                        """, "2001", "FAILURE", true),
                scenario("Testcase 5", "Missing fillPercentage - MISSING_PARAM", """
                        {
                          "edge_id": "Egde A6",
                          "current_count": 150
                        }
                        """, "2001", "FAILURE", true),
                scenario("Testcase 6", "Invalid edgeId - INVALID_TYPE", edgeDensityBody("\"Egde A'6\"", "150", "\"50%\""), "2002", "FAILURE", false),
                scenario("Testcase 7", "currentCount is not valid integer - INVALID_TYPE", edgeDensityBody("\"" + EDGE_ID + "\"", "150.5", "\"50%\""), "2002", "FAILURE", true),
                scenario("Testcase 8", "fillPercentage is invalid - INVALID_TYPE", edgeDensityBody("\"" + EDGE_ID + "\"", "150", "\"abc\""), "2002", "FAILURE", true),
                scenario("Testcase 9", "currentCount < 0 - INVALID_VALUE", edgeDensityBody("\"" + EDGE_ID + "\"", "-1", "\"50%\""), "2003", "FAILURE", true),
                scenario("Testcase 10", "edgeId does not exist in DB - EDGE_NOT_FOUND", edgeDensityBody("\"" + MISSING_EDGE_ID + "\"", "150", "\"50%\""), "4003", "FAILURE", false)
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("POST /api/v1/flow/insert-edge-density")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/edge-density", "/api/v1/clean/edge"))
                .build();
    }
}
