package com.example.apitestapp.services.AuthScenarios;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.List;

public class GetUserInfoScenarioProvider implements ApiScenarioProvider {
    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Testcase 1",
                        "Valid phone, existing user",
                        """
                                {
                                  "phoneNumber": "0901234567"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 2",
                        "Valid phone, non-existent user",
                        """
                                {
                                  "phoneNumber": "0901234567"
                                }
                                """,
                        "3007",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 3",
                        "Empty phone number",
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
                        "Invalid phone format",
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
                        "Phone with whitespace, existing user",
                        """
                                {
                                  "phoneNumber": " 0901234567 "
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 6",
                        "Null phone number",
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
                        "Multiple GetUserInfo queries - first user",
                        """
                                {
                                  "phoneNumber": "0901234567"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 7.2",
                        "Multiple GetUserInfo queries - second user",
                        """
                                {
                                  "phoneNumber": "0912345678"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 8.1",
                        "Invalid phone format - too short",
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
                        "Invalid phone format - contains letters",
                        """
                                {
                                  "phoneNumber": "ABC1234567"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 8.3",
                        "Invalid phone format - too short valid prefix",
                        """
                                {
                                  "phoneNumber": "09012345"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 8.4",
                        "Invalid phone format - too long",
                        """
                                {
                                  "phoneNumber": "09012345678901"
                                }
                                """,
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Testcase 9",
                        "User info contains correct code and message validation",
                        """
                                {
                                  "phoneNumber": "0901234567"
                                }
                                """,
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Testcase 10",
                        "Error handling - USER_NOT_FOUND",
                        """
                                {
                                  "phoneNumber": "0901234567"
                                }
                                """,
                        "3007",
                        "FAILURE"
                )
        );

        return new ApiScenarioDefinition(
                "Collections",
                "Auth Module",
                "POST /api/v1/get-user-info",
                "/api/v1/get-user-info",
                scenarios.get(0).getRequestBody(),
                scenarios
        );
    }
}
