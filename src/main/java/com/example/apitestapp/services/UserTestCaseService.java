package com.example.apitestapp.services;

import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.models.User;
import com.example.apitestapp.models.UserTestCase;
import com.example.apitestapp.repository.UserTestCaseRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserTestCaseService {
    private final UserTestCaseRepository repository;

    public UserTestCaseService() {
        this.repository = new UserTestCaseRepository();
    }

    public UserTestCase create(String apiLabel,
                               String suiteId,
                               String name,
                               String description,
                               String method,
                               String endpoint,
                               Map<String, String> headers,
                               Map<String, List<String>> queryParams,
                               Map<String, String> pathParams,
                               String requestBody,
                               List<ApiSetupRequest> setupRequests,
                               List<ApiCleanupRequest> cleanupRequests,
                               List<ApiPayloadAssertion> payloadAssertions,
                               String expectedResponseBody,
                               int expectedStatusCode) throws SQLException {
        validateRequired(apiLabel, "API");
        validateRequired(name, "Tên testcase");
        validateRequired(method, "Method");
        validateRequired(endpoint, "Endpoint");
        validateStatusCode(expectedStatusCode);
        validateJsonBody(requestBody);
        validateJsonBody(expectedResponseBody);

        User currentUser = AppSession.getInstance().getCurrentUser();
        String ownerName = AppSession.getUsername();

        UserTestCase testCase = new UserTestCase();
        testCase.setUserId(currentUser == null ? null : currentUser.getId());
        testCase.setSuiteId(normalizeBlank(suiteId));
        testCase.setOwnerName(ownerName == null || ownerName.isBlank() ? "User" : ownerName);
        testCase.setApiLabel(apiLabel.trim());
        testCase.setName(name.trim());
        testCase.setDescription(description);
        testCase.setMethod(method);
        testCase.setEndpoint(endpoint.trim());
        testCase.setRequestHeaders(headers);
        testCase.setQueryParams(queryParams);
        testCase.setPathParams(pathParams);
        testCase.setRequestBody(normalizeBlank(requestBody));
        testCase.setSetupRequests(setupRequests);
        testCase.setCleanupRequests(cleanupRequests);
        testCase.setPayloadAssertions(payloadAssertions);
        testCase.setExpectedResponseBody(normalizeBlank(expectedResponseBody));
        testCase.setExpectedStatusCode(expectedStatusCode);
        return repository.save(testCase);
    }

    public UserTestCase update(String id,
                               String name,
                               String description,
                               String method,
                               String endpoint,
                               Map<String, String> headers,
                               Map<String, List<String>> queryParams,
                               Map<String, String> pathParams,
                               String requestBody,
                               List<ApiSetupRequest> setupRequests,
                               List<ApiCleanupRequest> cleanupRequests,
                               List<ApiPayloadAssertion> payloadAssertions,
                               String expectedResponseBody,
                               int expectedStatusCode) throws SQLException {
        validateRequired(id, "Testcase");
        validateRequired(name, "Tên testcase");
        validateRequired(method, "Method");
        validateRequired(endpoint, "Endpoint");
        validateStatusCode(expectedStatusCode);
        validateJsonBody(requestBody);
        validateJsonBody(expectedResponseBody);

        UserTestCase testCase = new UserTestCase();
        testCase.setId(id);
        testCase.setName(name.trim());
        testCase.setDescription(description);
        testCase.setMethod(method);
        testCase.setEndpoint(endpoint.trim());
        testCase.setRequestHeaders(headers);
        testCase.setQueryParams(queryParams);
        testCase.setPathParams(pathParams);
        testCase.setRequestBody(normalizeBlank(requestBody));
        testCase.setSetupRequests(setupRequests);
        testCase.setCleanupRequests(cleanupRequests);
        testCase.setPayloadAssertions(payloadAssertions);
        testCase.setExpectedResponseBody(normalizeBlank(expectedResponseBody));
        testCase.setExpectedStatusCode(expectedStatusCode);
        return repository.update(testCase);
    }

    public void delete(String id) throws SQLException {
        validateRequired(id, "Testcase");
        repository.softDelete(id);
    }

    public List<UserTestCase> findForCurrentUserAndApi(String apiLabel) throws SQLException {
        User currentUser = AppSession.getInstance().getCurrentUser();
        String userId = currentUser == null ? null : currentUser.getId();
        String ownerName = AppSession.getUsername();
        return repository.findActiveByOwnerAndApi(userId, ownerName == null ? "User" : ownerName, apiLabel);
    }

    public List<UserTestCase> findBySuite(String suiteId) throws SQLException {
        validateRequired(suiteId, "Testsuit");
        return repository.findActiveBySuite(suiteId);
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        }
    }

    private void validateStatusCode(int statusCode) {
        if (statusCode < 0 || statusCode > 9999) {
            throw new IllegalArgumentException("Expected status code phải nằm trong khoảng 0-9999.");
        }
    }

    private void validateJsonBody(String requestBody) {
        if (requestBody == null || requestBody.isBlank()) {
            return;
        }

        // Setup/test bodies may reference values captured from earlier setup responses.
        // Replace those tokens only for syntax validation; runtime execution keeps the original body.
        String validationBody = requestBody.replaceAll("\\$\\{[A-Za-z0-9_]+}", "0");
        JsonParser.parseString(validationBody);
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public List<ApiSetupRequest> parseSetupRequests(String json) {
        return parseHookRequests(json, false).stream()
                .map(request -> (ApiSetupRequest) request)
                .toList();
    }

    public List<ApiCleanupRequest> parseCleanupRequests(String json) {
        return parseHookRequests(json, true).stream()
                .map(request -> (ApiCleanupRequest) request)
                .toList();
    }

    public String toJson(List<? extends ApiSetupRequest> requests) {
        JsonArray array = new JsonArray();
        if (requests == null) {
            return array.toString();
        }

        for (ApiSetupRequest request : requests) {
            JsonObject object = new JsonObject();
            object.addProperty("name", request.getName());
            object.addProperty("method", request.getMethod());
            object.addProperty("endpoint", request.getEndpoint());
            object.addProperty("requestBody", request.getRequestBody());
            object.add("headers", stringMapToJson(request.getHeaders()));
            object.add("expectedCodes", stringArrayToJson(request.getExpectedCodes()));
            object.addProperty("required", request.isRequired());
            object.add("responseVariables", responseVariablesToJson(request.getResponseVariables()));
            array.add(object);
        }
        return array.toString();
    }

    public RequestParams parseRequestParams(String json) {
        if (json == null || json.isBlank()) {
            return new RequestParams(Map.of(), Map.of());
        }

        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Params phải là JSON object.");
        }

        JsonObject object = root.getAsJsonObject();
        return new RequestParams(
                readMultiValueMap(object.get("queryParams"), "queryParams"),
                readStringMap(object.get("pathParams"))
        );
    }

    public String requestParamsToJson(Map<String, List<String>> queryParams, Map<String, String> pathParams) {
        JsonObject object = new JsonObject();
        object.add("queryParams", multiValueMapToJson(queryParams));
        object.add("pathParams", stringMapToJson(pathParams));
        return object.toString();
    }

    public List<ApiPayloadAssertion> parsePayloadAssertions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonArray()) {
            throw new IllegalArgumentException("Response assertions phải là JSON array.");
        }

        List<ApiPayloadAssertion> assertions = new ArrayList<>();
        for (JsonElement item : root.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                throw new IllegalArgumentException("Mỗi response assertion phải là JSON object.");
            }
            JsonObject object = item.getAsJsonObject();
            String jsonPath = getString(object, "jsonPath", "");
            String operatorText = getString(object, "operator", "");
            validateRequired(jsonPath, "JSON path");
            validateRequired(operatorText, "Assertion operator");
            ApiPayloadAssertion.Operator operator;
            try {
                operator = ApiPayloadAssertion.Operator.valueOf(operatorText.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Assertion operator không hỗ trợ: " + operatorText);
            }
            String expectedValue = object.has("expectedValue") && !object.get("expectedValue").isJsonNull()
                    ? object.get("expectedValue").getAsString()
                    : null;
            assertions.add(new ApiPayloadAssertion(jsonPath.trim(), operator, expectedValue));
        }
        return assertions;
    }

    public String payloadAssertionsToJson(List<ApiPayloadAssertion> assertions) {
        JsonArray array = new JsonArray();
        if (assertions == null) {
            return array.toString();
        }
        for (ApiPayloadAssertion assertion : assertions) {
            JsonObject object = new JsonObject();
            object.addProperty("jsonPath", assertion.getJsonPath());
            object.addProperty("operator", assertion.getOperator().name());
            object.addProperty("expectedValue", assertion.getExpectedValue());
            array.add(object);
        }
        return array.toString();
    }

    private Map<String, List<String>> readMultiValueMap(JsonElement element, String fieldName) {
        if (element == null || element.isJsonNull()) {
            return Map.of();
        }
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException(fieldName + " phải là JSON object.");
        }

        Map<String, List<String>> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            List<String> items = new ArrayList<>();
            if (value != null && value.isJsonArray()) {
                for (JsonElement item : value.getAsJsonArray()) {
                    items.add(item == null || item.isJsonNull() ? "" : item.getAsString());
                }
            } else {
                items.add(value == null || value.isJsonNull() ? "" : value.getAsString());
            }
            values.put(entry.getKey(), List.copyOf(items));
        }
        return values;
    }

    private JsonObject multiValueMapToJson(Map<String, List<String>> values) {
        JsonObject object = new JsonObject();
        if (values == null) {
            return object;
        }
        values.forEach((key, items) -> {
            if (key == null || key.isBlank()) {
                return;
            }
            JsonArray array = new JsonArray();
            if (items != null) {
                items.forEach(value -> array.add(value == null ? "" : value));
            }
            if (array.size() == 1) {
                object.addProperty(key, array.get(0).getAsString());
            } else {
                object.add(key, array);
            }
        });
        return object;
    }

    public record RequestParams(Map<String, List<String>> queryParams, Map<String, String> pathParams) {
    }

    private List<? extends ApiSetupRequest> parseHookRequests(String json, boolean cleanup) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonArray()) {
            throw new IllegalArgumentException((cleanup ? "Cleanup" : "Setup") + " phải là JSON array.");
        }

        List<ApiSetupRequest> requests = new ArrayList<>();
        for (JsonElement item : root.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                throw new IllegalArgumentException("Mỗi request setup/cleanup phải là JSON object.");
            }
            JsonObject object = item.getAsJsonObject();
            String name = getString(object, "name", cleanup ? "Cleanup data" : "Setup data");
            String method = getString(object, "method", cleanup ? "DELETE" : "POST");
            String endpoint = getString(object, "endpoint", "");
            validateRequired(endpoint, "Endpoint setup/cleanup");
            String requestBody = getString(object, "requestBody", "");
            validateJsonBody(requestBody);
            Map<String, String> headers = readStringMap(object.get("headers"));
            List<String> expectedCodes = readStringArray(object.get("expectedCodes"));
            boolean required = getBoolean(object, "required", true);
            List<ApiResponseVariable> variables = readResponseVariables(object.get("responseVariables"));

            if (cleanup) {
                requests.add(new ApiCleanupRequest(name, method, endpoint, requestBody, headers, expectedCodes, required));
            } else {
                requests.add(new ApiSetupRequest(name, method, endpoint, requestBody, headers, expectedCodes, required, variables));
            }
        }
        return requests;
    }

    private Map<String, String> readStringMap(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return Map.of();
        }
        Map<String, String> values = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            values.put(entry.getKey(), value == null || value.isJsonNull() ? "" : value.getAsString());
        }
        return values;
    }

    private List<String> readStringArray(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonElement item : element.getAsJsonArray()) {
            values.add(item.getAsString());
        }
        return values;
    }

    private List<ApiResponseVariable> readResponseVariables(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }
        List<ApiResponseVariable> variables = new ArrayList<>();
        for (JsonElement item : element.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject object = item.getAsJsonObject();
            variables.add(new ApiResponseVariable(
                    getString(object, "name", ""),
                    getString(object, "jsonPath", "")
            ));
        }
        return variables;
    }

    private JsonArray stringArrayToJson(List<String> values) {
        JsonArray array = new JsonArray();
        if (values == null) {
            return array;
        }
        values.forEach(array::add);
        return array;
    }

    private JsonObject stringMapToJson(Map<String, String> values) {
        JsonObject object = new JsonObject();
        if (values == null) {
            return object;
        }
        values.forEach((key, value) -> {
            if (key != null && !key.isBlank()) {
                object.addProperty(key, value == null ? "" : value);
            }
        });
        return object;
    }

    private JsonArray responseVariablesToJson(List<ApiResponseVariable> variables) {
        JsonArray array = new JsonArray();
        if (variables == null) {
            return array;
        }
        for (ApiResponseVariable variable : variables) {
            JsonObject object = new JsonObject();
            object.addProperty("name", variable.getName());
            object.addProperty("jsonPath", variable.getJsonPath());
            array.add(object);
        }
        return array;
    }

    private String getString(JsonObject object, String key, String defaultValue) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsString();
    }

    private boolean getBoolean(JsonObject object, String key, boolean defaultValue) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsBoolean();
    }
}
