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
public class TestSuit {

    private String id;
    private Integer collectionId;
    private Integer folderId;      // null nếu không thuộc folder nào

    private String name;
    private String description;
    private String baseUrl;        // https://api.benhvien.com/v1

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
    private Date updatedAt;
}
