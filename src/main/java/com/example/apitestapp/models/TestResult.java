package com.example.apitestapp.models;

import java.time.Instant;

public class TestResult {
    private String caseName;
    private String status; // PASSED | FAILED
    private String message;
    private int expectedCode;
    private int actualCode;
    private long responseTimeMs;
    private Instant executedAt;

    public TestResult() {}

    public String getCaseName() { return caseName; }
    public void setCaseName(String caseName) { this.caseName = caseName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getExpectedCode() { return expectedCode; }
    public void setExpectedCode(int expectedCode) { this.expectedCode = expectedCode; }
    public int getActualCode() { return actualCode; }
    public void setActualCode(int actualCode) { this.actualCode = actualCode; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}
