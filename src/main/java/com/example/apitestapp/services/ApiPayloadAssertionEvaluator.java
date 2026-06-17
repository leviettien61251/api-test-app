package com.example.apitestapp.services;

import com.example.apitestapp.models.dto.ApiPayloadAssertion;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Đánh giá nội dung JSON trả về từ API dựa trên danh sách payload assertion
 * và, nếu có, toàn bộ response JSON mong đợi.
 *
 * <p>JSON path được hỗ trợ theo cú pháp phân tách bằng dấu chấm, ví dụ
 * {@code data.items.0.id}. Thành phần số được dùng làm chỉ số khi giá trị hiện
 * tại là một JSON array.</p>
 */
public class ApiPayloadAssertionEvaluator {

    /**
     * Đánh giá response body bằng danh sách assertion, không so sánh toàn bộ response.
     *
     * @param responseBody JSON response cần đánh giá
     * @param assertions   danh sách điều kiện cần kiểm tra; {@code null} được xem như danh sách rỗng
     * @return kết quả tổng hợp và thông báo của từng assertion
     */
    public Evaluation evaluate(String responseBody, List<ApiPayloadAssertion> assertions) {
        return evaluate(responseBody, assertions, null);
    }

    /**
     * Đánh giá response body bằng các assertion và response JSON mong đợi.
     * Phép đánh giá chỉ PASS khi so sánh toàn bộ response, nếu được cấu hình,
     * và tất cả assertion đều thành công.
     *
     * @param responseBody         JSON response cần đánh giá
     * @param assertions           danh sách điều kiện cần kiểm tra
     * @param expectedResponseBody JSON dùng để so sánh toàn bộ response; có thể để trống
     * @return kết quả tổng hợp cùng các thông báo chi tiết
     */
    public Evaluation evaluate(String responseBody,
                               List<ApiPayloadAssertion> assertions,
                               String expectedResponseBody) {
        List<ApiPayloadAssertion> normalizedAssertions = assertions == null ? List.of() : assertions;
        boolean hasExpectedResponse = expectedResponseBody != null && !expectedResponseBody.isBlank();
        if (normalizedAssertions.isEmpty()) {
            if (!hasExpectedResponse) {
                return Evaluation.passed(List.of());
            }
        }
        if (responseBody == null || responseBody.isBlank()) {
            return Evaluation.failed(List.of("Response body rỗng nên không thể so sánh payload."));
        }

        JsonElement responseJson;
        try {
            responseJson = JsonParser.parseString(responseBody);
        } catch (Exception e) {
            return Evaluation.failed(List.of("Response body không phải JSON hợp lệ."));
        }

        List<String> messages = new ArrayList<>();
        boolean passed = true;
        if (hasExpectedResponse) {
            try {
                JsonElement expectedJson = JsonParser.parseString(expectedResponseBody);
                boolean fullResponsePassed = expectedJson.equals(responseJson);
                messages.add(fullResponsePassed
                        ? "Full response JSON khớp expected response."
                        : "Full response JSON không khớp expected response.");
                passed = fullResponsePassed;
            } catch (Exception e) {
                return Evaluation.failed(List.of("Expected response body không phải JSON hợp lệ."));
            }
        }
        for (ApiPayloadAssertion assertion : normalizedAssertions) {
            AssertionCheck check = check(responseJson, assertion);
            messages.add(check.message());
            if (!check.passed()) {
                passed = false;
            }
        }
        return new Evaluation(passed, messages);
    }

    /** Điều phối assertion đến hàm kiểm tra tương ứng với operator được khai báo. */
    private AssertionCheck check(JsonElement responseJson, ApiPayloadAssertion assertion) {
        if (assertion == null || assertion.getOperator() == null) {
            return AssertionCheck.failed("Payload assertion không hợp lệ.");
        }

        Optional<JsonElement> actualValue = findJsonValue(responseJson, assertion.getJsonPath());
        if (assertion.getOperator() == ApiPayloadAssertion.Operator.EXISTS) {
            boolean shouldExist = assertion.getExpectedValue() == null
                    || Boolean.parseBoolean(assertion.getExpectedValue());
            boolean exists = actualValue.isPresent();
            return comparisonResult(exists == shouldExist, assertion, String.valueOf(exists), String.valueOf(shouldExist));
        }
        if (actualValue.isEmpty()) {
            return AssertionCheck.failed("Payload thiếu path `" + assertion.getJsonPath() + "`.");
        }

        JsonElement actual = actualValue.get();
        return switch (assertion.getOperator()) {
            case EQUALS -> equalsCheck(assertion, actual);
            case NOT_EQUALS -> notEqualsCheck(assertion, actual);
            case GREATER_THAN -> greaterThanCheck(assertion, actual);
            case LESS_THAN -> lessThanCheck(assertion, actual);
            case STARTS_WITH -> startsWithCheck(assertion, actual);
            case CONTAINS -> containsCheck(assertion, actual);
            case ARRAY_LENGTH -> arrayLengthCheck(assertion, actual);
            case JSON_TYPE -> jsonTypeCheck(assertion, actual);
            case EXISTS -> throw new IllegalStateException("EXISTS phải được xử lý trước khi đọc payload.");
        };
    }

    /** So sánh bằng; số được so sánh bằng {@link BigDecimal} để tránh sai lệch định dạng. */
    private AssertionCheck equalsCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        String expected = assertion.getExpectedValue();
        if (actual.isJsonPrimitive() && actual.getAsJsonPrimitive().isNumber()) {
            Optional<BigDecimal> expectedNumber = parseNumber(expected);
            if (expectedNumber.isPresent()) {
                BigDecimal actualNumber = actual.getAsBigDecimal();
                boolean passed = actualNumber.compareTo(expectedNumber.get()) == 0;
                return comparisonResult(passed, assertion, actualNumber.toPlainString(), expected);
            }
        }

        String actualText = asText(actual);
        boolean passed = expected == null ? actual.isJsonNull() : expected.equals(actualText);
        return comparisonResult(passed, assertion, actualText, expected);
    }

    /** Kiểm tra giá trị số thực tế lớn hơn giá trị mong đợi. */
    private AssertionCheck greaterThanCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        return numericComparisonCheck(assertion, actual, true);
    }

    /** Kiểm tra giá trị số thực tế nhỏ hơn giá trị mong đợi. */
    private AssertionCheck lessThanCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        return numericComparisonCheck(assertion, actual, false);
    }

    /**
     * Thực hiện phép so sánh số dùng chung cho GREATER_THAN và LESS_THAN.
     * Trả về thất bại nếu một trong hai giá trị không thể chuyển thành số.
     */
    private AssertionCheck numericComparisonCheck(ApiPayloadAssertion assertion, JsonElement actual, boolean greaterThan) {
        Optional<BigDecimal> actualNumber = actualNumber(actual);
        Optional<BigDecimal> expectedNumber = parseNumber(assertion.getExpectedValue());
        if (actualNumber.isEmpty() || expectedNumber.isEmpty()) {
            return AssertionCheck.failed("Payload `" + assertion.getJsonPath() + "` không so sánh lớn hơn được với `"
                    + assertion.getExpectedValue() + "`.");
        }

        int comparison = actualNumber.get().compareTo(expectedNumber.get());
        boolean passed = greaterThan ? comparison > 0 : comparison < 0;
        return comparisonResult(passed, assertion, actualNumber.get().toPlainString(), assertion.getExpectedValue());
    }

    /** Kiểm tra không bằng bằng cách đảo kết quả của phép so sánh EQUALS. */
    private AssertionCheck notEqualsCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        AssertionCheck equals = equalsCheck(assertion, actual);
        return comparisonResult(!equals.passed(), assertion, asText(actual), assertion.getExpectedValue());
    }

    /** Kiểm tra JSON string bắt đầu bằng prefix mong đợi. */
    private AssertionCheck startsWithCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        if (!actual.isJsonPrimitive() || !actual.getAsJsonPrimitive().isString()) {
            return AssertionCheck.failed("Payload `" + assertion.getJsonPath() + "` không phải chuỗi để kiểm tra prefix.");
        }

        String actualText = actual.getAsString();
        String expectedPrefix = assertion.getExpectedValue() == null ? "" : assertion.getExpectedValue();
        boolean passed = actualText.startsWith(expectedPrefix);
        return comparisonResult(passed, assertion, actualText, expectedPrefix);
    }

    /**
     * Kiểm tra chuỗi có chứa đoạn mong đợi hoặc array có phần tử có nội dung bằng
     * giá trị mong đợi. Các loại JSON khác không hỗ trợ operator này.
     */
    private AssertionCheck containsCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        String expected = assertion.getExpectedValue() == null ? "" : assertion.getExpectedValue();
        boolean passed;
        if (actual.isJsonArray()) {
            passed = actual.getAsJsonArray().asList().stream().anyMatch(item -> expected.equals(asText(item)));
        } else if (actual.isJsonPrimitive() && actual.getAsJsonPrimitive().isString()) {
            passed = actual.getAsString().contains(expected);
        } else {
            return AssertionCheck.failed("Payload `" + assertion.getJsonPath() + "` không hỗ trợ kiểm tra contains.");
        }
        return comparisonResult(passed, assertion, asText(actual), expected);
    }

    /** Kiểm tra số phần tử của JSON array bằng độ dài mong đợi. */
    private AssertionCheck arrayLengthCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        if (!actual.isJsonArray()) {
            return AssertionCheck.failed("Payload `" + assertion.getJsonPath() + "` không phải array.");
        }
        Optional<BigDecimal> expectedLength = parseNumber(assertion.getExpectedValue());
        if (expectedLength.isEmpty()) {
            return AssertionCheck.failed("Array length mong đợi không hợp lệ: `" + assertion.getExpectedValue() + "`.");
        }
        int actualLength = actual.getAsJsonArray().size();
        boolean passed = BigDecimal.valueOf(actualLength).compareTo(expectedLength.get()) == 0;
        return comparisonResult(passed, assertion, String.valueOf(actualLength), assertion.getExpectedValue());
    }

    /** Kiểm tra kiểu JSON thực tế có tên trùng với kiểu mong đợi. */
    private AssertionCheck jsonTypeCheck(ApiPayloadAssertion assertion, JsonElement actual) {
        ApiPayloadAssertion.JsonType actualType = jsonType(actual);
        String expectedType = assertion.getExpectedValue();
        boolean passed = actualType.name().equals(expectedType);
        return comparisonResult(passed, assertion, actualType.name(), expectedType);
    }

    /** Tạo kết quả kiểm tra với thông báo thống nhất gồm path, operator, expected và actual. */
    private AssertionCheck comparisonResult(boolean passed,
                                            ApiPayloadAssertion assertion,
                                            String actualValue,
                                            String expectedValue) {
        String message = "Payload `" + assertion.getJsonPath() + "` " + assertion.getOperator()
                + " expected `" + expectedValue + "`, actual `" + actualValue + "`.";
        return passed ? AssertionCheck.passed(message) : AssertionCheck.failed(message);
    }

    /**
     * Tìm giá trị JSON theo path phân tách bằng dấu chấm, hỗ trợ thuộc tính object
     * và chỉ số array. Trả về rỗng nếu path không tồn tại hoặc không hợp lệ.
     */
    private Optional<JsonElement> findJsonValue(JsonElement root, String jsonPath) {
        if (root == null || jsonPath == null || jsonPath.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonElement current = root;
            for (String part : jsonPath.split("\\.")) {
                if (current.isJsonObject()) {
                    if (!current.getAsJsonObject().has(part)) {
                        return Optional.empty();
                    }
                    current = current.getAsJsonObject().get(part);
                } else if (current.isJsonArray()) {
                    int index = Integer.parseInt(part);
                    if (index < 0 || index >= current.getAsJsonArray().size()) {
                        return Optional.empty();
                    }
                    current = current.getAsJsonArray().get(index);
                } else {
                    return Optional.empty();
                }
            }
            return Optional.ofNullable(current);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Chuyển JSON primitive dạng số thành {@link BigDecimal}. */
    private Optional<BigDecimal> actualNumber(JsonElement actual) {
        if (!actual.isJsonPrimitive() || !actual.getAsJsonPrimitive().isNumber()) {
            return Optional.empty();
        }
        return Optional.of(actual.getAsBigDecimal());
    }

    /** Chuyển chuỗi thành {@link BigDecimal}; trả về rỗng nếu định dạng không hợp lệ. */
    private Optional<BigDecimal> parseNumber(String value) {
        try {
            return Optional.of(new BigDecimal(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Chuyển JSON value thành chuỗi dùng cho so sánh và thông báo kết quả. */
    private String asText(JsonElement actual) {
        if (actual == null || actual.isJsonNull()) {
            return "null";
        }
        if (actual.isJsonPrimitive()) {
            JsonPrimitive primitive = actual.getAsJsonPrimitive();
            return primitive.getAsString();
        }
        return actual.toString();
    }

    /** Xác định kiểu JSON của một giá trị, gồm NULL, ARRAY, OBJECT và các primitive. */
    private ApiPayloadAssertion.JsonType jsonType(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return ApiPayloadAssertion.JsonType.NULL;
        }
        if (value.isJsonArray()) {
            return ApiPayloadAssertion.JsonType.ARRAY;
        }
        if (value.isJsonObject()) {
            return ApiPayloadAssertion.JsonType.OBJECT;
        }
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return ApiPayloadAssertion.JsonType.BOOLEAN;
        }
        if (primitive.isNumber()) {
            return ApiPayloadAssertion.JsonType.NUMBER;
        }
        return ApiPayloadAssertion.JsonType.STRING;
    }

    /**
     * Kết quả tổng hợp của toàn bộ quá trình đánh giá payload.
     *
     * @param passed   {@code true} khi tất cả điều kiện đều đạt
     * @param messages thông báo chi tiết của từng phép kiểm tra
     */
    public record Evaluation(boolean passed, List<String> messages) {
        /** Tạo kết quả đánh giá thành công. */
        static Evaluation passed(List<String> messages) {
            return new Evaluation(true, messages);
        }

        /** Tạo kết quả đánh giá thất bại. */
        static Evaluation failed(List<String> messages) {
            return new Evaluation(false, messages);
        }
    }

    /** Kết quả nội bộ của một assertion đơn lẻ. */
    private record AssertionCheck(boolean passed, String message) {
        /** Tạo kết quả assertion thành công. */
        static AssertionCheck passed(String message) {
            return new AssertionCheck(true, message);
        }

        /** Tạo kết quả assertion thất bại. */
        static AssertionCheck failed(String message) {
            return new AssertionCheck(false, message);
        }
    }
}
