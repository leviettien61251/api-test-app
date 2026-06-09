package com.example.apitestapp.services.flow;

import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetBottlenecksScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/get_bottlenecks";
    private static final String ROUTE_ID = "R_GET_BOTTLENECKS";

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

    private static List<ApiSetupRequest> seedBottleneckData() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.addAll(FlowScenarioSupport.cleanSetup("/api/v1/clean/bottleneck-data", "/api/v1/clean/route"));
        setupRequests.addAll(FlowScenarioSupport.routeSetup(ROUTE_ID));
        setupRequests.add(FlowScenarioSupport.bottleneckSetup(ROUTE_ID, "Edge_Warning", "10.0", "20.0", "0.85"));
        setupRequests.add(FlowScenarioSupport.bottleneckSetup(ROUTE_ID, "Edge_Critical", "11.0", "21.0", "0.95"));
        setupRequests.add(FlowScenarioSupport.bottleneckSetup(ROUTE_ID, "Edge_Normal", "12.0", "22.0", "0.5"));
        return setupRequests;
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Get bottlenecks success", seedBottleneckData(), Map.of("route_id", ROUTE_ID), "1000", "SUCCESS"),
                scenario("Testcase 2", "Missing route_id - MISSING_PARAM", List.of(), Map.of(), "2001", "FAILURE"),
                scenario("Testcase 3", "route_id has multiple values - INVALID_TYPE", List.of(), Map.of("route_id[]", ROUTE_ID), "2002", "FAILURE"),
                scenario("Testcase 4", "route_id contains single quote - INVALID_TYPE", List.of(), Map.of("route_id", "R_GET'BOTTLENECKS"), "2002", "FAILURE"),
                scenario("Testcase 5", "Route does not exist - PATH_NOT_FOUND", List.of(), Map.of("route_id", "R_GET_BOTTLENECKS_MISSING"), "5003", "FAILURE")
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("GET /api/v1/flow/get_bottlenecks")
                .endpoint(ENDPOINT)
                .sampleRequestBody(null)
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/bottleneck-data"))
                .build();
    }
}
