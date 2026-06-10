package com.example.apitestapp.services.auth;

import com.example.apitestapp.models.dto.ApiCleanupRequest;
import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;
import java.util.Map;

public class SignupScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1")
                        .description("Số điện thoại hợp lệ, chưa đăng ký")
//                        .setupRequests(List.of(
//                                new ApiSetupRequest(
//                                        "Ensure user is already registered",
//                                        "/api/v1/signup",
//                                        """
//                                                {
//                                                  "phoneNumber": "0988888888",
//                                                  "password": "111111"
//                                                }
//                                                """,
//                                        List.of("1000", "3006"),
//                                        true
//                                ),
//                                ApiSetupRequest.builder()
//                                        .name("Ensure user is already login")
//                                        .method("POST")
//                                        .endpoint("/api/v1/login")
//                                        .requestBody("""
//                                                {
//                                                  "phoneNumber": "0988888888",
//                                                  "password": "111111"
//                                                }
//                                                """)
//                                        .responseVariables(List.of(new ApiResponseVariable("token", "token")))
//                                        .expectedCodes(List.of("1000", "3006"))
//                                        .required(true)
//                                        .build()
//                        ))
                        .requestBody("""
                                {
                                  "phoneNumber": "0982222222",
                                  "password": "111111"
                                }
                                """)
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build()
                ,

                new ApiTestScenario(
                        "Scenario 2",
                        "Số điện thoại hợp lệ, đã đăng ký",
                        List.of(new ApiSetupRequest(
                                "Ensure user is already registered",
                                "/api/v1/signup",
                                """
                                        {
                                          "phoneNumber": "0981111111",
                                          "password": "111111"
                                        }
                                        """,
                                List.of("1000", "3006"),
                                true
                        )),
                        """
                                {
                                  "phoneNumber": "0981111111",
                                  "password": "111111"
                                }
                                """,
                        "3006",
                        "FAILURE"
                ),

                new ApiTestScenario(
                        "Scenario 3",
                        "Số điện thoại hợp lệ, thiếu password",
                        """
                                {
                                  "phoneNumber": "0981111112",
                                  "password": ""
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 4",
                        "Số điện thoại không hợp lệ",
                        """
                                {
                                  "phoneNumber": "123",
                                  "password": "111111"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 5",
                        "Số điện thoại không hợp lệ, nhưng đã đăng ký(ko phải đầu số viettel)",
                        """
                                {
                                  "phoneNumber": "0123456789",
                                  "password": "111111"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Additional Test",
                        "Mật khẩu quá ngắn",
                        """
                                {
                                  "phoneNumber": "0981111111",
                                  "password": "123"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Additional Test",
                        "Mật khẩu hợp lệ + chứa ký tự đặc biệt",
                        """
                                {
                                  "phoneNumber": "0985111111",
                                  "password": "P@ssw0rd!#$%"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                )
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Auth Module",
                "POST /api/v1/signup",
                "/api/v1/signup",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(
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
                                "Clean signup test data",
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
