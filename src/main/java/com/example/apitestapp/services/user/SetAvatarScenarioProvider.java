package com.example.apitestapp.services.user;

import com.example.apitestapp.models.dto.ApiCleanupRequest;
import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;
import java.util.Map;

public class SetAvatarScenarioProvider implements ApiScenarioProvider {

    private static List<ApiSetupRequest> createAuthSetupRequests(String phoneNumber) {
        return List.of(
                createSignupSetupRequest(phoneNumber),
                new ApiSetupRequest(
                        "Thêm dữ liệu mồi Login",
                        "POST",
                        "api/v1/login",
                        createAuthRequestBody(phoneNumber),
                        Map.of("Authorization", "Bearer ${token}"),
                        List.of("1000"),
                        true
                )
        );
    }

    private static ApiSetupRequest createSignupSetupRequest(String phoneNumber) {
        return new ApiSetupRequest(
                "Thêm dữ liệu mồi Signup",
                "POST",
                "api/v1/signup",
                createAuthRequestBody(phoneNumber),
                Map.of("Authorization", "Bearer ${token}"),
                List.of("1000"),
                true
        );
    }

    private static String createAuthRequestBody(String phoneNumber) {
        return """
                {
                  "phoneNumber": "%s",
                  "password": "111111"
                }
                """.formatted(phoneNumber);
    }

    private static String createSetAvatarRequestBody(String avatarUrl, String phoneNumber) {
        return """
                {
                  "avatarUrl": %s,
                  "phoneNumber": %s
                }
                """.formatted(toJsonString(avatarUrl), toJsonString(phoneNumber));
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
                        .description("Cập nhật avatar thành công khi user đã signup và login")
                        .setupRequests(createAuthSetupRequests("0980000001"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("https://example.com/avatar1.jpg", "0980000001"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Thiếu avatarUrl")
                        .setupRequests(createAuthSetupRequests("0980000002"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0980000002"
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("avatarUrl null")
                        .setupRequests(createAuthSetupRequests("0980000003"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody(null, "0980000003"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("avatarUrl để trống")
                        .setupRequests(createAuthSetupRequests("0980000004"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("", "0980000004"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("avatarUrl không hợp lệ")
                        .setupRequests(createAuthSetupRequests("0980000005"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("not-a-url", "0980000005"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("Thiếu phoneNumber")
                        .setupRequests(createAuthSetupRequests("0980000006"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "avatarUrl": "https://example.com/avatar6.jpg"
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("phoneNumber null")
                        .setupRequests(createAuthSetupRequests("0980000007"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("https://example.com/avatar7.jpg", null))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("phoneNumber để trống")
                        .setupRequests(createAuthSetupRequests("0980000008"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("https://example.com/avatar8.jpg", ""))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 9")
                        .description("phoneNumber không hợp lệ")
                        .setupRequests(createAuthSetupRequests("0980000009"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("https://example.com/avatar9.jpg", "123"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 10")
                        .description("User đã signup nhưng chưa login")
                        .setupRequests(List.of(createSignupSetupRequest("0980000010")))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("https://example.com/avatar10.jpg", "0980000010"))
                        .expectedCode("3009")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 11")
                        .description("User không tồn tại")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetAvatarRequestBody("https://example.com/avatar11.jpg", "0980000011"))
                        .expectedCode("3007")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("User Module")
                .apiLabel("POST /api/v1/set-avatar")
                .endpoint("/api/v1/set-avatar")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
                        new ApiCleanupRequest(
                                "Clean set-avatar test data",
                                "DELETE",
                                "/api/v1/clean/set-avatar",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                false
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
                        ), new ApiCleanupRequest(
                                "Clean user-test test data",
                                "DELETE",
                                "/api/v1/clean/user-test",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                false
                        )
                ))
                .build();
    }
}
