package com.example.apitestapp.services;

import java.util.List;

public class ChangePasswordScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Scenario 1",
                        "Số điện thoại hợp lệ, nhưng chưa đăng ký",
                        """
                                {
                                  "phoneNumber": "0983111111",
                                  "password": "111111"
                                }
                                """,
                        "",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Scenario 2",
                        "Valid phone, already registered",
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
                        "Valid phone, missing password",
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
                        "Invalid phone format",
                        """
                                {
                                  "phoneNumber": "123",
                                  "password": "111111"
                                }
                                """,
                        "3007",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 5",
                        "Invalid phone, already registered",
                        """
                                {
                                  "phoneNumber": "invalid",
                                  "password": "111111"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Additional Test",
                        "Invalid password format (too short)",
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
                        "Valid password with special characters",
                        """
                                {
                                  "phoneNumber": "0981111111",
                                  "password": "P@ssw0rd!#$%"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                )
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Map Module",
                "POST /api/v1/login",
                "/api/v1/login",
                scenarios.get(0).getRequestBody(),
                scenarios
        );
    }
}
