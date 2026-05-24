package com.example.apitestapp.services.map;

import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class GetFloorScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Lấy toàn bộ danh sách tầng thành công")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Tạo dữ liệu mồi MapTest",
                                        "POST",
                                        "/api/v1/generateMapDataBuildingAB_5",
                                        """
                                                {}
                                                """,
                                        Map.of("Authorization", "Bearer ${token}"),
                                        List.of("1000", "200", "201"),
                                        true
                                )
                        ))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Lấy toàn bộ danh sách tầng theo mã Building A thành công")
                        .queryParams(Map.of("building_code", "A"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("Lấy toàn bộ danh sách tầng theo mã Building B thành công")
                        .queryParams(Map.of("building_code", "B"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("Lấy toàn bộ danh sách tầng khi mã Building trống")
                        .queryParams(Map.of("building_code", ""))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("Lấy toàn bộ danh sách tầng khi thiếu mã Building")
                        .queryParams(Map.of("building_code", ""))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build()
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Map Module",
                "GET /api/v1/map/floors -",
                "/api/v1/map/floors",
                null,
                scenarios,
                List.of(new ApiCleanupRequest(
                                "Clean signup test data",
                                "DELETE",
                                "/api/v1/clean/map",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
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
                        ))
        );
    }
}
