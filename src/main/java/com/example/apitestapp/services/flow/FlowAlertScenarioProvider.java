package com.example.apitestapp.services.flow;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiSetupRequest;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.ArrayList;
import java.util.List;

public class FlowAlertScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/insert-obstacle";
    private static final String ROUTE_ID = "R_ALERT";

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Testcase 1")
                        .description("Insert obstacle success")
                        .setupRequests(createAlertSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(obstacleBody(ROUTE_ID, "\"Construction\"", "5.0", "5.0", "\"Building works\"", "\"Active\""))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 2")
                        .description("Insert obstacle fail - missing required parameters")
                        .setupRequests(createAlertSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody("""
                                {
                                  "route_id": "R_ALERT"
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("fail")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 3")
                        .description("Insert obstacle fail - invalid empty type")
                        .setupRequests(createAlertSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(obstacleBody(ROUTE_ID, "\"\"", "1.0", "1.0", "\"D\"", "\"S\""))
                        .expectedCode("2002")
                        .expectedStatus("fail")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 4")
                        .description("Insert multiple obstacles for same route")
                        .setupRequests(createMultipleObstacleSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(obstacleBody(ROUTE_ID, "\"Obstacle 1\"", "1", "1", "\"Desc\"", "\"Open\""))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("POST /api/v1/flow/insert-obstacle")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/obstacle"))
                .build();
    }

    private static String obstacleBody(String routeId,
                                       String type,
                                       String xCoordinate,
                                       String yCoordinate,
                                       String description,
                                       String obstacleStatus) {
        return """
                {
                  "route_id": "%s",
                  "type": %s,
                  "x_coordinate": %s,
                  "y_coordinate": %s,
                  "description": %s,
                  "obstacle_status": %s
                }
                """.formatted(routeId, type, xCoordinate, yCoordinate, description, obstacleStatus);
    }

    private static List<ApiSetupRequest> createAlertSetupRequests() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.add(new ApiSetupRequest(
                "Clean obstacle data before alert testcase",
                "DELETE",
                "/api/v1/clean/obstacle",
                "",
                FlowScenarioSupport.AUTH_HEADERS,
                List.of("1000", "200", "204", "201"),
                true
        ));
        setupRequests.add(new ApiSetupRequest(
                "Clean route data before alert testcase",
                "DELETE",
                "/api/v1/clean/route",
                "",
                FlowScenarioSupport.AUTH_HEADERS,
                List.of("1000", "200", "204", "201"),
                true
        ));
        setupRequests.addAll(FlowScenarioSupport.routeSetup(ROUTE_ID));
        return setupRequests;
    }

    private static List<ApiSetupRequest> createMultipleObstacleSetupRequests() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>(createAlertSetupRequests());
        setupRequests.add(new ApiSetupRequest(
                "Insert first obstacle for same route",
                "POST",
                ENDPOINT,
                obstacleBody(ROUTE_ID, "\"Obstacle 0\"", "0", "0", "\"Desc\"", "\"Open\""),
                FlowScenarioSupport.AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        ));
        return setupRequests;
    }
}
