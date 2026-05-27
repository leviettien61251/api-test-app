package com.example.apitestapp.services.flow;

import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiSetupRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class FlowScenarioSupport {

    static final String MODULE_NAME = "Flow Module";
    static final Map<String, String> AUTH_HEADERS = Map.of("Authorization", "Bearer ${token}");

    private FlowScenarioSupport() {
    }

    static List<ApiSetupRequest> routeSetup(String routeId) {
        return List.of(new ApiSetupRequest(
                "Create route " + routeId,
                "POST",
                "/api/v1/flow/insert-route",
                """
                        {
                          "routeId": "%s"
                        }
                        """.formatted(routeId),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        ));
    }

    static List<ApiCleanupRequest> cleanupRequests(String... flowCleanEndpoints) {
        List<ApiCleanupRequest> cleanupRequests = new ArrayList<>();
        for (String endpoint : flowCleanEndpoints) {
            cleanupRequests.add(new ApiCleanupRequest(
                    "Clean " + endpoint,
                    "DELETE",
                    endpoint,
                    "",
                    AUTH_HEADERS,
                    List.of("1000", "200", "204", "201"),
                    true
            ));
        }

        cleanupRequests.add(new ApiCleanupRequest(
                "Clean route test data",
                "DELETE",
                "/api/v1/clean/route",
                "",
                AUTH_HEADERS,
                List.of("1000", "200", "204", "201"),
                true
        ));
        cleanupRequests.add(new ApiCleanupRequest(
                "Clean login test data",
                "DELETE",
                "/api/v1/clean/login",
                "",
                AUTH_HEADERS,
                List.of("1000", "200", "204", "201"),
                true
        ));
        cleanupRequests.add(new ApiCleanupRequest(
                "Clean signup test data",
                "DELETE",
                "/api/v1/clean/signup",
                "",
                AUTH_HEADERS,
                List.of("1000", "200", "204", "201"),
                true
        ));
        cleanupRequests.add(new ApiCleanupRequest(
                "Clean user test data",
                "DELETE",
                "/api/v1/clean/user-test",
                "",
                AUTH_HEADERS,
                List.of("1000", "200", "204", "201"),
                true
        ));
        return cleanupRequests;
    }
}
