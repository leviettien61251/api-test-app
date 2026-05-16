package com.example.apitestapp.services.AuthScenarios;

import com.example.apitestapp.services.*;

import java.util.List;

public class ChangePasswordScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Scenario 1",
                        "Đủ dữ liệu, mật khẩu cũ đúng",
                        List.of(
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
                        ),
                        """
                                {
                                  "phoneNumber": "0981111111",
                                  "oldPassword": "111111",
                                  "newPassword": "222222"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Scenario 2",
                        "Đủ dữ liệu, mật khẩu cũ sai",
                        List.of(
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
                        ),
                        """
                                {
                                  "phoneNumber": "0982222222",
                                  "oldPassword": "222222",
                                  "newPassword": "333333"
                                }
                                """,
                        "3008",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 3",
                        "Tài khoản chưa đăng ký (không có trong cả 2 bảng)",
                        """
                                {
                                  "phoneNumber": "0983333333",
                                  "oldPassword": "111111",
                                  "newPassword": "222222"
                                }
                                """,
                        "3009",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 4",
                        "Thiếu nhập mật khẩu cũ",
                        """
                                {
                                  "phoneNumber": "0984444444",
                                  "oldPassword": "",
                                  "newPassword": "222222"
                                }
                                """,
                        "2006",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 5",
                        "Thiếu nhập mật khẩu mới ",
                        """
                                {
                                  "phoneNumber": "0985555555",
                                  "oldPassword": "111111",
                                  "newPassword": ""
                                }
                                """,
                        "2006",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 6",
                        "Mật khẩu mới không hợp lệ (quá ngắn)",
                        """
                                {
                                  "phoneNumber": "0986666666",
                                  "oldPassword": "111111",
                                  "newPassword": "222"
                                }
                                """,
                        "3008",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 7",
                        "Mật khẩu mới trùng mật khẩu cũ",
                        """
                                {
                                  "phoneNumber": "0987777777",
                                  "oldPassword": "111111",
                                  "newPassword": "111111"
                                }
                                """,
                        "3008",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 8",
                        "Thiếu số điện thoại",
                        """
                                {
                                  "phoneNumber": "",
                                  "oldPassword": "111111",
                                  "newPassword": "222222"
                                }
                                """,
                        "2006",
                        "FAILURE"
                )
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Node Module",
                "POST /api/v1/change-password",
                "/api/v1/change-password",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu change password",
                                "DELETE",
                                "/api/v1/change-password/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu login",
                                "DELETE",
                                "/api/v1/login/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Dọn dẹp dữ liệu signup",
                                "DELETE",
                                "/api/v1/signup/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ))
        );
    }
}
