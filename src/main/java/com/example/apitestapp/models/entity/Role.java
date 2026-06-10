package com.example.apitestapp.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    private Integer id;
    private String name;           // admin, tester, viewer
    private String description;
    private Date createdAt;
}
