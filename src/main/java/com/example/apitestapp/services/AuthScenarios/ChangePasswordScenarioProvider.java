package com.example.apitestapp.services.AuthScenarios;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.List;

public class ChangePasswordScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Scenario 1",
                        "Đủ dữ liệu, mật khẩu cũ đúng",
                        """
                                {
                                  "phoneNumber": "0982345678",
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
                        """
                                {
                                  "phoneNumber": "0982345679",
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
                                  "phoneNumber": "0999999999",
                                  "oldPassword": "any",
                                  "newPassword": "anyNew"
                                }
                                """,
                        "3009",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 4",
                        "Không nhập mật khẩu cũ (null)",
                        """
                                {
                                  "phoneNumber": "0982111222",
                                  "oldPassword": null,
                                  "newPassword": "NewPass123"
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 5",
                        "Không nhập mật khẩu mới (null)",
                        """
                                {
                                  "phoneNumber": "0982111333",
                                  "oldPassword": "111111",
                                  "newPassword": null
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 6",
                        "Mật khẩu mới không hợp lệ (quá ngắn)",
                        """
                                {
                                  "phoneNumber": "0982111444",
                                  "oldPassword": "111111",
                                  "newPassword": "a"
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
                                  "phoneNumber": "0982111555",
                                  "oldPassword": "111111",
                                  "newPassword": "111111"
                                }
                                """,
                        "3008",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 8",
                        "Không có phone",
                        """
                                {
                                  "phoneNumber": null,
                                  "oldPassword": "any",
                                  "newPassword": "any"
                                }
                                """,
                        "3008",
                        "FAILURE"
                )
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Auth Module",
                "POST /api/v1/change-password",
                "/api/v1/change-password",
                scenarios.get(0).getRequestBody(),
                scenarios
        );
    }
}
