package com.example.apitestapp.services.map;

import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class NodeTestScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Yêu cầu hợp lệ với đầy đủ tham số - Nên Thành công")
                        .setupRequests(
                                List.of(
                                        new ApiSetupRequest(
                                                "Ensure user is already registered",
                                                "POST",
                                                "/api/v1/insert-map-test",
                                                """
                                                        {
                                                            "buildingCode": "B-A1",
                                                            "buildingName": "Building A1",
                                                            "imageUrl": " https://example.com/BA1.jpg ",
                                                            "scaleX": 30,
                                                            "scaleY": 30.3
                                                          }
                                                        """,
                                                Map.of("Authorization", "Bearer ${token}"),
                                                List.of("1000"),
                                                true,
                                                List.of(new ApiResponseVariable("mapId", "data.0.id"))
                                        )
                                )
                        )
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Thiếu Id Map null")
                        .requestBody(
                                """
                                                {
                                                    "mapId": null,
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("Thiếu mapId")
                        .requestBody(
                                """
                                                {
                                                   "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("mapId không phải kiểu số")
                        .requestBody(
                                """
                                                {
                                                    "mapId": "abc",
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("mapId là số 0")
                        .requestBody(
                                """
                                                {
                                                    "mapId": 0,
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("mapId là số âm")
                        .requestBody(
                                """
                                                {
                                                    "mapId": -5,
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("mapId không tồn tại")
                        .requestBody(
                                """
                                                {
                                                    "mapId": 999999,
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("4001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("Thiếu xCoordinate")
                        .setupRequests(createMapSetupRequests("S8"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 9")
                        .description("xCoordinate không phải kiểu số")
                        .setupRequests(createMapSetupRequests("S9"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": "abc",
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 10")
                        .description("xCoordinate là số âm")
                        .setupRequests(createMapSetupRequests("S10"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": -1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 11")
                        .description("Thiếu yCoordinate")
                        .setupRequests(createMapSetupRequests("S11"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 12")
                        .description("yCoordinate không phải kiểu số")
                        .setupRequests(createMapSetupRequests("S12"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "yCoordinate": "abc",
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 13")
                        .description("yCoordinate là số âm")
                        .setupRequests(createMapSetupRequests("S13"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "yCoordinate": -3,
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 14")
                        .description("Thiếu type")
                        .setupRequests(createMapSetupRequests("S14"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 15")
                        .description("type rỗng")
                        .setupRequests(createMapSetupRequests("S15"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 16")
                        .description("Thiếu isPassable")
                        .setupRequests(createMapSetupRequests("S16"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room"
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 17")
                        .description("isPassable không phải kiểu boolean")
                        .setupRequests(createMapSetupRequests("S17"))
                        .requestBody(
                                """
                                                {
                                                    "mapId": ${mapId},
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "room",
                                                    "isPassable": "true"
                                                  }
                                        """
                        )
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Map Module")
                .apiLabel("POST /api/v1/insert-node-test")
                .endpoint("/api/v1/insert-node-test")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
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
                        ),new ApiCleanupRequest(
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

    private static List<ApiSetupRequest> createMapSetupRequests(String suffix) {
        return List.of(
                new ApiSetupRequest(
                        "Tạo dữ liệu mồi Map",
                        "POST",
                        "/api/v1/insert-map-test",
                        """
                                {
                                    "buildingCode": "B-NODE-%s",
                                    "buildingName": "Building Node %s",
                                    "imageUrl": "https://example.com/node-test.jpg",
                                    "scaleX": 30,
                                    "scaleY": 30
                                  }
                                """.formatted(suffix, suffix),
                        Map.of("Authorization", "Bearer ${token}"),
                        List.of("1000"),
                        true,
                        List.of(new ApiResponseVariable("mapId", "data.0.id"))
                )
        );
    }
}
