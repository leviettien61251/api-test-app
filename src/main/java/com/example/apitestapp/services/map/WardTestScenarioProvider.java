package com.example.apitestapp.services.map;

import com.example.apitestapp.models.dto.*;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;
import java.util.Map;

public class WardTestScenarioProvider implements ApiScenarioProvider {

    private static List<ApiSetupRequest> createStepSetupRequests(String suffix) {
        return List.of(
                new ApiSetupRequest(
                        "Thêm dữ liệu mẫu Map",
                        "POST",
                        "/api/v1/insert-map-test",
                        """
                                {
                                    "buildingCode": "B-WARD-%s",
                                    "buildingName": "Building Step %s",
                                    "imageUrl": "https://example.com/step-test.jpg",
                                    "scaleX": 10,
                                    "scaleY": 10
                                }
                                """.formatted(suffix, suffix),
                        Map.of("Authorization", "Bearer ${token}"),
                        List.of("1000", "200", "201"),
                        true,
                        List.of(new ApiResponseVariable("mapId", "data.0.id"))
                ),
                new ApiSetupRequest(
                        "Thêm dữ liệu mẫu Node bắt đầu",
                        "POST",
                        "/api/v1/insert-node-test",
                        """
                                {
                                    "mapId": ${mapId},
                                    "xCoordinate": 1,
                                    "yCoordinate": 3,
                                    "type": "abc",
                                    "isPassable": true
                                }
                                """,
                        Map.of("Authorization", "Bearer ${token}"),
                        List.of("1000"),
                        true,
                        List.of(new ApiResponseVariable("map_node_id", "data.id"))
                )
        );
    }

    private static String createWardRequestBody(String name, String status) {
        return """
                {
                   "map_node_id": ${map_node_id},
                   "name": %s,
                   "ward_status": %s
                }
                """.formatted(toJsonString(name), toJsonString(status));
    }

    private static String toJsonString(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value + "\"";
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Yêu câu hợp lệ với đấy đủ tham số - 1000")
                        .setupRequests(createStepSetupRequests("S1"))
                        .requestBody(createWardRequestBody("Ward 1", "open"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Map_node_id null - 2001")
                        .requestBody(
                                """
                                        {
                                           "map_node_id": null,
                                           "name": "Ward 2",
                                           "ward_status": "open"
                                        }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("Ward name null - 2001")
                        .setupRequests(createStepSetupRequests("S3"))
                        .requestBody(createWardRequestBody(null, "open"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("Ward status null - 2001")
                        .setupRequests(createStepSetupRequests("S4"))
                        .requestBody(createWardRequestBody("Ward 4", null))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("Ward name bị để trống - 2001")
                        .setupRequests(createStepSetupRequests("S5"))
                        .requestBody(createWardRequestBody("", "open"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("Ward status bị để trống - 2001")
                        .setupRequests(createStepSetupRequests("S6"))
                        .requestBody(createWardRequestBody("Ward 6", ""))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("Node id không tồn tại - 4002")
                        .setupRequests(createStepSetupRequests("S7"))
                        .requestBody(
                                """
                                        {
                                           "map_node_id": 9999,
                                           "name": "Ward 7",
                                           "ward_status": "open"
                                        }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("4002")
                        .expectedStatus("FAILURE")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("Node id không phải kiểu số nguyên - 400")
                        .setupRequests(createStepSetupRequests("S8"))
                        .requestBody(
                                """
                                        {
                                           "map_node_id": "abcd",
                                           "name": "Ward 8",
                                           "ward_status": "open"
                                        }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("400")
                        .expectedStatus("FAILURE")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 9")
                        .description("Ward name chứa ký tự đặc biệt - 2002")
                        .setupRequests(createStepSetupRequests("S9"))
                        .requestBody(createWardRequestBody("Ward 9;DROP TABLE ward_test;", "open"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 10")
                        .description("Ward status chứa ký tự đặc biệt - 2002")
                        .setupRequests(createStepSetupRequests("S10"))
                        .requestBody(createWardRequestBody("Ward 10", "open;DROP TABLE ward_test;"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build()

        );
        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Map Module")
                .apiLabel("POST /api/v1/map/insert-ward")
                .endpoint("/api/v1/map/insert-ward")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu Ward",
                                "DELETE",
                                "/api/v1/clean/map/ward",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu Map",
                                "DELETE",
                                "/api/v1/clean/map/node",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu Map",
                                "DELETE",
                                "/api/v1/clean/map",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ), new ApiCleanupRequest(
                                "Clean login test data",
                                "DELETE",
                                "/api/v1/clean/login",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean signup test data",
                                "DELETE",
                                "/api/v1/clean/signup",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean user test data",
                                "DELETE",
                                "/api/v1/clean/user-test",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                ))
                .build();
    }
}
