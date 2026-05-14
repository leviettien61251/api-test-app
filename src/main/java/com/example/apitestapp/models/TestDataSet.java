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
public class TestDataSet {

    private Integer id;
    private String testCaseId;
    private String name;
    private Map<String, Object> data;   // JSONB
    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
}
