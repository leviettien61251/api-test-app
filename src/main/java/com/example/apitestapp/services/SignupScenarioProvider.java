package com.example.apitestapp.services;

import java.util.List;

public class SignupScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Scenario 1",
                        "Valid phone, not yet registered",
                        """
                                {
                                  "phoneNumber": "0982222222",
                                  "password": "111111"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Scenario 2",
                        "Valid phone, already registered",
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
                "Auth Module",
                "POST /api/v1/signup",
                "/api/v1/signup",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(new ApiCleanupRequest(
                        "Clean signup test data",
                        "DELETE",
                        "/api/v1/signup/clean",
                        "",
                        List.of("1000", "200", "204", "201"),
                        true
                ))
        );
    }
}
