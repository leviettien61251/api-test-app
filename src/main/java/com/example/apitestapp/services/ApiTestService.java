package com.example.apitestapp.services;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiTestService {

    private final OkHttpClient client;
    private String baseUrl = "http://localhost:8080";

    public ApiTestService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public ApiTestService(String baseUrl) {
        this();
        this.baseUrl = baseUrl;
    }

    public ApiResponse callSignupApi(String phone, String password) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("phoneNumber", phone);
        requestBody.addProperty("password", password);
        String jsonString = requestBody.toString();
        return callApi("/api/v1/signup", jsonString);
    }

    public ApiResponse callApi(String endpointOrUrl, String jsonBody) {
        try {
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                return new ApiResponse(0, false, "", "Error: Request body is empty");
            }

            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(resolveUrl(endpointOrUrl))
                    .post(body)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                return new ApiResponse(
                        response.code(),
                        response.isSuccessful(),
                        responseBody,
                        response.message()
                );
            }
        } catch (IOException e) {
            return new ApiResponse(0, false, "", "Error: " + e.getMessage());
        }
    }

    private String resolveUrl(String endpointOrUrl) {
        if (endpointOrUrl == null || endpointOrUrl.isBlank()) {
            return baseUrl;
        }
        if (endpointOrUrl.startsWith("http://") || endpointOrUrl.startsWith("https://")) {
            return endpointOrUrl;
        }
        if (endpointOrUrl.startsWith("/")) {
            return baseUrl + endpointOrUrl;
        }
        return baseUrl + "/" + endpointOrUrl;
    }
}
