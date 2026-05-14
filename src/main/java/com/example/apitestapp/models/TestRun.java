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
public class TestRun {

    public enum RunMode {
        SUITE, SINGLE_CASE
    }

    public enum FailureStrategy {
        STOP_ON_FAIL, CONTINUE
    }

    public enum OverallStatus {
        RUNNING, PASSED, FAILED, PARTIALLY_PASSED, STOPPED, ERROR
    }

    private String id;
    private String userId;
    private String clientMachineId;
    private Date runTimestamp;
    private String runName;

    // Chế độ chạy
    @Builder.Default
    private RunMode runMode = RunMode.SUITE;

    private String testSuitId;        // nếu runMode = SUITE
    private String testCaseId;        // nếu runMode = SINGLE_CASE

    // Chế độ cảnh báo
    @Builder.Default
    private FailureStrategy failureStrategy = FailureStrategy.CONTINUE;

    // Kết quả tổng thể
    @Builder.Default
    private OverallStatus overallStatus = OverallStatus.RUNNING;

    @Builder.Default
    private Integer totalCases = 0;

    @Builder.Default
    private Integer passedCases = 0;

    @Builder.Default
    private Integer failedCases = 0;

    @Builder.Default
    private Integer skippedCases = 0;

    @Builder.Default
    private Integer errorCases = 0;

    private Integer totalDurationMs;
    private String notes;
    private Date createdAt;
    private Date completedAt;
}
