package com.example.apitestapp.services;

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
}
