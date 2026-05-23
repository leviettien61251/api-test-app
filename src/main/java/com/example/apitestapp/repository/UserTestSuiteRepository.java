package com.example.apitestapp.repository;

import com.example.apitestapp.db.ConnectionManager;
import com.example.apitestapp.models.UserTestSuite;
import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiSetupRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import java.util.Optional;

public class UserTestSuiteRepository {

    public UserTestSuite save(UserTestSuite suite) throws SQLException {
        String sql = """
                INSERT INTO user_test_suites (user_id, owner_name, name, method, endpoint, description)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING *
                """;

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setNullableString(ps, 1, suite.getUserId());
            ps.setString(2, suite.getOwnerName());
            ps.setString(3, suite.getName());
            ps.setString(4, normalizeMethod(suite.getMethod()));
            ps.setString(5, suite.getEndpoint());
            setNullableString(ps, 6, suite.getDescription());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return suite;
    }

    public UserTestSuite updateCleanupRequests(String id, List<ApiCleanupRequest> cleanupRequests) throws SQLException {
        String sql = """
                UPDATE user_test_suites
                SET cleanup_requests = ?::jsonb, updated_at = NOW()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hookRequestsToJson(cleanupRequests));
            ps.setString(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return findById(id).orElse(null);
    }

    public UserTestSuite update(UserTestSuite suite) throws SQLException {
        String sql = """
                UPDATE user_test_suites
                SET name = ?, method = ?, endpoint = ?, description = ?, updated_at = NOW()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, suite.getName());
            ps.setString(2, normalizeMethod(suite.getMethod()));
            ps.setString(3, suite.getEndpoint());
            setNullableString(ps, 4, suite.getDescription());
            ps.setString(5, suite.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return suite;
    }

    public void softDelete(String id) throws SQLException {
        String sql = "UPDATE user_test_suites SET is_active = FALSE, updated_at = NOW() WHERE id = ?";
        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<UserTestSuite> findById(String id) throws SQLException {
        String sql = "SELECT * FROM user_test_suites WHERE id = ? AND is_active = TRUE";
        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<UserTestSuite> findActiveByOwner(String userId, String ownerName) throws SQLException {
        String sql = """
                SELECT *
                FROM user_test_suites
                WHERE is_active = TRUE
                  AND (
                      (? IS NOT NULL AND user_id = ?)
                      OR (? IS NULL AND owner_name = ?)
                  )
                ORDER BY created_at
                """;

        List<UserTestSuite> suites = new ArrayList<>();
        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setNullableString(ps, 1, userId);
            setNullableString(ps, 2, userId);
            setNullableString(ps, 3, userId);
            ps.setString(4, ownerName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suites.add(mapRow(rs));
                }
            }
        }

        return suites;
    }

    private UserTestSuite mapRow(ResultSet rs) throws SQLException {
        UserTestSuite suite = new UserTestSuite();
        suite.setId(rs.getString("id"));
        suite.setUserId(rs.getString("user_id"));
        suite.setOwnerName(rs.getString("owner_name"));
        suite.setName(rs.getString("name"));
        suite.setMethod(rs.getString("method"));
        suite.setEndpoint(rs.getString("endpoint"));
        suite.setDescription(rs.getString("description"));
        suite.setCleanupRequests(readCleanupRequests(rs.getString("cleanup_requests")));
        suite.setActive(rs.getBoolean("is_active"));
        suite.setCreatedAt(readLocalDateTime(rs, "created_at"));
        suite.setUpdatedAt(readLocalDateTime(rs, "updated_at"));
        return suite;
    }

    private LocalDateTime readLocalDateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
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
            array.add(object);
        }
        return array.toString();
    }

    private List<ApiCleanupRequest> readCleanupRequests(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        List<ApiCleanupRequest> requests = new ArrayList<>();
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonArray()) {
            return List.of();
        }

        for (JsonElement item : root.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject object = item.getAsJsonObject();
            requests.add(new ApiCleanupRequest(
                    getString(object, "name", "Cleanup data"),
                    getString(object, "method", "DELETE"),
                    getString(object, "endpoint", ""),
                    getString(object, "requestBody", ""),
                    readStringMap(object.get("headers")),
                    readStringArray(object.get("expectedCodes")),
                    getBoolean(object, "required", true)
            ));
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

    private JsonArray stringArrayToJson(List<String> values) {
        JsonArray array = new JsonArray();
        if (values != null) {
            values.forEach(array::add);
        }
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

    private String getString(JsonObject object, String key, String defaultValue) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsString();
    }

    private boolean getBoolean(JsonObject object, String key, boolean defaultValue) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsBoolean();
    }
}
