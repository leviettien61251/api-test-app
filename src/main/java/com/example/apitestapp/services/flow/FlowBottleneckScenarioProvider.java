package com.example.apitestapp.services.flow;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiSetupRequest;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.ArrayList;
import java.util.List;

public class FlowBottleneckScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/insert-bottleneck-data";
    private static final String ROUTE_ID = "R_BOTTLENECK";

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Testcase 1")
                        .description("Insert bottleneck success")
                        .setupRequests(createBottleneckSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(bottleneckBody(ROUTE_ID, "\"Edge_01\"", "15.0", "25.0", "0.9"))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 2")
                        .description("Insert bottleneck fail - occupancy rate greater than 1")
                        .setupRequests(createBottleneckSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(bottleneckBody(ROUTE_ID, "\"Edge_01\"", "15.0", "25.0", "1.5"))
                        .expectedCode("2003")
                        .expectedStatus("fail")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 3")
                        .description("Insert bottleneck success - boundary 0.0")
                        .setupRequests(createBottleneckSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(bottleneckBody(ROUTE_ID, "\"Edge_Zero\"", "0.0", "0.0", "0.0"))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 4")
                        .description("Insert bottleneck success - boundary 1.0")
                        .setupRequests(createBottleneckSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(bottleneckBody(ROUTE_ID, "\"Edge_Full\"", "1.0", "1.0", "1.0"))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC5")
                        .description("Insert bottleneck fail - negative occupancy")
                        .setupRequests(createBottleneckSetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody("""
                                {
                                  "route_id": "R_BOTTLENECK",
                                  "occupancy_rate": -0.5
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("fail")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("POST /api/v1/flow/insert-bottleneck-data")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/bottleneck-data"))
                .build();
    }

    private static String bottleneckBody(String routeId, String edgeName, String x, String y, String occupancyRate) {
        return """
                {
                  "route_id": "%s",
                  "edge_name": %s,
                  "x": %s,
                  "y": %s,
                  "occupancy_rate": %s
                }
                """.formatted(routeId, edgeName, x, y, occupancyRate);
    }

    private static List<ApiSetupRequest> createBottleneckSetupRequests() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.add(new ApiSetupRequest(
                "Clean bottleneck data before testcase",
                "DELETE",
                "/api/v1/clean/bottleneck-data",
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
}
