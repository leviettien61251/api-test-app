package com.example.apitestapp.services.flow;

import com.example.apitestapp.models.dto.ApiCleanupRequest;
import com.example.apitestapp.models.dto.ApiSetupRequest;

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

    static List<ApiSetupRequest> edgeSetup(String edgeId) {
        return List.of(new ApiSetupRequest(
                "Create edge " + edgeId,
                "POST",
                "/api/v1/flow/insert-edge",
                """
                        {
                          "edge_id": "%s"
                        }
                        """.formatted(edgeId),
                AUTH_HEADERS,
                List.of("1000", "200", "201", "3006", "4006"),
                true
        ));
    }

    static List<ApiSetupRequest> cleanSetup(String... flowCleanEndpoints) {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        for (String endpoint : flowCleanEndpoints) {
            setupRequests.add(new ApiSetupRequest(
                    "Clean " + endpoint + " before testcase",
                    "DELETE",
                    endpoint,
                    "",
                    AUTH_HEADERS,
                    List.of("1000", "200", "204", "201"),
                    true
            ));
        }
        return setupRequests;
    }

    static ApiSetupRequest routeDensitySetup(String routeId, String currentPeople) {
        return new ApiSetupRequest(
                "Create route density for " + routeId,
                "POST",
                "/api/v1/flow/insert-route-density",
                """
                        {
                          "route_id": "%s",
                          "current_people": %s
                        }
                        """.formatted(routeId, currentPeople),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        );
    }

    static ApiSetupRequest heatmapSetup(String routeId,
                                        String x,
                                        String y,
                                        String densityValue,
                                        String statusMessage,
                                        String radius) {
        return new ApiSetupRequest(
                "Create heatmap data for " + routeId,
                "POST",
                "/api/v1/flow/insert-heatmap-data",
                """
                        {
                          "route_id": "%s",
                          "x": %s,
                          "y": %s,
                          "density_value": %s,
                          "status_message": "%s",
                          "radius": %s
                        }
                        """.formatted(routeId, x, y, densityValue, statusMessage, radius),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        );
    }

    static ApiSetupRequest bottleneckSetup(String routeId,
                                           String edgeName,
                                           String x,
                                           String y,
                                           String occupancyRate) {
        return new ApiSetupRequest(
                "Create bottleneck data for " + routeId,
                "POST",
                "/api/v1/flow/insert-bottleneck-data",
                """
                        {
                          "route_id": "%s",
                          "edge_name": "%s",
                          "x": %s,
                          "y": %s,
                          "occupancy_rate": %s
                        }
                        """.formatted(routeId, edgeName, x, y, occupancyRate),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        );
    }

    static ApiSetupRequest edgeStatusSetup(String edgeId, String occupancyRate) {
        return new ApiSetupRequest(
                "Create edge status for " + edgeId,
                "POST",
                "/api/v1/flow/insert-edge-status",
                """
                        {
                          "edge_id": "%s",
                          "occupancy_rate": %s
                        }
                        """.formatted(edgeId, occupancyRate),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        );
    }

    static ApiSetupRequest edgeDensitySetup(String edgeId, String currentCount, String fillPercentage) {
        return new ApiSetupRequest(
                "Create edge density for " + edgeId,
                "POST",
                "/api/v1/flow/insert-edge-density",
                """
                        {
                          "edge_id": "%s",
                          "current_count": %s,
                          "fill_percentage": "%s"
                        }
                        """.formatted(edgeId, currentCount, fillPercentage),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true
        );
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
