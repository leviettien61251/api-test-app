package com.example.apitestapp.services.user;

import com.example.apitestapp.models.dto.ApiCleanupRequest;
import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;
import java.util.Map;

public class SetUserInfoScenarioProvider implements ApiScenarioProvider {

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

    private static String createSetUserInfoRequestBody(String fullName, String phoneNumber, String address) {
        return """
                {
                  "fullName": %s,
                  "phoneNumber": %s,
                  "address": %s
                }
                """.formatted(toJsonString(fullName), toJsonString(phoneNumber), toJsonString(address));
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
                        .description("Cập nhật thông tin user thành công")
                        .setupRequests(createAuthSetupRequests("0981111111"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetUserInfoRequestBody("Steve Jobs", "0981111111", "Ha Noi, Viet Nam"))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Thiếu fullName")
                        .setupRequests(createAuthSetupRequests("0981111112"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                          "phoneNumber": "0981111112",
                                          "address": "Ha Noi, Viet Nam"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("fullName rỗng")
                        .setupRequests(createAuthSetupRequests("0981111113"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetUserInfoRequestBody("", "0981111113", "Ha Noi, Viet Nam"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("Thiếu phoneNumber")
                        .setupRequests(createAuthSetupRequests("0981111114"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                          "fullName": "Steve Jobs",
                                          "address": "Ha Noi, Viet Nam"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("phoneNumber null")
                        .setupRequests(createAuthSetupRequests("0981111115"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetUserInfoRequestBody("Steve Jobs", null, "Ha Noi, Viet Nam"))
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("phoneNumber không hợp lệ")
                        .setupRequests(createAuthSetupRequests("0981111116"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetUserInfoRequestBody("Steve Jobs", "123", "Ha Noi, Viet Nam"))
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("Thiếu address")
                        .setupRequests(createAuthSetupRequests("0981111117"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(
                                """
                                        {
                                          "fullName": "Steve Jobs",
                                          "phoneNumber": "0981111117"
                                        }
                                        """
                        )
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("address để trống")
                        .setupRequests(createAuthSetupRequests("0981111118"))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetUserInfoRequestBody("Steve Jobs", "0981111118", ""))
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 9")
                        .description("User đã đăng ký nhưng chưa login")
                        .setupRequests(List.of(createSignupSetupRequest("0981111119")))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetUserInfoRequestBody("Steve Jobs", "0981111119", "Ha Noi, Viet Nam"))
                        .expectedCode("3009")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 10")
                        .description("User không tồn tại")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody(createSetUserInfoRequestBody("Steve Jobs", "0981111120", "Ha Noi, Viet Nam"))
                        .expectedCode("3007")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("User Module")
                .apiLabel("POST /api/v1/set-user-info")
                .endpoint("/api/v1/set-user-info")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
                        new ApiCleanupRequest(
                                "Clean set-user-info test data",
                                "DELETE",
                                "/api/v1/clean/set-user-info",
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
