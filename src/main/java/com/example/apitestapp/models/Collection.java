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
public class Collection {

    private Integer id;
    private String userId;
    private String name;
    private String description;
    private String icon;           // Emoji hoặc icon name

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
    private Date updatedAt;
}
