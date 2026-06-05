package com.example.apitestapp.services.map;

import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class PostAreaTestScenarioProvider implements ApiScenarioProvider {

    private static final String ENDPOINT = "/api/v1/map/area";
    private static final Map<String, String> AUTH_HEADERS = Map.of("Authorization", "Bearer ${token}");

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                scenario("Scenario 1", "Body null - MISSING_BODY", "null", "2006", false),
                scenario("Scenario 2", "Body rỗng {}", "{}", "2001", false),
                scenario("Scenario 3", "Thiếu areaId", """
                        {
                            "mapId": 1
                        }
                        """, "2001", false),
                scenario("Scenario 4", "Thiếu mapId", """
                        {
                            "areaId": "AREA_MISSING_MAP"
                        }
                        """, "2001", false),
                scenario("Scenario 5", "areaId null", areaBody("null", "1"), "2001", false),
                scenario("Scenario 6", "mapId null", areaBody("\"AREA_MAP_NULL\"", "null"), "2001", false),
                scenario("Scenario 7", "areaId rỗng", areaBody("\"\"", "1"), "2001", false),
                scenario("Scenario 8", "areaId chỉ chứa whitespace", areaBody("\"   \"", "1"), "2001", false),
                scenario("Scenario 9", "areaId không phải String", areaBody("123", "1"), "2002", false),
                scenario("Scenario 10", "mapId không phải integer", areaBody("\"AREA_MAP_STRING\"", "\"1\""), "2002", false),
                scenario("Scenario 11", "mapId là decimal", areaBody("\"AREA_MAP_DECIMAL\"", "1.5"), "2002", false),
                scenario("Scenario 12", "mapId <= 0", areaBody("\"AREA_MAP_ZERO\"", "0"), "2003", false),
                scenario("Scenario 13", "mapId không tồn tại", areaBody("\"AREA_MAP_NOT_FOUND\"", "999999"), "4001", false),
                scenario("Scenario 14", "areaId đã tồn tại", areaBody("\"AREA_DUP\"", "${mapId}"), "2003", true, duplicateAreaSetupRequests()),
                scenario("Scenario 15", "JSON invalid - INVALID_BODY", "{", "2005", false),
                scenario("Scenario 16", "Body valid", areaBody("\"AREA_VALID\"", "${mapId}"), "1000", true)
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Map Module")
                .apiLabel("POST /api/v1/map/area")
                .endpoint(ENDPOINT)
                .sampleRequestBody(scenarios.get(15).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(cleanupRequests("/api/v1/clean/map/area", "/api/v1/clean/map"))
                .build();
    }

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            String requestBody,
                                            String expectedCode,
                                            boolean seedMap) {
        return scenario(name, description, requestBody, expectedCode, seedMap, seedMap ? createMapSetupRequests(name) : List.of());
    }

    private static ApiTestScenario scenario(String name,
                                            String description,
                                            String requestBody,
                                            String expectedCode,
                                            boolean seedMap,
                                            List<ApiSetupRequest> setupRequests) {
        return ApiTestScenario.builder()
                .scenario(name)
                .description(description)
                .setupRequests(setupRequests)
                .headers(AUTH_HEADERS)
                .requestBody(requestBody)
                .expectedCode(expectedCode)
                .expectedStatus(expectedCode.equals("1000") ? "SUCCESS" : "FAILURE")
                .build();
    }

    private static List<ApiSetupRequest> duplicateAreaSetupRequests() {
        return List.of(
                createMapSetupRequests("DUP").get(0),
                new ApiSetupRequest(
                        "Tạo dữ liệu Area trùng",
                        "POST",
                        ENDPOINT,
                        areaBody("\"AREA_DUP\"", "${mapId}"),
                        AUTH_HEADERS,
                        List.of("1000", "200", "201"),
                        true
                )
        );
    }

    private static List<ApiSetupRequest> createMapSetupRequests(String suffix) {
        return List.of(new ApiSetupRequest(
                "Thêm dữ liệu mẫu Map",
                "POST",
                "/api/v1/insert-map-test",
                """
                        {
                            "buildingCode": "B-AREA-%s",
                            "buildingName": "Building Area %s",
                            "imageUrl": "https://example.com/area-test.jpg",
                            "scaleX": 10,
                            "scaleY": 10
                        }
                        """.formatted(suffix.replace(" ", "-"), suffix),
                AUTH_HEADERS,
                List.of("1000", "200", "201"),
                true,
                List.of(new ApiResponseVariable("mapId", "data.0.id"))
        ));
    }

    private static List<ApiCleanupRequest> cleanupRequests(String... endpoints) {
        List<ApiCleanupRequest> cleanupRequests = new java.util.ArrayList<>();
        for (String endpoint : endpoints) {
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
        cleanupRequests.add(new ApiCleanupRequest("Clean login test data", "DELETE", "/api/v1/clean/login", "", AUTH_HEADERS, List.of("1000", "200", "204", "201"), true));
        cleanupRequests.add(new ApiCleanupRequest("Clean signup test data", "DELETE", "/api/v1/clean/signup", "", AUTH_HEADERS, List.of("1000", "200", "204", "201"), true));
        cleanupRequests.add(new ApiCleanupRequest("Clean user test data", "DELETE", "/api/v1/clean/user-test", "", AUTH_HEADERS, List.of("1000", "200", "204", "201"), true));
        return cleanupRequests;
    }

    private static String areaBody(String areaId, String mapId) {
        return """
                {
                    "areaId": %s,
                    "mapId": %s
                }
                """.formatted(areaId, mapId);
    }
}
