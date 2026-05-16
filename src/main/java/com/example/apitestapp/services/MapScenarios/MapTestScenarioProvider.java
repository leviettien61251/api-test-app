package com.example.apitestapp.services.MapScenarios;


import com.example.apitestapp.services.*;

import java.util.List;

public class MapTestScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Yêu cầu hợp lệ với đầy đủ các tham số cần thiết - Nên đạt (Thành công)")
                        .requestBody(
                                """
                                                {
                                                        "buildingCode": "B-A2",
                                                          "buildingName": "Building A2",
                                                          "imageUrl": " https://example.com/BA1.jpg ",
                                                          "scaleX": 5.1,
                                                          "scaleY": 5.1
                                                }
                                        """
                        )
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Thiếu tham số buildingCode")
                        .requestBody(
                                """
                                                {
                                                        "buildingCode": "",
                                                          "buildingName": "Building A2",
                                                          "imageUrl": " https://example.com/BA1.jpg ",
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
                        .description("Thiếu tham số buildingName")
                        .requestBody(
                                """
                                                {
                                                        "buildingCode": "BA-1",
                                                          "buildingName": "",
                                                          "imageUrl": "https://example.com/BA1.jpg",
                                                          "scaleX": 5.1,
                                                          "scaleY": 5.1
                                                }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("Thiếu tham số imageUrl")
                        .requestBody(
                                """
                                                {
                                                        "buildingCode": "BA-1",
                                                          "buildingName": "Building A-1",
                                                          "imageUrl": "",
                                                          "scaleX": 5.1,
                                                          "scaleY": 5.1
                                                }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("Thiếu tham số scaleX")
                        .requestBody(
                                """
                                                {
                                                        "buildingCode": "BA-1",
                                                          "buildingName": "Building A-1",
                                                          "imageUrl": "https://example.com/BA1.jpg",
                                                          "scaleX": ,
                                                          "scaleY": 5.1
                                                }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("Thiếu tham số scaleY")
                        .requestBody(
                                """
                                                {
                                                        "buildingCode": "BA-1",
                                                          "buildingName": "Building A-1",
                                                          "imageUrl": "",
                                                          "scaleX": 5.1,
                                                          "scaleY": 
                                                }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("BuildingCode đã có sẵn (Nên Fail)")
                        .setupRequests(
                                List.of(
                                        new ApiSetupRequest(
                                                "Thêm BuildingCode mồi",
                                                "/api/v1/insert-map-test",
                                                """
                                                        {
                                                          "buildingCode": "BA-3",
                                                          "buildingName": "Building A-3",
                                                          "imageUrl": "https://example.com/BA1.jpg",
                                                          "scaleX": 5.1,
                                                          "scaleY": 5.1
                                                        }
                                                        """,
                                                List.of("1000", "3006"),
                                                true
                                        )
                                )
                        )
                        .requestBody(
                                """
                                                {
                                                        "buildingCode": "BA-3",
                                                          "buildingName": "Building A-3",
                                                          "imageUrl": "https://example.com/BA3.jpg",
                                                          "scaleX": 5.1,
                                                          "scaleY": 5.1
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
                .apiLabel("POST /api/v1/insert-map-test")
                .endpoint("/api/v1/insert-map-test")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
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
