package com.example.apitestapp.services.map;

import com.example.apitestapp.models.dto.*;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;
import java.util.Map;

public class GetMetaScenarioProvider implements ApiScenarioProvider {
    private static List<ApiSetupRequest> createMetaSetupRequests(String buildingCode,
                                                                 String buildingName,
                                                                 String imageUrl,
                                                                 String scaleX,
                                                                 String scaleY) {
        return List.of(
                new ApiSetupRequest(
                        "Thêm dữ liệu mẫu Map",
                        "POST",
                        "/api/v1/insert-map-test",
                        """
                                {
                                    "buildingCode": "%s",
                                    "buildingName": "%s",
                                    "imageUrl": "%s",
                                    "scaleX": %s,
                                    "scaleY": %s
                                }
                                """.formatted(buildingCode, buildingName, imageUrl, scaleX, scaleY),
                        Map.of("Authorization", "Bearer ${token}"),
                        List.of("1000", "200", "201"),
                        true,
                        List.of(new ApiResponseVariable("floor_id", "data.0.id"))
                )
        );
    }

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("TC-1")
                        .description("Lấy meta thành công với floor_id hợp lệ và dữ liệu building_code, scale trả về đúng")
                        .setupRequests(createMetaSetupRequests("B_META", "Building Meta", "https://example.com/meta-test.jpg", "1.5", "2.0"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "${floor_id}"))
                        .payloadAssertions(List.of(
                                ApiPayloadAssertion.equalsTo("data.building_code", "B_META"),
                                ApiPayloadAssertion.equalsTo("data.scale_x", 1.5),
                                ApiPayloadAssertion.equalsTo("data.scale_y", 2.0)
                        ))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-2")
                        .description("scale_x và scale_y là số dương")
                        .setupRequests(createMetaSetupRequests("B_META_SCALE", "Building Meta Scale", "https://example.com/meta-scale.jpg", "1.5", "2.0"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "${floor_id}"))
                        .payloadAssertions(List.of(
                                ApiPayloadAssertion.greaterThan("data.scale_x", 0),
                                ApiPayloadAssertion.greaterThan("data.scale_y", 0)
                        ))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-3")
                        .description("Tầng có image_url hợp lệ bắt đầu bằng http")
                        .setupRequests(createMetaSetupRequests("B_META_IMAGE", "Building Meta Image", "https://example.com/meta-image.jpg", "1.5", "2.0"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "${floor_id}"))
                        .payloadAssertions(List.of(
                                ApiPayloadAssertion.isType("data.image_url", ApiPayloadAssertion.JsonType.STRING),
                                ApiPayloadAssertion.startsWith("data.image_url", "http")
                        ))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-4")
                        .description("floor_id là số nguyên dương hợp lệ")
                        .setupRequests(createMetaSetupRequests("B_META_FLOOR", "Building Meta Floor", "https://example.com/meta-floor.jpg", "1.5", "2.0"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "${floor_id}"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-5")
                        .description("building_name trả về đúng kiểu chuỗi ký tự")
                        .setupRequests(createMetaSetupRequests("B_META_NAME", "Building Meta Name", "https://example.com/meta-name.jpg", "1.5", "2.0"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "${floor_id}"))
                        .payloadAssertions(List.of(
                                ApiPayloadAssertion.isType("data.building_name", ApiPayloadAssertion.JsonType.STRING)
                        ))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-6")
                        .description("Thiếu floor_id - Mã 2001")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-7")
                        .description("floor_id không tồn tại - Mã 4001")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "99999"))
                        .expectedCode("4001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-8")
                        .description("floor_id không phải là số - Mã 2002")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "abc"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-9")
                        .description("floor_id là số âm - Mã 2002")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "-1"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-10")
                        .description("floor_id là 0 - Mã 4001")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "0"))
                        .expectedCode("4001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-11")
                        .description("floor_id vượt quá giới hạn integer - Mã 2002")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "999999999999"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-12")
                        .description("floor_id là mảng - Mã 2001")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id[]", "1"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-13")
                        .description("floor_id chứa ký tự đặc biệt (SQL Injection) - Mã 2002")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", "1;DROP TABLE maps"))
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("TC-14")
                        .description("floor_id là chuỗi rỗng - Mã 2001")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .queryParams(Map.of("floor_id", ""))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Map Module",
                "GET /api/v1/map/meta",
                "/api/v1/map/meta",
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
