package com.example.apitestapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEndpoint {

    private String id;
    private String testSuitId;
    private String method;         // GET, POST, PUT, PATCH, DELETE
    private String path;           // /patients, /guides/{id}
    private String description;

    @Builder.Default
    private List<String> tags = List.of();   // maps từ JSONB ["patients", "navigation"]

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
    private Date updatedAt;
}
