package com.example.apitestapp.services.map;

import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.ArrayList;
import java.util.List;

public class BulkTest1ScenarioProvider implements ApiScenarioProvider {

    private static final int SIGNUP_SCENARIO_COUNT = 10_000;
    private static final String PHONE_NUMBER_PREFIX = "0980";
    private static final String SIGNUP_PASSWORD = "111111";

    private static List<ApiTestScenario> createSignupScenarios() {
        List<ApiTestScenario> scenarios = new ArrayList<>(SIGNUP_SCENARIO_COUNT);

        for (int index = 0; index < SIGNUP_SCENARIO_COUNT; index++) {
            String phoneNumber = PHONE_NUMBER_PREFIX + String.format("%06d", index);
            scenarios.add(new ApiTestScenario(
                    "Bulk Signup " + (index + 1),
                    "Sign up phone number " + phoneNumber,
                    createSignupBody(phoneNumber),
                    "1000",
                    "SUCCESS"
            ));
        }

        return scenarios;
    }

    private static String createSignupBody(String phoneNumber) {
        return """
                {
                  "phoneNumber": "%s",
                  "password": "%s"
                }
                """.formatted(phoneNumber, SIGNUP_PASSWORD);
    }


    @Override
    public ApiScenarioDefinition getDefinition() {

        List<ApiTestScenario> scenarios = createSignupScenarios();
        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Bulk Test Module")
                .apiLabel("POST bulk /api/v1/signup")
                .endpoint("/api/v1/signup")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
                        new ApiCleanupRequest(
                                "Clean signup test data",
                                "DELETE",
                                "/api/v1/signup/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                ))
                .build();
    }


}
