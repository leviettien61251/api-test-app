package com.example.apitestapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String id;
    private Integer roleId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;

    @Builder.Default
    private Boolean isActive = true;

    private Date createdAt;
    private Date updatedAt;
}
