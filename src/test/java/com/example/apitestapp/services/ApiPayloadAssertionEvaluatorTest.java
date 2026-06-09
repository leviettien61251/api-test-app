package com.example.apitestapp.services;

import com.example.apitestapp.models.dto.ApiPayloadAssertion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiPayloadAssertionEvaluatorTest {
    private final ApiPayloadAssertionEvaluator evaluator = new ApiPayloadAssertionEvaluator();

    @Test
    void passesReusablePayloadAssertionsForJsonPaths() {
        ApiPayloadAssertionEvaluator.Evaluation evaluation = evaluator.evaluate(
                """
                        {
                          "data": {
                            "building_code": "B_META",
                            "scale_x": 1.5,
                            "image_url": "https://example.com/meta.jpg",
                            "building_name": "Building Meta"
                          }
                        }
                        """,
                List.of(
                        ApiPayloadAssertion.equalsTo("data.building_code", "B_META"),
                        ApiPayloadAssertion.greaterThan("data.scale_x", 0),
                        ApiPayloadAssertion.startsWith("data.image_url", "http"),
                        ApiPayloadAssertion.isType("data.building_name", ApiPayloadAssertion.JsonType.STRING)
                )
        );

        assertTrue(evaluation.passed());
    }

    @Test
    void failsWhenPayloadPathIsMissing() {
        ApiPayloadAssertionEvaluator.Evaluation evaluation = evaluator.evaluate(
                """
                        {
                          "data": {}
                        }
                        """,
                List.of(ApiPayloadAssertion.equalsTo("data.building_code", "B_META"))
        );

        assertFalse(evaluation.passed());
    }

    @Test
    void supportsExtendedPayloadOperators() {
        ApiPayloadAssertionEvaluator.Evaluation evaluation = evaluator.evaluate(
                """
                        {
                          "data": {
                            "name": "Building Alpha",
                            "count": 2,
                            "items": ["indoor", "priority"]
                          }
                        }
                        """,
                List.of(
                        new ApiPayloadAssertion("data.name", ApiPayloadAssertion.Operator.NOT_EQUALS, "Building Beta"),
                        new ApiPayloadAssertion("data.count", ApiPayloadAssertion.Operator.LESS_THAN, "3"),
                        new ApiPayloadAssertion("data.name", ApiPayloadAssertion.Operator.CONTAINS, "Alpha"),
                        new ApiPayloadAssertion("data.items", ApiPayloadAssertion.Operator.CONTAINS, "priority"),
                        new ApiPayloadAssertion("data.items", ApiPayloadAssertion.Operator.ARRAY_LENGTH, "2"),
                        new ApiPayloadAssertion("data.missing", ApiPayloadAssertion.Operator.EXISTS, "false")
                )
        );

        assertTrue(evaluation.passed());
    }

    @Test
    void comparesFullResponseJsonWithoutDependingOnObjectKeyOrder() {
        ApiPayloadAssertionEvaluator.Evaluation evaluation = evaluator.evaluate(
                """
                        { "code": 1000, "data": { "id": 12, "name": "Map" } }
                        """,
                List.of(),
                """
                        { "data": { "name": "Map", "id": 12 }, "code": 1000 }
                        """
        );

        assertTrue(evaluation.passed());
    }
}
