package com.example.apitestapp.services.map;


import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class MapTestScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Yêu cầu hợp lệ với đầy đủ tham số - Nên Thành công")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S1", "Building Map S1", "https://example.com/map-s1.jpg", "5.1", "5.1"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Thiếu buildingCode")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                            "buildingName": "Building Map S2",
                                            "imageUrl": "https://example.com/map-s2.jpg",
                                            "scaleX": 5.1,
                                            "scaleY": 5.1
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("buildingCode null")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody(null, "Building Map S3", "https://example.com/map-s3.jpg", "5.1", "5.1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("buildingCode rỗng")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("", "Building Map S4", "https://example.com/map-s4.jpg", "5.1", "5.1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("Thiếu buildingName")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                            "buildingCode": "B-MAP-S5",
                                            "imageUrl": "https://example.com/map-s5.jpg",
                                            "scaleX": 5.1,
                                            "scaleY": 5.1
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("buildingName null")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S6", null, "https://example.com/map-s6.jpg", "5.1", "5.1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("buildingName rỗng")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S7", "", "https://example.com/map-s7.jpg", "5.1", "5.1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("Thiếu imageUrl")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                            "buildingCode": "B-MAP-S8",
                                            "buildingName": "Building Map S8",
                                            "scaleX": 5.1,
                                            "scaleY": 5.1
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 9")
                        .description("imageUrl null")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S9", "Building Map S9", null, "5.1", "5.1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 10")
                        .description("imageUrl rỗng")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S10", "Building Map S10", "", "5.1", "5.1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 11")
                        .description("Thiếu scaleX")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                            "buildingCode": "B-MAP-S11",
                                            "buildingName": "Building Map S11",
                                            "imageUrl": "https://example.com/map-s11.jpg",
                                            "scaleY": 5.1
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 12")
                        .description("scaleX null")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S12", "Building Map S12", "https://example.com/map-s12.jpg", "null", "5.1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 13")
                        .description("scaleX không phải kiểu số")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S13", "Building Map S13", "https://example.com/map-s13.jpg", "\"abc\"", "5.1"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 14")
                        .description("scaleX là số 0")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S14", "Building Map S14", "https://example.com/map-s14.jpg", "0", "5.1"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 15")
                        .description("scaleX là số âm")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S15", "Building Map S15", "https://example.com/map-s15.jpg", "-1", "5.1"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 16")
                        .description("Thiếu scaleY")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                            "buildingCode": "B-MAP-S16",
                                            "buildingName": "Building Map S16",
                                            "imageUrl": "https://example.com/map-s16.jpg",
                                            "scaleX": 5.1
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 17")
                        .description("scaleY null")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S17", "Building Map S17", "https://example.com/map-s17.jpg", "5.1", "null"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 18")
                        .description("scaleY không phải kiểu số")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S18", "Building Map S18", "https://example.com/map-s18.jpg", "5.1", "\"abc\""))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 19")
                        .description("scaleY là số 0")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S19", "Building Map S19", "https://example.com/map-s19.jpg", "5.1", "0"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 20")
                        .description("scaleY là số âm")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createMapRequestBody("B-MAP-S20", "Building Map S20", "https://example.com/map-s20.jpg", "5.1", "-1"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 21")
                        .description("buildingCode đã tồn tại")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .setupRequests(createDuplicateMapSetupRequests())
                        .requestBody(createMapRequestBody("B-MAP-DUP", "Building Map Duplicate Updated", "https://example.com/map-dup-2.jpg", "5.1", "5.1"))
                        .expectedCode("4006")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Map Module")
                .apiLabel("POST /api/v1/insert-map-test")
                .endpoint("/api/v1/insert-map-test")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu Map",
                                "DELETE",
                                "/api/v1/clean/map",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                ))
                .build();
    }

    private static List<ApiSetupRequest> createDuplicateMapSetupRequests() {
        return List.of(
                new ApiSetupRequest(
                        "Thêm dữ liệu mồi Map trùng buildingCode",
                        "POST",
                        "/api/v1/insert-map-test",
                        createMapRequestBody("B-MAP-DUP", "Building Map Duplicate", "https://example.com/map-dup.jpg", "5.1", "5.1"),
                        Map.of("Authorization", "Bearer ${token}"),
                        List.of("1000"),
                        true
                )
        );
    }

    private static String createMapRequestBody(String buildingCode,
                                               String buildingName,
                                               String imageUrl,
                                               String scaleX,
                                               String scaleY) {
        return """
                {
                    "buildingCode": %s,
                    "buildingName": %s,
                    "imageUrl": %s,
                    "scaleX": %s,
                    "scaleY": %s
                }
                """.formatted(
                toJsonString(buildingCode),
                toJsonString(buildingName),
                toJsonString(imageUrl),
                scaleX,
                scaleY
        );
    }

    private static String toJsonString(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value + "\"";
    }
}
