package com.example.apitestapp.services.MapScenarios;

import com.example.apitestapp.services.*;

import java.util.List;

public class StepTestScenarioProvider implements ApiScenarioProvider {


    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Yêu cầu hợp lệ với tham số đầy đủ - Nên Thành công")
                        .setupRequests(createStepSetupRequests("S1"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("mapId null - Mã 2001")
                        .requestBody(
                                """
                                        {
                                            "mapId": null,
                                            "startNodeId": 1,
                                            "endNodeId": 2,
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("Thiếu mapId - Mã 2001")
                        .requestBody(
                                """
                                        {
                                            "startNodeId": 1,
                                            "endNodeId": 2,
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("startNodeId null - Mã 2001")
                        .requestBody(
                                """
                                        {
                                            "mapId": 1,
                                            "startNodeId": null,
                                            "endNodeId": 2,
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("Thiếu startNodeId - Mã 2001")
                        .requestBody(
                                """
                                        {
                                            "mapId": 1,
                                            "endNodeId": 2,
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("endNodeId null - Mã 2001")
                        .requestBody(
                                """
                                        {
                                            "mapId": 1,
                                            "startNodeId": 1,
                                            "endNodeId": null,
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("Thiếu endNodeId - Mã 2001")
                        .requestBody(
                                """
                                        {
                                            "mapId": 1,
                                            "startNodeId": 1,
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("distance null - Mã 5000")
                        .setupRequests(createStepSetupRequests("S8"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": ,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("5000")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 9")
                        .description("direction rỗng - Mã 2001")
                        .setupRequests(createStepSetupRequests("S9"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 10")
                        .description("direction chỉ chứa khoảng trắng - Mã 2001")
                        .setupRequests(createStepSetupRequests("S10"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "   ",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 11")
                        .description("instruction rỗng - Mã 2001")
                        .setupRequests(createStepSetupRequests("S11"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": ""
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 12")
                        .description("instruction chỉ chứa khoảng trắng - Mã 2001")
                        .setupRequests(createStepSetupRequests("S12"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "   "
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 13")
                        .description("mapId không tồn tại - Mã 4001")
                        .setupRequests(createStepSetupRequests("S13"))
                        .requestBody(
                                """
                                        {
                                            "mapId": 999999,
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("4001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 14")
                        .description("startNodeId không tồn tại - Mã 4002")
                        .setupRequests(createStepSetupRequests("S14"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": 999999,
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("4002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 15")
                        .description("endNodeId không tồn tại - Mã 4002")
                        .setupRequests(createStepSetupRequests("S15"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": 999999,
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("4002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 16")
                        .description("distance null gây lỗi xử lý - Mã 2001")
                        .setupRequests(createStepSetupRequests("S16"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": null,
                                            "direction": "up",
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 17")
                        .description("direction null gây lỗi xử lý - Mã 2001")
                        .setupRequests(createStepSetupRequests("S17"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": null,
                                            "instruction": "up up up"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 18")
                        .description("instruction null gây lỗi xử lý - Mã 2001")
                        .setupRequests(createStepSetupRequests("S18"))
                        .requestBody(
                                """
                                        {
                                            "mapId": ${mapId},
                                            "startNodeId": ${startNodeId},
                                            "endNodeId": ${endNodeId},
                                            "distance": 10,
                                            "direction": "up",
                                            "instruction": null
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build()
        );


        return new ApiScenarioDefinition(
                "Collections",
                "Map Module",
                "POST /api/v1/map/insert-step",
                "/api/v1/map/insert-step",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(
                        new ApiCleanupRequest(
                                "Clean step test data",
                                "DELETE",
                                "/api/v1/map/step/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean node test data",
                                "DELETE",
                                "/api/v1/map/node/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean map test data",
                                "DELETE",
                                "/api/v1/map/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ))
        );
    }

    private static List<ApiSetupRequest> createStepSetupRequests(String suffix) {
        return List.of(
                new ApiSetupRequest(
                        "Thêm dữ liệu mẫu Map",
                        "POST",
                        "/api/v1/insert-map-test",
                        """
                                {
                                    "buildingCode": "B-STEP-%s",
                                    "buildingName": "Building Step %s",
                                    "imageUrl": "https://example.com/step-test.jpg",
                                    "scaleX": 10,
                                    "scaleY": 10
                                }
                                """.formatted(suffix, suffix),
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
                        List.of("1000"),
                        true,
                        List.of(new ApiResponseVariable("startNodeId", "data.id"))
                ),
                new ApiSetupRequest(
                        "Thêm dữ liệu mẫu Node kết thúc",
                        "POST",
                        "/api/v1/insert-node-test",
                        """
                                {
                                    "mapId": ${mapId},
                                    "xCoordinate": 2,
                                    "yCoordinate": 4,
                                    "type": "abc",
                                    "isPassable": true
                                }
                                """,
                        List.of("1000"),
                        true,
                        List.of(new ApiResponseVariable("endNodeId", "data.id"))
                )
        );
    }
}
