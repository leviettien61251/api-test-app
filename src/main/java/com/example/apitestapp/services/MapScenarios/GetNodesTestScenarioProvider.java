package com.example.apitestapp.services.MapScenarios;

import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class GetNodesTestScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Lấy danh sách node thành công với floor_id hợp lệ")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mẫu Map",
                                        "POST",
                                        "/api/v1/insert-map-test",
                                        """
                                                {
                                                    "buildingCode": " B-A1 ",
                                                    "buildingName": " Building A1 ",
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
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
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
                                                    "xCoordinate": 1,
                                                    "yCoordinate": 3,
                                                    "type": "abc",
                                                    "isPassable": true
                                                }
                                                """,
                                        List.of("1000"),
                                        true,
                                        List.of(new ApiResponseVariable("endNodeId", "data.id"))
                                )

                        ))
                        .queryParams(Map.of("floor_id", "${mapId}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Thiếu floor_id - Mã 2001")
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("floor_id truyền vào là mảng - Mã 2001")
                        .queryParams(Map.of("floor_id[]", "1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("floor_id không phải là số - Mã 2002")
                        .queryParams(Map.of("floor_id", "abc"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("floor_id là số âm - Mã 2002")
                        .queryParams(Map.of("floor_id", "-5"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("floor_id là số 0 - Mã 2003")
                        .queryParams(Map.of("floor_id", "0"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("floor_id không tồn tại trong DB - Mã 4001")
                        .queryParams(Map.of("floor_id", "999999"))
                        .expectedCode("4001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("floor_id vượt quá giới hạn integer - Mã 2003")
                        .queryParams(Map.of("floor_id", "999999999999"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Map Module")
                .apiLabel("GET /api/v1/map/nodes")
                .endpoint("/api/v1/map/nodes")
                .sampleRequestBody(null)
                .scenarios(scenarios)
                .cleanupRequests(List.of(
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
                        )
                ))
                .build();
    }
}
