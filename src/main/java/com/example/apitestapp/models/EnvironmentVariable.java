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
public class EnvironmentVariable {

    private Integer id;
    private Integer collectionId;
    private String key;
    private String value;

    @Builder.Default
    private Boolean isSecret = false;   // true nếu là token, password...

    private String description;
    private Date createdAt;
    private Date updatedAt;
}
