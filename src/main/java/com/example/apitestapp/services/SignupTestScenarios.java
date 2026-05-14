package com.example.apitestapp.services;

import java.util.ArrayList;
import java.util.List;

public class SignupTestScenarios {

    public static List<SignupTestData> getSignupScenarios() {
        List<SignupTestData> scenarios = new ArrayList<>();

        // Scenario 1: Valid data, not yet registered → SUCCESS
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 1")
                .phoneNumber("0981111111")
                .password("111111")
                .expectedCode("1000")
                .expectedStatus("SUCCESS")
                .description("Valid phone, not yet registered")
                .build());

        // Scenario 2: Valid data, already registered → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 2")
                .phoneNumber("0981111111")
                .password("111111")
                .expectedCode("3006")
                .expectedStatus("FAILURE")
                .description("Valid phone, already registered")
                .build());

        // Scenario 3: Valid phone, no password → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 3")
                .phoneNumber("0981111112")
                .password("")
                .expectedCode("2001")
                .expectedStatus("FAILURE")
                .description("Valid phone, missing password")
                .build());

        // Scenario 4: Invalid phone, has password → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 4")
                .phoneNumber("123")
                .password("111111")
                .expectedCode("3007")
                .expectedStatus("FAILURE")
                .description("Invalid phone format")
                .build());

        // Scenario 5: Invalid phone, has password, already registered → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 5")
                .phoneNumber("invalid")
                .password("111111")
                .expectedCode("2003")
                .expectedStatus("FAILURE")
                .description("Invalid phone, already registered")
                .build());

        // Additional Test: Invalid password format (too short)
        scenarios.add(SignupTestData.builder()
                .scenario("Additional Test")
                .phoneNumber("0981111111")
                .password("123")
                .expectedCode("2003")
                .expectedStatus("FAILURE")
                .description("Invalid password format (too short)")
                .build());

        // Additional Test: Valid password with special characters
        scenarios.add(SignupTestData.builder()
                .scenario("Additional Test")
                .phoneNumber("0981111111")
                .password("P@ssw0rd!#$%")
                .expectedCode("1000")
                .expectedStatus("SUCCESS")
                .description("Valid password with special characters")
                .build());

        return scenarios;
    }
}
