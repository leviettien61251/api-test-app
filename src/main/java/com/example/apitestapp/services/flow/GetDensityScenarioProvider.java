package com.example.apitestapp.services.flow;

import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetDensityScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/get_density";
    private static final String ROUTE_ID = "R_GET_DENSITY";
    private static final String ROUTE_NO_DENSITY = "R_GET_DENSITY_EMPTY";

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

    private static List<ApiSetupRequest> seedDensityData() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.addAll(FlowScenarioSupport.cleanSetup("/api/v1/clean/route-density", "/api/v1/clean/route"));
        setupRequests.addAll(FlowScenarioSupport.routeSetup(ROUTE_ID));
        setupRequests.add(FlowScenarioSupport.routeDensitySetup(ROUTE_ID, "150"));
        return setupRequests;
    }

    private static List<ApiSetupRequest> seedRouteOnly(String routeId) {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.addAll(FlowScenarioSupport.cleanSetup("/api/v1/clean/route-density", "/api/v1/clean/route"));
        setupRequests.addAll(FlowScenarioSupport.routeSetup(routeId));
        return setupRequests;
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Testcase 1", "Get density success", seedDensityData(), Map.of("route_id", ROUTE_ID), "1000", "SUCCESS"),
                scenario("Testcase 2", "Route exists but density data is empty - returns 0", seedRouteOnly(ROUTE_NO_DENSITY), Map.of("route_id", ROUTE_NO_DENSITY), "1000", "SUCCESS"),
                scenario("Testcase 3", "Missing route_id - MISSING_PARAM", List.of(), Map.of(), "2001", "FAILURE"),
                scenario("Testcase 4", "route_id has multiple values - INVALID_TYPE", List.of(), Map.of("route_id[]", ROUTE_ID), "2002", "FAILURE"),
                scenario("Testcase 5", "route_id contains single quote - INVALID_TYPE", List.of(), Map.of("route_id", "R_GET'DENSITY"), "2002", "FAILURE"),
                scenario("Testcase 6", "Route does not exist - PATH_NOT_FOUND", List.of(), Map.of("route_id", "R_GET_DENSITY_MISSING"), "5003", "FAILURE")
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("GET /api/v1/flow/get_density")
                .endpoint(ENDPOINT)
                .sampleRequestBody(null)
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/route-density"))
                .build();
    }
}
