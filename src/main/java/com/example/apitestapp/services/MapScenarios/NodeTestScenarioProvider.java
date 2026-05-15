package com.example.apitestapp.services.MapScenarios;

import com.example.apitestapp.services.*;

import java.util.List;

public class NodeTestScenarioProvider implements ApiScenarioProvider {
    /**
     * @return
     */
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
                                                    "xCoordinate": "1",
                                                    "yCoordinate": "3",
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
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
                                                    "xCoordinate": "1",
                                                    "yCoordinate": "3",
                                                    "type": "room",
                                                    "isPassable": true
                                                  }
                                        """
                        )
                        .expectedCode("2001")
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
                                "/api/v1/map/node/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu Map",
                                "DELETE",
                                "/api/v1/map/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                ))
                .build();
    }
}
