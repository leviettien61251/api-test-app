package com.example.apitestapp.services.auth;

import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class GetUserInfoScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Testcase 1")
                        .description("Số điện thoại hợp lệ, user tồn tại")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
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
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
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
                                  "phoneNumber": "0981111111"
                                }
                                """)
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 2")
                        .description("Số điện thoại hợp lệ, user chưa đăng nhập tồn tại - 3009")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0982222222",
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
                                  "phoneNumber": "0982222222"
                                }
                                """)
                        .expectedCode("3009")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 3")
                        .description("Số điện thoại trống")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0983333333",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0983333333",
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
                                  "phoneNumber": ""
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 4")
                        .description("Số điện thoại không hợp lệ")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0984444444",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0984444444",
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
                                  "phoneNumber": "123"
                                }
                                """)
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 5")
                        .description("Số điện thoại hợp lệ + khoảng trắng, user tồn tại")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0985555555",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0985555555",
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
                                  "phoneNumber": " 0985555555 "
                                }
                                """)
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 6")
                        .description("Số điện thoại null")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0986666666",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0986666666",
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
                                  "phoneNumber": null
                                }
                                """)
                        .expectedCode("2001")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 7.1")
                        .description("Lấy dữ liệu user nhiều lần - Lần 1")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0987777777",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0987777777",
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
                                  "phoneNumber": "0987777777"
                                }
                                """)
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 7.2")
                        .description("Lấy dữ liệu user nhiều lần - Lần 2")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0987777777",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000", "3006"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0987777777",
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
                                  "phoneNumber": " 0987777777 "
                                }
                                """)
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 8.1")
                        .description("Số điện thoại không hợp lệ + quá ngắn")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0988888888",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0988888888",
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
                                  "phoneNumber": "123"
                                }
                                """)
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 8.2")
                        .description("Số điện thoại quá dài - chứa chữ")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "ABC0988888888"
                                }
                                """)
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 8.3")
                        .description("Số điện thoại quá ngắn - regex hợp lệ")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "09888"
                                }
                                """)
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 8.4")
                        .description("Số điện thoại quá dài")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "09888888888888"
                                }
                                """)
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 9")
                        .description("Thông tin trả về chứa code và tin nhắn hợp lệ")
                        .setupRequests(List.of(
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/signup",
                                        """
                                                {
                                                  "phoneNumber": "0981234567",
                                                  "password": "111111"
                                                }
                                                """,
                                        List.of("1000"),
                                        true
                                ),
                                new ApiSetupRequest(
                                        "Thêm dữ liệu mồi Signup",
                                        "api/v1/login",
                                        """
                                                {
                                                  "phoneNumber": "0981234567",
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
                                  "phoneNumber": "0981234567"
                                }
                                """)
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Testcase 10")
                        .description("User không tồn tại")
                        .headers(Map.of("Authorization", "Bearer ${token}"))
                        .requestBody("""
                                {
                                  "phoneNumber": "0901234567"
                                }
                                """)
                        .expectedCode("3009")
                        .expectedStatus("FAILURE")
                        .build()
        );

        return new ApiScenarioDefinition(
                "Collections",
                "User Module",
                "POST /api/v1/get-user-info",
                "/api/v1/get-user-info",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(new ApiCleanupRequest(
                                "Clean login test data",
                                "DELETE",
                                "/api/v1/clean/get-user-info",
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
                        ), new ApiCleanupRequest(
                                "Clean login test data",
                                "DELETE",
                                "/api/v1/clean/user-test",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                )
        );
    }
}
