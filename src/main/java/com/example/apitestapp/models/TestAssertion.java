package com.example.apitestapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAssertion {

    private String id;
    private String testResultId;
    // Loại assertion
    private AssertionType assertionType;
    // Đối tượng kiểm tra
    private String targetPath;         // JSON path: $.data.patients[0].name
    private String targetDescription;
    // Giá trị
    private String expectedValue;
    private String actualValue;
    @Builder.Default
    private ComparisonOperator comparisonOperator = ComparisonOperator.EQUALS;
    // Kết quả
    private Boolean passed;
    private String message;
    private Date executedAt;
    public enum AssertionType {
        STATUS_CODE, RESPONSE_SCHEMA, RESPONSE_BODY, RESPONSE_HEADER, RESPONSE_TIME
    }

    public enum ComparisonOperator {
        EQUALS, CONTAINS, REGEX, GREATER_THAN, LESS_THAN, EXISTS, NOT_NULL
    }
}
