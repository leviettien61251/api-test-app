package com.example.apitestapp.services.auth;

import com.example.apitestapp.services.*;

import java.util.List;
import java.util.Map;

public class LoginScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Scenario 1",
                        "Số điện thoại hợp lệ, đã đăng ký",
                        List.of(new ApiSetupRequest(
                                "Thêm dữ liệu mồi Signup",
                                "api/v1/signup",
                                """
                                        {
                                          "phoneNumber": "0981234567",
                                          "password": "111111"
                                        }
                                        """,
                                List.of(),
                                true
                        )),
                        """
                                {
                                  "phoneNumber": "0981234567",
                                  "password": "111111"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Scenario 2",
                        "Số điện thoại đúng, sai mật khẩu",
                        List.of(new ApiSetupRequest(
                                "Thêm dữ liệu mồi Signup",
                                "api/v1/signup",
                                """
                                        {
                                          "phoneNumber": "0981111111",
                                          "password": "111111"
                                        }
                                        """,
                                List.of(),
                                true
                        )),
                        """
                                {
                                  "phoneNumber": "0981111111",
                                  "password": "222222"
                                }
                                """,
                        "3008",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 3",
                        "Số điện thoại hợp lệ, chưa đăng ký",
                        """
                                {
                                  "phoneNumber": "0983333333",
                                  "password": "111111"
                                }
                                """,
                        "3007",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 4",
                        "Thiếu mật khẩu",
                        """
                                {
                                  "phoneNumber": "0981111111",
                                  "password": ""
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 5",
                        "Thiếu số điện thoại",
                        """
                                {
                                  "phoneNumber": "",
                                  "password": "111111"
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 6",
                        "Số điện thoại hợp lệ, đã đăng ký nhưng thiếu mật khẩu",
                        """
                                {
                                  "phoneNumber": "0981234567",
                                  "password": ""
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 7",
                        "Số điện thoại không hợp lệ, mật khẩu không hợp lệ",
                        """
                                {
                                  "phoneNumber": "0981",
                                  "password": "111"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Additional Test",
                        "Số điện thoại chứa khoảng trắng ở đầu/cuối, mật khẩu hợp lệ - đã đăng ký (Nên thành công)",
                        """
                                {
                                  "phoneNumber": " 0981234567 ",
                                  "password": "111111"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Additional Test",
                        "Số điện thoại đã đăng nhập",
                        """
                                {
                                  "phoneNumber": "0981234567",
                                  "password": "111111"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                )
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Auth Module",
                "POST /api/v1/login",
                "/api/v1/login",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(new ApiCleanupRequest(
                                "Clean login test data",
                                "DELETE",
                                "/api/v1/login/clean",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean signup test data",
                                "DELETE",
                                "/api/v1/signup/clean",
                                "",
                                Map.of("Authorization", "Bearer ${token}"),
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                )
        );
    }
}
