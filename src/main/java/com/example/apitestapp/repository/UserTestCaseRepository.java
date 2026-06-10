package com.example.apitestapp.repository;

import com.example.apitestapp.db.ConnectionManager;
import com.example.apitestapp.models.dto.ApiCleanupRequest;
import com.example.apitestapp.models.dto.ApiPayloadAssertion;
import com.example.apitestapp.models.dto.ApiResponseVariable;
import com.example.apitestapp.models.dto.ApiSetupRequest;
import com.example.apitestapp.models.entity.UserTestCase;
import com.google.gson.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserTestCaseRepository {
    private final Gson gson = new Gson();

    public UserTestCase save(UserTestCase testCase) throws SQLException {
        String sql = """
                INSERT INTO user_test_cases (
                    user_id, suite_id, owner_name, api_label, name, description, method, endpoint,
                    request_headers, query_params, path_params, request_body, setup_requests, cleanup_requests,
                    payload_assertions, expected_response_body, expected_status_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?)
                RETURNING *
                """;

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setNullableString(ps, 1, testCase.getUserId());
            setNullableString(ps, 2, testCase.getSuiteId());
            ps.setString(3, testCase.getOwnerName());
            ps.setString(4, testCase.getApiLabel());
            ps.setString(5, testCase.getName());
            setNullableString(ps, 6, testCase.getDescription());
            ps.setString(7, normalizeMethod(testCase.getMethod()));
            ps.setString(8, testCase.getEndpoint());
            ps.setString(9, gson.toJson(nonNullMap(testCase.getRequestHeaders())));
            ps.setString(10, gson.toJson(nonNullMap(testCase.getQueryParams())));
            ps.setString(11, gson.toJson(nonNullMap(testCase.getPathParams())));
            setNullableJson(ps, 12, testCase.getRequestBody());
            ps.setString(13, hookRequestsToJson(testCase.getSetupRequests()));
            ps.setString(14, hookRequestsToJson(testCase.getCleanupRequests()));
            ps.setString(15, payloadAssertionsToJson(testCase.getPayloadAssertions()));
            setNullableJson(ps, 16, testCase.getExpectedResponseBody());
            ps.setInt(17, testCase.getExpectedStatusCode());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return testCase;
    }

    public UserTestCase update(UserTestCase testCase) throws SQLException {
        String sql = """
                UPDATE user_test_cases
                SET name = ?, description = ?, method = ?, endpoint = ?,
                    request_headers = ?::jsonb, query_params = ?::jsonb, path_params = ?::jsonb, request_body = ?,
                    setup_requests = ?::jsonb, cleanup_requests = ?::jsonb, payload_assertions = ?::jsonb,
                    expected_response_body = ?, expected_status_code = ?, updated_at = NOW()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, testCase.getName());
            setNullableString(ps, 2, testCase.getDescription());
            ps.setString(3, normalizeMethod(testCase.getMethod()));
            ps.setString(4, testCase.getEndpoint());
            ps.setString(5, gson.toJson(nonNullMap(testCase.getRequestHeaders())));
            ps.setString(6, gson.toJson(nonNullMap(testCase.getQueryParams())));
            ps.setString(7, gson.toJson(nonNullMap(testCase.getPathParams())));
            setNullableJson(ps, 8, testCase.getRequestBody());
            ps.setString(9, hookRequestsToJson(testCase.getSetupRequests()));
            ps.setString(10, hookRequestsToJson(testCase.getCleanupRequests()));
            ps.setString(11, payloadAssertionsToJson(testCase.getPayloadAssertions()));
            setNullableJson(ps, 12, testCase.getExpectedResponseBody());
            ps.setInt(13, testCase.getExpectedStatusCode());
            ps.setString(14, testCase.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return testCase;
    }

    public void softDelete(String id) throws SQLException {
        String sql = "UPDATE user_test_cases SET is_active = FALSE, updated_at = NOW() WHERE id = ?";
        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public List<UserTestCase> findActiveByOwnerAndApi(String userId, String ownerName, String apiLabel) throws SQLException {
        String sql = """
                SELECT *
                FROM user_test_cases
                WHERE is_active = TRUE
                  AND api_label = ?
                  AND (
                      (? IS NOT NULL AND user_id = ?)
                      OR (? IS NULL AND owner_name = ?)
                  )
                ORDER BY created_at
                """;

        List<UserTestCase> testCases = new ArrayList<>();
        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, apiLabel);
            setNullableString(ps, 2, userId);
            setNullableString(ps, 3, userId);
            setNullableString(ps, 4, userId);
            ps.setString(5, ownerName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    testCases.add(mapRow(rs));
                }
            }
        }

        return testCases;
    }

    public List<UserTestCase> findActiveBySuite(String suiteId) throws SQLException {
        String sql = """
                SELECT *
                FROM user_test_cases
                WHERE is_active = TRUE
                  AND suite_id = ?
                ORDER BY created_at
                """;

        List<UserTestCase> testCases = new ArrayList<>();
        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, suiteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    testCases.add(mapRow(rs));
                }
            }
        }

        return testCases;
    }

    private UserTestCase mapRow(ResultSet rs) throws SQLException {
        UserTestCase testCase = new UserTestCase();
        testCase.setId(rs.getString("id"));
        testCase.setUserId(rs.getString("user_id"));
        testCase.setSuiteId(rs.getString("suite_id"));
        testCase.setOwnerName(rs.getString("owner_name"));
        testCase.setApiLabel(rs.getString("api_label"));
        testCase.setName(rs.getString("name"));
        testCase.setDescription(rs.getString("description"));
        testCase.setMethod(rs.getString("method"));
        testCase.setEndpoint(rs.getString("endpoint"));
        testCase.setRequestHeaders(readStringMap(rs.getString("request_headers")));
        testCase.setQueryParams(readMultiValueMap(rs.getString("query_params")));
        testCase.setPathParams(readStringMap(rs.getString("path_params")));
        testCase.setRequestBody(rs.getString("request_body"));
        testCase.setSetupRequests(readSetupRequests(rs.getString("setup_requests")));
        testCase.setCleanupRequests(readCleanupRequests(rs.getString("cleanup_requests")));
        testCase.setPayloadAssertions(readPayloadAssertions(rs.getString("payload_assertions")));
        testCase.setExpectedResponseBody(rs.getString("expected_response_body"));
        testCase.setExpectedStatusCode(rs.getInt("expected_status_code"));
        testCase.setActive(rs.getBoolean("is_active"));
        testCase.setCreatedAt(readLocalDateTime(rs, "created_at"));
        testCase.setUpdatedAt(readLocalDateTime(rs, "updated_at"));
        return testCase;
    }

    private Map<String, String> readStringMap(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }

        return readStringMap(JsonParser.parseString(json));
    }

    private Map<String, List<String>> readMultiValueMap(String json) {
        Map<String, List<String>> values = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return values;
        }

        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            return values;
        }
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            List<String> items = new ArrayList<>();
            if (value != null && value.isJsonArray()) {
                value.getAsJsonArray().forEach(item -> items.add(item == null || item.isJsonNull() ? "" : item.getAsString()));
            } else {
                items.add(value == null || value.isJsonNull() ? "" : value.getAsString());
            }
            values.put(entry.getKey(), items);
        }
        return values;
    }

    private List<ApiSetupRequest> readSetupRequests(String json) {
        return readHookRequests(json, false).stream()
                .map(request -> (ApiSetupRequest) request)
                .toList();
    }

    private List<ApiCleanupRequest> readCleanupRequests(String json) {
        return readHookRequests(json, true).stream()
                .map(request -> (ApiCleanupRequest) request)
                .toList();
    }

    private List<ApiPayloadAssertion> readPayloadAssertions(String json) {
        List<ApiPayloadAssertion> assertions = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return assertions;
        }
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonArray()) {
            return assertions;
        }
        for (JsonElement item : root.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject object = item.getAsJsonObject();
            try {
                assertions.add(new ApiPayloadAssertion(
                        getString(object, "jsonPath", ""),
                        ApiPayloadAssertion.Operator.valueOf(getString(object, "operator", "")),
                        getNullableString(object, "expectedValue")
                ));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed legacy assertions instead of blocking testcase loading.
            }
        }
        return assertions;
    }

    private String payloadAssertionsToJson(List<ApiPayloadAssertion> assertions) {
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

    private String hookRequestsToJson(List<? extends ApiSetupRequest> requests) {
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

    private List<? extends ApiSetupRequest> readHookRequests(String json, boolean cleanup) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        List<ApiSetupRequest> requests = new ArrayList<>();
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonArray()) {
            return List.of();
        }

        JsonArray array = root.getAsJsonArray();
        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject object = item.getAsJsonObject();
            String name = getString(object, "name", cleanup ? "Cleanup data" : "Setup data");
            String method = getString(object, "method", cleanup ? "DELETE" : "POST");
            String endpoint = getString(object, "endpoint", "");
            String requestBody = getString(object, "requestBody", "");
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
        Map<String, String> values = new LinkedHashMap<>();
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
            if (item.isJsonPrimitive()) {
                values.add(item.getAsString());
            }
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

    private String getString(JsonObject object, String key, String defaultValue) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsString();
    }

    private String getNullableString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private boolean getBoolean(JsonObject object, String key, boolean defaultValue) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsBoolean();
    }

    private LocalDateTime readLocalDateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private Map<?, ?> nonNullMap(Map<?, ?> values) {
        return values == null ? Map.of() : values;
    }

    private String normalizeMethod(String method) {
        return method == null || method.isBlank() ? "POST" : method.trim().toUpperCase();
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private void setNullableJson(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}
