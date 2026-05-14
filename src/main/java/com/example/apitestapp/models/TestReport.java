package com.example.apitestapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestReport {

    public enum ReportType {
        JSON, HTML, PDF
    }

    private String id;
    private String testRunId;

    @Builder.Default
    private ReportType reportType = ReportType.JSON;

    private Map<String, Object> reportData;   // JSONB
    private String summary;
    private String filePath;
    private Integer fileSizeBytes;

    private Date createdAt;
}
