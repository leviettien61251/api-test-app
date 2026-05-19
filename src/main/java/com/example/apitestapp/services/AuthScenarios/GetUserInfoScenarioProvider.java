package com.example.apitestapp.services.AuthScenarios;

import com.example.apitestapp.services.*;

import java.util.List;

public class GetUserInfoScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Testcase 1",
                        "Số điện thoại hợp lệ, user tồn tại",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": "0981111111"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 2",
                        "Số điện thoại hợp lệ, user chưa đăng nhập tồn tại - 3009",
                        List.of(new ApiSetupRequest(
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
                        ),
                        """
                                {
                                  "phoneNumber": "0982222222"
                                }
                                """,
                        "3009",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 3",
                        "Số điện thoại trống",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": ""
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 4",
                        "Số điện thoại không hợp lệ",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": "123"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 5",
                        "Số điện thoại hợp lệ + khoảng trắng, user tồn tại",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": " 0985555555 "
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 6",
                        "Số điện thoại null",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": null
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 7.1",
                        "Lấy dữ liệu user nhiều lần - Lần 1",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": "0987777777"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 7.2",
                        "Lấy dữ liệu user nhiều lần - Lần 2",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": " 0987777777 "
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 8.1",
                        "Số điện thoại không hợp lệ + quá ngắn",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": "123"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 8.2",
                        "Số điện thoại quá dài - chứa chữ",
                        """
                                {
                                  "phoneNumber": "ABC0988888888"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 8.3",
                        "Số điện thoại quá ngắn - regex hợp lệ",
                        """
                                {
                                  "phoneNumber": "09888"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 8.4",
                        "Số điện thoại quá dài",
                        """
                                {
                                  "phoneNumber": "09888888888888"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 9",
                        "Thông tin trả về chứa code và tin nhắn hợp lệ",
                        List.of(new ApiSetupRequest(
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
                                )
                                ,
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
                                )),
                        """
                                {
                                  "phoneNumber": "0981234567"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 10",
                        "User không tồn tại",
                        """
                                {
                                  "phoneNumber": "0901234567"
                                }
                                """,
                        "3009",
                        "FAILURE"
                )
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
                                "/api/v1/get-user-info/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean login test data",
                                "DELETE",
                                "/api/v1/user-test/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean login test data",
                                "DELETE",
                                "/api/v1/login/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean signup test data",
                                "DELETE",
                                "/api/v1/signup/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                )
        );
    }
}
