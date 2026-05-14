package com.example.apitestapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {

    public enum Status {
        PENDING, RUNNING, PASSED, FAILED, SKIPPED, ERROR
    }

    public enum ErrorType {
        NETWORK_ERROR, TIMEOUT, ASSERTION_FAILED
    }

    private String id;
    private String testRunId;
    private String testCaseId;

    // Kết quả
    @Builder.Default
    private Status status = Status.PENDING;

    private Integer responseStatusCode;
    private Map<String, Object> responseHeaders;   // JSONB
    private Map<String, Object> responseBody;      // JSONB
    private Integer responseSizeBytes;
    private Integer responseTimeMs;
    private Integer totalDurationMs;

    // Chi tiết lỗi
    private ErrorType errorType;
    private String errorMessage;
    private String errorStack;

    // Retry
    @Builder.Default
    private Integer retryAttempts = 0;

    private Date executedAt;
    private Date completedAt;
}
