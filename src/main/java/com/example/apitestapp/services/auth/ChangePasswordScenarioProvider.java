package com.example.apitestapp.services.auth;

import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class ChangePasswordScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Đủ dữ liệu, mật khẩu cũ đúng")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "/api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0981111111",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Login",
                                        "/api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0981111111",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                )
                        ))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0981111111",
                                  "oldPassword": "111111",
                                  "newPassword": "222222"
                                }
                                """)
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2")
                        .description("Đủ dữ liệu, mật khẩu cũ sai")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "/api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0982222222",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Login",
                                        "/api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0982222222",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of(),
                                        true
                                )
                        ))
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0982222222",
                                  "oldPassword": "222222",
                                  "newPassword": "333333"
                                }
                                """)
                        .expectedCode("3008")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 3")
                        .description("Tài khoản chưa đăng ký (không có trong cả 2 bảng)")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0983333333",
                                  "oldPassword": "111111",
                                  "newPassword": "222222"
                                }
                                """)
                        .expectedCode("3009")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 4")
                        .description("Thiếu nhập mật khẩu cũ")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0984444444",
                                  "oldPassword": "",
                                  "newPassword": "222222"
                                }
                                """)
                        .expectedCode("2006")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 5")
                        .description("Thiếu nhập mật khẩu mới ")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0985555555",
                                  "oldPassword": "111111",
                                  "newPassword": ""
                                }
                                """)
                        .expectedCode("2006")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 6")
                        .description("Mật khẩu mới không hợp lệ (quá ngắn)")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0986666666",
                                  "oldPassword": "111111",
                                  "newPassword": "222"
                                }
                                """)
                        .expectedCode("3008")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7")
                        .description("Mật khẩu mới trùng mật khẩu cũ")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0987777777",
                                  "oldPassword": "111111",
                                  "newPassword": "111111"
                                }
                                """)
                        .expectedCode("3008")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8")
                        .description("Thiếu số điện thoại")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "",
                                  "oldPassword": "111111",
                                  "newPassword": "222222"
                                }
                                """)
                        .expectedCode("2006")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Auth Module",
                "POST /api/v1/change-password",
                "/api/v1/change-password",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu change password",
                                "DELETE",
                                "/api/v1/clean/change-password",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu login",
                                "DELETE",
                                "/api/v1/clean/login",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu signup",
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
                        ))

        );
    }
}
