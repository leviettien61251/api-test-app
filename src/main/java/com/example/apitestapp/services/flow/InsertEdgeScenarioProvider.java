package com.example.apitestapp.services.flow;

import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;

public class InsertEdgeScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/insert-edge";

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            String requestBody,
                                            String expectedCode,
                                            String expectedStatus) {
        return ApiTestScenario.builder()
                .scenario(name)
                .description(description)
                .headers(FlowScenarioSupport.AUTH_HEADERS)
                .requestBody(requestBody)
                .expectedCode(expectedCode)
                .expectedStatus(expectedStatus)
                .build();
    }

    private static String edgeBody(String edgeIdValue) {
        return """
                {
                  "edge_id": %s
                }
                """.formatted(edgeIdValue);
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Insert edge success", edgeBody("\"Egde A04\""), "1000", "SUCCESS"),
                scenario("Testcase 2", "request == null - MISSING_BODY", "null", "2006", "FAILURE"),
                scenario("Testcase 3", "request.getEdgeId() == null - MISSING_PARAM", edgeBody("null"), "2001", "FAILURE"),
                scenario("Testcase 4", "edgeId is not String - INVALID_TYPE", edgeBody("123"), "2002", "FAILURE"),
                scenario("Testcase 5", "edgeId is empty - INVALID_TYPE", edgeBody("\"\""), "2002", "FAILURE"),
                scenario("Testcase 6", "edgeId contains single quote - INVALID_TYPE", edgeBody("\"Egde A'04\""), "2002", "FAILURE")
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("POST /api/v1/flow/insert-edge")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/edge"))
                .build();
    }
}
