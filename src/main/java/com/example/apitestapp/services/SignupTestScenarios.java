package com.example.apitestapp.services;

import java.util.ArrayList;
import java.util.List;

public class SignupTestScenarios {

    public static List<SignupTestData> getSignupScenarios() {
        List<SignupTestData> scenarios = new ArrayList<>();

        // Scenario 1: Valid data, not yet registered → SUCCESS
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 1")
                .phone("84901234567")
                .password("Password@123")
                .expectedCode("1000")
                .expectedStatus("SUCCESS")
                .description("Valid phone, not yet registered")
                .build());

        // Scenario 2: Valid data, already registered → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 2")
                .phone("84901234567")
                .password("Password@123")
                .expectedCode("2001")
                .expectedStatus("FAILURE")
                .description("Valid phone, already registered")
                .build());

        // Scenario 3: Valid phone, no password → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 3")
                .phone("84901234567")
                .password("")
                .expectedCode("3006")
                .expectedStatus("FAILURE")
                .description("Valid phone, missing password")
                .build());

        // Scenario 4: Invalid phone, has password → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 4")
                .phone("123")
                .password("Password@123")
                .expectedCode("3007")
                .expectedStatus("FAILURE")
                .description("Invalid phone format")
                .build());

        // Scenario 5: Invalid phone, has password, already registered → FAILURE
        scenarios.add(SignupTestData.builder()
                .scenario("Scenario 5")
                .phone("invalid")
                .password("Password@123")
                .expectedCode("3007")
                .expectedStatus("FAILURE")
                .description("Invalid phone, already registered")
                .build());

        // Additional Test: Invalid password format (too short)
        scenarios.add(SignupTestData.builder()
                .scenario("Additional Test")
                .phone("84901234567")
                .password("123")
                .expectedCode("3008")
                .expectedStatus("FAILURE")
                .description("Invalid password format (too short)")
                .build());

        // Additional Test: Valid password with special characters
        scenarios.add(SignupTestData.builder()
                .scenario("Additional Test")
                .phone("84909876543")
                .password("P@ssw0rd!#$%")
                .expectedCode("1000")
                .expectedStatus("SUCCESS")
                .description("Valid password with special characters")
                .build());

        return scenarios;
    }
}
