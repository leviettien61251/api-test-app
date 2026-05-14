package com.example.apitestapp.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupTestData {
    private String scenario;
    private String phoneNumber;
    private String password;
    private String expectedCode;
    private String expectedStatus;
    private String description;
}
