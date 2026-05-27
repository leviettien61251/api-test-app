package com.example.apitestapp.services.flow;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiSetupRequest;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.ArrayList;
import java.util.List;

public class FlowDensityScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/flow/insert-route-density";
    private static final String ROUTE_ID = "R_DENSITY";

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Testcase 1")
                        .description("Insert density success")
                        .setupRequests(createDensitySetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(densityBody(ROUTE_ID, "100"))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 2")
                        .description("Insert density fail - people count negative")
                        .setupRequests(createDensitySetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(densityBody(ROUTE_ID, "-5"))
                        .expectedCode("2003")
                        .expectedStatus("fail")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 3")
                        .description("Insert density success - zero people")
                        .setupRequests(createDensitySetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody(densityBody(ROUTE_ID, "0"))
                        .expectedCode("1000")
                        .expectedStatus("success")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 4")
                        .description("Insert density fail - missing people count")
                        .setupRequests(createDensitySetupRequests())
                        .headers(FlowScenarioSupport.AUTH_HEADERS)
                        .requestBody("""
                                {
                                  "route_id": "R_DENSITY"
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("fail")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName(FlowScenarioSupport.MODULE_NAME)
                .apiLabel("POST /api/v1/flow/insert-route-density")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(FlowScenarioSupport.cleanupRequests("/api/v1/clean/route-density"))
                .build();
    }

    private static String densityBody(String routeId, String currentPeople) {
        return """
                {
                  "route_id": "%s",
                  "current_people": %s
                }
                """.formatted(routeId, currentPeople);
    }

    private static List<ApiSetupRequest> createDensitySetupRequests() {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.add(new ApiSetupRequest(
                "Clean route density data before testcase",
                "DELETE",
                "/api/v1/clean/route-density",
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
