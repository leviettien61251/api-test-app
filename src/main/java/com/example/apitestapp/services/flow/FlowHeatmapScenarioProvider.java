package com.example.apitestapp.services.flow;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiSetupRequest;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.ArrayList;
import java.util.List;

public class FlowHeatmapScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/insert-heatmap-data";
    private static final String ROUTE_ID = "R_HEATMAP";

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Testcase 1")
                        .description("Insert heatmap success")
                        .setupRequests(createHeatmapSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(heatmapBody(ROUTE_ID, "10.0", "20.0", "0.5", "\"Normal\"", "5.0"))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 2")
                        .description("Insert heatmap fail - invalid route id")
                        .setupRequests(createHeatmapSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(heatmapBody("INVALID_ID", "10.0", "20.0", "0.5", "\"Normal\"", "5.0"))
                        .expectedCode("5003")
                        .expectedStatus("fail")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 3")
                        .description("Insert heatmap fail - missing X coordinate")
                        .setupRequests(createHeatmapSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody("""
                                {
                                  "route_id": "R_HEATMAP",
                                  "y": 20.0,
                                  "density_value": 0.5,
                                  "status_message": "Test",
                                  "radius": 5.0
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("fail")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 4")
                        .description("Insert heatmap fail - negative density")
                        .setupRequests(createHeatmapSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(heatmapBody(ROUTE_ID, "10.0", "10.0", "-0.1", "\"Test\"", "5.0"))
                        .expectedCode("2003")
                        .expectedStatus("fail")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 5")
                        .description("Multiple inserts for same route")
                        .setupRequests(createMultipleHeatmapSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(heatmapBody(ROUTE_ID, "2", "2", "0.5", "\"M\"", "1.0"))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("POST /api/v1/flow/insert-heatmap-data")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/heatmap-data"))
                .build();
    }

    private static String heatmapBody(String routeId,
                                      String x,
                                      String y,
                                      String densityValue,
                                      String statusMessage,
                                      String radius) {
        return """
                {
                  "route_id": "%s",
                  "x": %s,
                  "y": %s,
                  "density_value": %s,
                  "status_message": %s,
                  "radius": %s
                }
                """.formatted(routeId, x, y, densityValue, statusMessage, radius);
    }

    private static List<ApiSetupRequest> createHeatmapSetupRequests() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.add(new ApiSetupRequest(
                "Clean heatmap data before testcase",
                "DELETE",
                "/api/v1/clean/heatmap-data",
                "",
                FlowScenarioSupport.AUTH_HEADERS,
                List.of("1000", "200", "204", "201"),
                true
        ));
        setupRequests.add(new ApiSetupRequest(
                "Clean route data before testcase",
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

    private static List<ApiSetupRequest> createMultipleHeatmapSetupRequests() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>(createHeatmapSetupRequests());
        setupRequests.add(new ApiSetupRequest(
                "Insert first heatmap point for same route",
                "POST",
                ENDPOINT,
                heatmapBody(ROUTE_ID, "0", "0", "0.5", "\"M\"", "1.0"),
                FlowScenarioSupport.AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        ));
        setupRequests.add(new ApiSetupRequest(
                "Insert second heatmap point for same route",
                "POST",
                ENDPOINT,
                heatmapBody(ROUTE_ID, "1", "1", "0.5", "\"M\"", "1.0"),
                FlowScenarioSupport.AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        ));
        return setupRequests;
    }
}
