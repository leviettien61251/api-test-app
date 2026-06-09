package com.example.apitestapp.models.dto;

public class ApiResponseVariable {
    private final String name;
    private final String jsonPath;

    public ApiResponseVariable(String name, String jsonPath) {
        this.name = name;
        this.jsonPath = jsonPath;
    }

    public String getName() {
        return name;
    }

    public String getJsonPath() {
        return jsonPath;
    }
}
