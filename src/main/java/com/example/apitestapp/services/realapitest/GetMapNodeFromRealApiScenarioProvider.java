package com.example.apitestapp.services.realapitest;

import com.example.apitestapp.models.dto.ApiPayloadAssertion;
import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.models.dto.ApiTestScenario;
import com.example.apitestapp.services.ApiScenarioProvider;

import java.util.List;
import java.util.Map;

public class GetMapNodeFromRealApiScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                ApiTestScenario.builder()
                        .scenario("Scenario 1: yêu cầu hợp lệ (1000)")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "1"))
                        .requestBody("")
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 2: floor_id quá dài")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "99999999999999999"))
                        .requestBody("")
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 3: floor_id là kiểu chữ")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "abcderf"))
                        .requestBody("")
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build()
                , ApiTestScenario.builder()
                        .scenario("Scenario 4: floor_id là số thực")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "1.5"))
                        .requestBody("")
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 5: floor_id là số âm")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "-1"))
                        .requestBody("")
                        .expectedCode("2002")
                        .expectedStatus("FAILURE")
                        .build()
                ,
                ApiTestScenario.builder()
                        .scenario("Scenario 6: floor_id là số 0")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "0"))
                        .requestBody("")
                        .expectedCode("2003")
                        .expectedStatus("FAILURE")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 7: grid_row là kiểu số")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "1"))
                        .requestBody("")
                        .payloadAssertions(
                                List.of(
                                        ApiPayloadAssertion.isType("data.0.grid_row", ApiPayloadAssertion.JsonType.NUMBER)
                                )
                        )
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 8: grid_col là kiểu số")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "1"))
                        .requestBody("")
                        .payloadAssertions(
                                List.of(
                                        ApiPayloadAssertion.isType("data.0.grid_col", ApiPayloadAssertion.JsonType.NUMBER)
                                )
                        )
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build(),
                ApiTestScenario.builder()
                        .scenario("Scenario 9: grid_location là kiểu số")
                        .description("Get map node from real api")
                        .queryParams(Map.of("floor_id", "1"))
                        .requestBody("")
                        .payloadAssertions(
                                List.of(
                                        ApiPayloadAssertion.isType("data.0.grid_location", ApiPayloadAssertion.JsonType.NUMBER)
                                )
                        )
                        .expectedCode("1000")
                        .expectedStatus("SUCCESS")
                        .build()
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("Real API")
                .apiLabel("GET /map/nodes")
                .endpoint("/map/nodes")
                .sampleRequestBody(null)
                .scenarios(scenarios)
                .build();
    }
}
