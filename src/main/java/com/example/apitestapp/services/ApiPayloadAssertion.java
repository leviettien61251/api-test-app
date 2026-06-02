package com.example.apitestapp.services;

public class ApiPayloadAssertion {
    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        LESS_THAN,
        STARTS_WITH,
        CONTAINS,
        EXISTS,
        ARRAY_LENGTH,
        JSON_TYPE
    }

    public enum JsonType {
        STRING,
        NUMBER,
        BOOLEAN,
        ARRAY,
        OBJECT,
        NULL
    }

    private final String jsonPath;
    private final Operator operator;
    private final String expectedValue;

    public ApiPayloadAssertion(String jsonPath, Operator operator, String expectedValue) {
        this.jsonPath = jsonPath;
        this.operator = operator;
        this.expectedValue = expectedValue;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public static ApiPayloadAssertion equalsTo(String jsonPath, String expectedValue) {
        return new ApiPayloadAssertion(jsonPath, Operator.EQUALS, expectedValue);
    }

    public static ApiPayloadAssertion equalsTo(String jsonPath, Number expectedValue) {
        return equalsTo(jsonPath, String.valueOf(expectedValue));
    }

    public static ApiPayloadAssertion greaterThan(String jsonPath, Number expectedValue) {
        return new ApiPayloadAssertion(jsonPath, Operator.GREATER_THAN, String.valueOf(expectedValue));
    }

    public static ApiPayloadAssertion startsWith(String jsonPath, String expectedPrefix) {
        return new ApiPayloadAssertion(jsonPath, Operator.STARTS_WITH, expectedPrefix);
    }

    public static ApiPayloadAssertion isType(String jsonPath, JsonType expectedType) {
        return new ApiPayloadAssertion(jsonPath, Operator.JSON_TYPE, expectedType.name());
    }
}
