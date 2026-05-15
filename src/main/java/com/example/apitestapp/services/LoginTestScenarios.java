package com.example.apitestapp.services;

import java.util.ArrayList;
import java.util.List;

public class LoginTestScenarios {

    public static List<LoginTestData> getLoginScenarios() {
        List<LoginTestData> scenarios = new ArrayList<>();

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 1")
                .phoneNumber("0901234567")
                .password("111111")
                .expectedCode("1000")
                .expectedStatus("SUCCESS")
                .description("Valid phone, registered account")
                .build());

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 2")
                .phoneNumber("0901234567")
                .password("wrongpassword")
                .expectedCode("3008")
                .expectedStatus("FAILURE")
                .description("Valid phone but incorrect password")
                .build());

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 3")
                .phoneNumber("0912345678")
                .password("111111")
                .expectedCode("3007")
                .expectedStatus("FAILURE")
                .description("Valid phone but unregistered")
                .build());

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 4")
                .phoneNumber("0901234567")
                .password("")
                .expectedCode("2001")
                .expectedStatus("FAILURE")
                .description("Missing/empty password")
                .build());

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 5")
                .phoneNumber("123")
                .password("111111")
                .expectedCode("2003")
                .expectedStatus("FAILURE")
                .description("Invalid phone format")
                .build());

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 6")
                .phoneNumber("")
                .password("111111")
                .expectedCode("2001")
                .expectedStatus("FAILURE")
                .description("Empty phone")
                .build());

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 7")
                .phoneNumber(" 0901234567 ")
                .password("111111")
                .expectedCode("1000")
                .expectedStatus("SUCCESS")
                .description("Phone with surrounding whitespace (trim expected by backend)")
                .build());

        scenarios.add(LoginTestData.builder()
                .scenario("Scenario 8")
                .phoneNumber("0901234567")
                .password("111111")
                .expectedCode("3006")
                .expectedStatus("FAILURE")
                .description("User already logged in / exists in logged_in_users")
                .build());

        return scenarios;
    }
}
