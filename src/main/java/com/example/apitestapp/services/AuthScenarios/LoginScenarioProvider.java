package com.example.apitestapp.services.AuthScenarios;

import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.List;

public class LoginScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Scenario 1",
                        "Valid phone, registered account",
                        """
                                {
                                  "phoneNumber": "0901234567",
                                  "password": "111111"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Scenario 2",
                        "Valid phone but incorrect password",
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
                        "Valid phone but unregistered",
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
                        "Missing/empty password",
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
                        "Empty phone",
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
                        "Phone with surrounding whitespace (trim expected by backend)",
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
                        "User already logged in / exists in logged_in_users",
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
                "POST /api/v1/login",
                "/api/v1/login",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(new ApiCleanupRequest(
                        "Clean login test data",
                        "DELETE",
                        "/api/v1/login/clean",
                        "",
                        List.of("1000", "200", "204", "201"),
                        true
                ))
        );
    }
}
