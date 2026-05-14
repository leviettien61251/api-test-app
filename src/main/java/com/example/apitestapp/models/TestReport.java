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
public class TestReport {

    private String id;
    private String testRunId;
    @Builder.Default
    private ReportType reportType = ReportType.JSON;
    private Map<String, Object> reportData;   // JSONB
    private String summary;
    private String filePath;
    private Integer fileSizeBytes;
    private Date createdAt;

    public enum ReportType {
        JSON, HTML, PDF
    }
}
