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
                        .setupRequests(
                                List.of(
                                        new ApiSetupRequest(
                                                "Thêm dữ liệu mẫu Map",
                                                "POST",
                                                "/api/v1/insert-map-test",
                                                """
                                                        {
                                                            "buildingCode": " B-A2 ",
                                                            "buildingName": " Building A2 ",
                                                            "imageUrl": " https://example.com/BA1.jpg ",
                                                            "scaleX": 10,
                                                            "scaleY": 10
                                                        }
                                                        """,
                                                List.of("1000", "200", "201"),
                                                true,
                                                List.of(new ApiResponseVariable("mapId", "data.0.id"))
                                        ),
                                        new ApiSetupRequest(
                                                "Thêm dữ liệu mẫu Node",
                                                "POST",
                                                "/api/v1/insert-node-test",
                                                """
                                                        {
                                                            "mapId": ${mapId},
                                                            "xCoordinate": "1",
                                                            "yCoordinate": "3",
                                                            "type": "abc",
                                                            "isPassable": true
                                                        }
                                                        """,
                                                List.of("1000"),
                                                true,
                                                List.of(new ApiResponseVariable("startNodeId", "data.id")
                                                )
                                        ),
                                        new ApiSetupRequest(
                                                "Thêm dữ liệu mẫu Node",
                                                "POST",
                                                "/api/v1/insert-node-test",
                                                """
                                                        {
                                                            "mapId": ${mapId},
                                                            "xCoordinate": "1",
                                                            "yCoordinate": "3",
                                                            "type": "abc",
                                                            "isPassable": true
                                                        }
                                                        """,
                                                List.of("1000"),
                                                true,
                                                List.of(new ApiResponseVariable("endNodeId", "data.id"))
                                        )
                                )
                        )
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
}
