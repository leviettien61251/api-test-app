package com.example.apitestapp.services.map;

import com.example.apitestapp.models.dto.*;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostHeatmapTestScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/map/heatmap";
    private static final Map<String, String> AUTH_HEADERS = Map.of("Authorization", "Bearer ${token}");

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            String requestBody,
                                            String expectedCode,
                                            boolean seedNode) {
        return ApiTestScenario.builder()
                .scenario(name)
                .description(description)
                .setupRequests(seedNode ? createNodeSetupRequests(name) : List.of())
                .headers(AUTH_HEADERS)
                .requestBody(requestBody)
                .expectedCode(expectedCode)
                .expectedStatus(expectedCode.equals("1000") ? "SUCCESS" : "FAILURE")
                .build();
    }

    private static List<ApiSetupRequest> createNodeSetupRequests(String suffix) {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.add(new ApiSetupRequest(
                "Thêm dữ liệu mẫu Map",
                "POST",
                "/api/v1/insert-map-test",
                """
                        {
                            "buildingCode": "B-HEATMAP-%s",
                            "buildingName": "Building Heatmap %s",
                            "imageUrl": "https://example.com/heatmap-test.jpg",
                            "scaleX": 10,
                            "scaleY": 10
                        }
                        """.formatted(suffix.replace(" ", "-"), suffix),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true,
                List.of(new ApiResponseVariable("mapId", "data.0.id"))
        ));
        setupRequests.add(new ApiSetupRequest(
                "Thêm dữ liệu mẫu Node",
                "POST",
                "/api/v1/insert-node-test",
                """
                        {
                            "mapId": ${mapId},
                            "xCoordinate": 1,
                            "yCoordinate": 3,
                            "type": "room",
                            "isPassable": true
                        }
                        """,
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true,
                List.of(new ApiResponseVariable("nodeId", "data.id"))
        ));
        return setupRequests;
    }

    private static List<ApiCleanupRequest> cleanupRequests() {
        return List.of(
                cleanup("Clean heatmap test data", "/api/v1/clean/map/heatmap"),
                cleanup("Clean node test data", "/api/v1/clean/map/node"),
                cleanup("Clean map test data", "/api/v1/clean/map"),
                cleanup("Clean login test data", "/api/v1/clean/login"),
                cleanup("Clean signup test data", "/api/v1/clean/signup"),
                cleanup("Clean user test data", "/api/v1/clean/user-test")
        );
    }

    private static ApiCleanupRequest cleanup(String name, String endpoint) {
        return new ApiCleanupRequest(name, "DELETE", endpoint, "", AUTH_HEADERS, List.of("1000", "200", "204", "201"), true);
    }

    private static String heatmapBody(String nodeField,
                                      String nodeId,
                                      String densityField,
                                      String densityScore,
                                      String recordedField,
                                      String recordedAt) {
        return """
                {
                    "%s": %s,
                    "%s": %s,
                    "%s": %s
                }
                """.formatted(nodeField, nodeId, densityField, densityScore, recordedField, recordedAt);
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Scenario 1", "Body null - MISSING_BODY", "null", "2006", false),
                scenario("Scenario 2", "Body rỗng {}", "{}", "2001", false),
                scenario("Scenario 3", "Thiếu nodeId", """
                        {
                            "densityScore": 80,
                            "recordedAt": "2026-06-03 05:30:00"
                        }
                        """, "2001", false),
                scenario("Scenario 4", "Thiếu densityScore", """
                        {
                            "nodeId": 1,
                            "recordedAt": "2026-06-03 05:30:00"
                        }
                        """, "2001", false),
                scenario("Scenario 5", "Thiếu recordedAt", """
                        {
                            "nodeId": 1,
                            "densityScore": 80
                        }
                        """, "2001", false),
                scenario("Scenario 6", "nodeId null", heatmapBody("nodeId", "null", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "2001", false),
                scenario("Scenario 7", "densityScore null", heatmapBody("nodeId", "1", "densityScore", "null", "recordedAt", "\"2026-06-03 05:30:00\""), "2001", false),
                scenario("Scenario 8", "recordedAt null", heatmapBody("nodeId", "1", "densityScore", "80", "recordedAt", "null"), "2001", false),
                scenario("Scenario 9", "nodeId blank string", heatmapBody("nodeId", "\"   \"", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "2001", false),
                scenario("Scenario 10", "densityScore blank string", heatmapBody("nodeId", "1", "densityScore", "\"\"", "recordedAt", "\"2026-06-03 05:30:00\""), "2001", false),
                scenario("Scenario 11", "recordedAt blank string", heatmapBody("nodeId", "1", "densityScore", "80", "recordedAt", "\"   \""), "2001", false),
                scenario("Scenario 12", "nodeId string", heatmapBody("nodeId", "\"1\"", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "2002", false),
                scenario("Scenario 13", "nodeId decimal", heatmapBody("nodeId", "1.5", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "2002", false),
                scenario("Scenario 14", "nodeId <= 0", heatmapBody("nodeId", "0", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "2003", false),
                scenario("Scenario 15", "nodeId không tồn tại", heatmapBody("nodeId", "999999", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "4002", false),
                scenario("Scenario 16", "densityScore string", heatmapBody("nodeId", "1", "densityScore", "\"80\"", "recordedAt", "\"2026-06-03 05:30:00\""), "2002", false),
                scenario("Scenario 17", "densityScore decimal", heatmapBody("nodeId", "1", "densityScore", "80.5", "recordedAt", "\"2026-06-03 05:30:00\""), "2002", false),
                scenario("Scenario 18", "densityScore < 0", heatmapBody("nodeId", "1", "densityScore", "-1", "recordedAt", "\"2026-06-03 05:30:00\""), "2003", false),
                scenario("Scenario 19", "recordedAt không phải String", heatmapBody("nodeId", "1", "densityScore", "80", "recordedAt", "123"), "2002", false),
                scenario("Scenario 20", "recordedAt format sai", heatmapBody("nodeId", "1", "densityScore", "80", "recordedAt", "\"03/06/2026\""), "2003", false),
                scenario("Scenario 21", "recordedAt SQL timestamp valid", heatmapBody("nodeId", "${nodeId}", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "1000", true),
                scenario("Scenario 22", "recordedAt ISO local valid", heatmapBody("nodeId", "${nodeId}", "densityScore", "80", "recordedAt", "\"2026-06-03T05:30:00\""), "1000", true),
                scenario("Scenario 23", "recordedAt ISO offset valid", heatmapBody("nodeId", "${nodeId}", "densityScore", "80", "recordedAt", "\"2026-06-03T05:30:00+07:00\""), "1000", true),
                scenario("Scenario 24", "Body valid camelCase", heatmapBody("nodeId", "${nodeId}", "densityScore", "80", "recordedAt", "\"2026-06-03 05:30:00\""), "1000", true),
                scenario("Scenario 25", "Body valid snake_case", heatmapBody("node_id", "${nodeId}", "density_score", "80", "recorded_at", "\"2026-06-03T05:30:00+07:00\""), "1000", true),
                scenario("Scenario 26", "JSON invalid - INVALID_BODY", "{", "2005", false)
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Map Module")
                .apiLabel("POST /api/v1/map/heatmap")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(23).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(cleanupRequests())
                .build();
    }
}
