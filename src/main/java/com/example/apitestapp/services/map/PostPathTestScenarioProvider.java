package com.example.apitestapp.services.map;

import com.example.apitestapp.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostPathTestScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/map/path";
    private static final Map<String, String> AUTH_HEADERS = Map.of("Authorization", "Bearer ${token}");

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Scenario 1", "Body null - MISSING_BODY", "null", "2006", false),
                scenario("Scenario 2", "Body rỗng {}", "{}", "2001", false),
                scenario("Scenario 3", "Thiếu phoneNumber", """
                        {
                            "startNodeId": 1,
                            "endNodeId": 2,
                            "totalDistance": 120.5,
                            "pathStatus": 1
                        }
                        """, "2001", false),
                scenario("Scenario 4", "Thiếu startNodeId", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", null, "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 5", "Thiếu endNodeId", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", null, "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 6", "Thiếu totalDistance", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", null, "pathStatus", "1"), "2001", false),
                scenario("Scenario 7", "Thiếu pathStatus", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", null), "2001", false),
                scenario("Scenario 8", "phoneNumber null", pathBody("phoneNumber", "null", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 9", "startNodeId null", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "null", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 10", "endNodeId null", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "null", "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 11", "totalDistance null", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "null", "pathStatus", "1"), "2001", false),
                scenario("Scenario 12", "pathStatus null", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "null"), "2001", false),
                scenario("Scenario 13", "phoneNumber rỗng", pathBody("phoneNumber", "\"\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 14", "phoneNumber chỉ chứa whitespace", pathBody("phoneNumber", "\"   \"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 15", "numeric field là blank string", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "\"\"", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2001", false),
                scenario("Scenario 16", "phoneNumber không phải String", pathBody("phoneNumber", "123", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2002", false),
                scenario("Scenario 17", "phoneNumber không tồn tại", pathBody("phoneNumber", "\"missing-user-id\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "3007", false),
                scenario("Scenario 18", "startNodeId string", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "\"1\"", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2002", false),
                scenario("Scenario 19", "endNodeId decimal", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2.5", "totalDistance", "120.5", "pathStatus", "1"), "2002", false),
                scenario("Scenario 20", "startNodeId <= 0", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "0", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "1"), "2003", false),
                scenario("Scenario 21", "endNodeId <= 0", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "-2", "totalDistance", "120.5", "pathStatus", "1"), "2003", false),
                scenario("Scenario 22", "startNodeId không tồn tại", pathBody("phoneNumber", "\"${phoneNumber}\"", "startNodeId", "999999", "endNodeId", "${endNodeId}", "totalDistance", "120.5", "pathStatus", "1"), "4002", true),
                scenario("Scenario 23", "endNodeId không tồn tại", pathBody("phoneNumber", "\"${phoneNumber}\"", "startNodeId", "${startNodeId}", "endNodeId", "999999", "totalDistance", "120.5", "pathStatus", "1"), "4002", true),
                scenario("Scenario 24", "totalDistance string", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "\"120.5\"", "pathStatus", "1"), "2002", false),
                scenario("Scenario 25", "totalDistance <= 0", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "0", "pathStatus", "1"), "2003", false),
                scenario("Scenario 26", "pathStatus string", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "\"1\""), "2002", false),
                scenario("Scenario 27", "pathStatus < 0", pathBody("phoneNumber", "\"phoneNumber\"", "startNodeId", "1", "endNodeId", "2", "totalDistance", "120.5", "pathStatus", "-1"), "2003", false),
                scenario("Scenario 28", "Body valid camelCase", pathBody("phoneNumber", "\"${phoneNumber}\"", "startNodeId", "${startNodeId}", "endNodeId", "${endNodeId}", "totalDistance", "120.5", "pathStatus", "1"), "1000", true),
                scenario("Scenario 29", "Body valid snake_case", pathBody("user_id", "\"${phoneNumber}\"", "start_node_id", "${startNodeId}", "end_node_id", "${endNodeId}", "total_distance", "120.5", "path_status", "1"), "1000", true),
                scenario("Scenario 30", "JSON invalid - INVALID_BODY", "{", "2005", false)
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Map Module")
                .apiLabel("POST /api/v1/map/path")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(27).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(cleanupRequests())
                .build();
    }

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            String requestBody,
                                            String expectedCode,
                                            boolean seedData) {
        return ApiTestScenario.builder()
                .scenario(name)
                .description(description)
                .setupRequests(seedData ? createPathSetupRequests(name) : List.of())
                .headers(AUTH_HEADERS)
                .requestBody(requestBody)
                .expectedCode(expectedCode)
                .expectedStatus(expectedCode.equals("1000") ? "SUCCESS" : "FAILURE")
                .build();
    }

    private static List<ApiSetupRequest> createPathSetupRequests(String suffix) {
        List<ApiSetupRequest> setupRequests = new ArrayList<>();
        setupRequests.add(new ApiSetupRequest(
                "Thêm dữ liệu mồi Signup",
                "POST",
                "/api/v1/signup",
                """
                        {
                            "phoneNumber": "0987%s",
                            "password": "111111"
                        }
                        """.formatted(phoneSuffix(suffix)),
                AUTH_HEADERS,
                List.of("1000", "200", "201", "3006"),
                true
        ));

        setupRequests.add(new ApiSetupRequest(
                "Thêm dữ liệu mồi Signup",
                "POST",
                "/api/v1/login",
                """
                        {
                            "phoneNumber": "0987%s",
                            "password": "111111"
                        }
                        """.formatted(phoneSuffix(suffix)),
                AUTH_HEADERS,
                List.of("1000", "200", "201", "3006"),
                true,
                List.of(new ApiResponseVariable("phoneNumber", "data.phoneNumber"))
        ));

        setupRequests.add(new ApiSetupRequest(
                "Thêm dữ liệu mẫu Map",
                "POST",
                "/api/v1/insert-map-test",
                """
                        {
                            "buildingCode": "B-PATH-%s",
                            "buildingName": "Building Path %s",
                            "imageUrl": "https://example.com/path-test.jpg",
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
                "Thêm dữ liệu mẫu Node bắt đầu",
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
                List.of(new ApiResponseVariable("startNodeId", "data.id"))
        ));
        setupRequests.add(new ApiSetupRequest(
                "Thêm dữ liệu mẫu Node kết thúc",
                "POST",
                "/api/v1/insert-node-test",
                """
                        {
                            "mapId": ${mapId},
                            "xCoordinate": 2,
                            "yCoordinate": 4,
                            "type": "room",
                            "isPassable": true
                        }
                        """,
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true,
                List.of(new ApiResponseVariable("endNodeId", "data.id"))
        ));
        return setupRequests;
    }

    private static String phoneSuffix(String suffix) {
        int hash = Math.abs(suffix.hashCode() % 1000000);
        return String.format("%06d", hash);
    }

    private static List<ApiCleanupRequest> cleanupRequests() {
        return List.of(
                cleanup("Clean path test data", "/api/v1/clean/map/path"),
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

    private static String pathBody(String phoneNumberField,
                                   String phoneNumber,
                                   String startField,
                                   String startNodeId,
                                   String endField,
                                   String endNodeId,
                                   String distanceField,
                                   String totalDistance,
                                   String statusField,
                                   String pathStatus) {
        List<String> fields = new ArrayList<>();
        addField(fields, phoneNumberField, phoneNumber);
        addField(fields, startField, startNodeId);
        addField(fields, endField, endNodeId);
        addField(fields, distanceField, totalDistance);
        addField(fields, statusField, pathStatus);
        return "{\n    " + String.join(",\n    ", fields) + "\n}";
    }

    private static void addField(List<String> fields, String name, String value) {
        if (value != null) {
            fields.add("\"%s\": %s".formatted(name, value));
        }
    }
}
