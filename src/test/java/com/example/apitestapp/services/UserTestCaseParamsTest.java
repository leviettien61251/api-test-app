package com.example.apitestapp.services;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTestCaseParamsTest {
    private final UserTestCaseService service = new UserTestCaseService();

    @Test
    void parsesScalarArrayAndPathParams() {
        UserTestCaseService.RequestParams params = service.parseRequestParams("""
                {
                  "queryParams": {
                    "filter": "active",
                    "tag": ["indoor", "priority"]
                  },
                  "pathParams": {
                    "id": 123
                  }
                }
                """);

        assertEquals(List.of("active"), params.queryParams().get("filter"));
        assertEquals(List.of("indoor", "priority"), params.queryParams().get("tag"));
        assertEquals("123", params.pathParams().get("id"));
    }

    @Test
    void appendsRepeatedQueryParameters() throws Exception {
        ApiTestService apiTestService = new ApiTestService("http://localhost:8080");
        Method resolveUrl = ApiTestService.class.getDeclaredMethod("resolveUrl", String.class, Map.class);
        resolveUrl.setAccessible(true);

        String url = (String) resolveUrl.invoke(apiTestService, "/items", Map.of("tag", List.of("indoor", "priority")));

        assertEquals("http://localhost:8080/items?tag=indoor&tag=priority", url);
    }

    @Test
    void parsesResponseAssertionsWithRuntimeVariables() {
        List<ApiPayloadAssertion> assertions = service.parsePayloadAssertions("""
                [
                  {
                    "jsonPath": "data.id",
                    "operator": "equals",
                    "expectedValue": "${mapId}"
                  }
                ]
                """);

        assertEquals(1, assertions.size());
        assertEquals(ApiPayloadAssertion.Operator.EQUALS, assertions.get(0).getOperator());
        assertEquals("${mapId}", assertions.get(0).getExpectedValue());
    }
}
