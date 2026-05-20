package com.example.apitestapp.repository;

import com.example.apitestapp.db.ConnectionManager;
import com.example.apitestapp.models.UserTestCase;
import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiResponseVariable;
import com.example.apitestapp.services.ApiSetupRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserTestCaseRepository {
    private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private final Gson gson = new Gson();

    public UserTestCaseRepository() {
        ensureTable();
    }

    public UserTestCase save(UserTestCase testCase) throws SQLException {
        String sql = """
                INSERT INTO user_test_cases (
                    user_id, suite_id, owner_name, api_label, name, description, method, endpoint,
                    request_headers, query_params, request_body, setup_requests, cleanup_requests, expected_status_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?)
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
            setNullableJson(ps, 11, testCase.getRequestBody());
            ps.setString(12, hookRequestsToJson(testCase.getSetupRequests()));
            ps.setString(13, hookRequestsToJson(testCase.getCleanupRequests()));
            ps.setInt(14, testCase.getExpectedStatusCode());

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
                    request_headers = ?::jsonb, query_params = ?::jsonb, request_body = ?::jsonb,
                    setup_requests = ?::jsonb, cleanup_requests = ?::jsonb,
                    expected_status_code = ?, updated_at = NOW()
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
            setNullableJson(ps, 7, testCase.getRequestBody());
            ps.setString(8, hookRequestsToJson(testCase.getSetupRequests()));
            ps.setString(9, hookRequestsToJson(testCase.getCleanupRequests()));
            ps.setInt(10, testCase.getExpectedStatusCode());
            ps.setString(11, testCase.getId());

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

    private void ensureTable() {
        String extensionSql = "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"";
        String sql = """
                CREATE TABLE IF NOT EXISTS user_test_cases
                (
                    id                   VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
                    user_id              VARCHAR(255) REFERENCES users (id),
                    suite_id             VARCHAR(255) REFERENCES user_test_suites (id),
                    owner_name           VARCHAR(255) NOT NULL,
                    api_label            VARCHAR(255) NOT NULL,
                    name                 VARCHAR(255) NOT NULL,
                    description          TEXT,
                    method               VARCHAR(10) NOT NULL,
                    endpoint             VARCHAR(2048) NOT NULL,
                    request_headers      JSONB NOT NULL DEFAULT '{}',
                    query_params         JSONB NOT NULL DEFAULT '{}',
                    request_body         JSONB,
                    setup_requests       JSONB NOT NULL DEFAULT '[]',
                    cleanup_requests     JSONB NOT NULL DEFAULT '[]',
                    expected_status_code INTEGER NOT NULL,
                    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
                    updated_at           TIMESTAMP
                )
                """;

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement extensionPs = c.prepareStatement(extensionSql);
             PreparedStatement tablePs = c.prepareStatement(sql)) {
            extensionPs.executeUpdate();
            tablePs.executeUpdate();
            ensureSuiteIdColumn(c);
            ensureJsonColumn(c, "setup_requests", "'[]'");
            ensureJsonColumn(c, "cleanup_requests", "'[]'");
        } catch (SQLException e) {
            System.err.println("Không thể khởi tạo bảng user_test_cases: " + e.getMessage());
        }
    }

    private void ensureSuiteIdColumn(Connection c) throws SQLException {
        String sql = "ALTER TABLE user_test_cases ADD COLUMN IF NOT EXISTS suite_id VARCHAR(255) REFERENCES user_test_suites (id)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private void ensureJsonColumn(Connection c, String column, String defaultValue) throws SQLException {
        String sql = "ALTER TABLE user_test_cases ADD COLUMN IF NOT EXISTS " + column + " JSONB NOT NULL DEFAULT " + defaultValue;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        }
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
        testCase.setRequestHeaders(readMap(rs.getString("request_headers")));
        testCase.setQueryParams(readMap(rs.getString("query_params")));
        testCase.setRequestBody(rs.getString("request_body"));
        testCase.setSetupRequests(readSetupRequests(rs.getString("setup_requests")));
        testCase.setCleanupRequests(readCleanupRequests(rs.getString("cleanup_requests")));
        testCase.setExpectedStatusCode(rs.getInt("expected_status_code"));
        testCase.setActive(rs.getBoolean("is_active"));
        testCase.setCreatedAt(readLocalDateTime(rs, "created_at"));
        testCase.setUpdatedAt(readLocalDateTime(rs, "updated_at"));
        return testCase;
    }

    private Map<String, String> readMap(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }

        Map<String, String> parsed = gson.fromJson(json, STRING_MAP_TYPE);
        return parsed == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parsed);
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
            List<String> expectedCodes = readStringArray(object.get("expectedCodes"));
            boolean required = getBoolean(object, "required", true);
            List<ApiResponseVariable> variables = readResponseVariables(object.get("responseVariables"));

            if (cleanup) {
                requests.add(new ApiCleanupRequest(name, method, endpoint, requestBody, expectedCodes, required));
            } else {
                requests.add(new ApiSetupRequest(name, method, endpoint, requestBody, expectedCodes, required, variables));
            }
        }
        return requests;
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

    private boolean getBoolean(JsonObject object, String key, boolean defaultValue) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsBoolean();
    }

    private LocalDateTime readLocalDateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private Map<String, String> nonNullMap(Map<String, String> values) {
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
