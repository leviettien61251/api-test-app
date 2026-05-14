package com.example.apitestapp.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        requestBody.addProperty("phone", phone);
        requestBody.addProperty("password", password);
        String jsonString = requestBody.toString();
        return callApi("/api/v1/signup", jsonString);
    }

    public ApiResponse callApi(String endpoint, String jsonBody) {
        try {
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                return ApiResponse.builder()
                        .httpCode(0)
                        .isSuccess(false)
                        .responseBody("")
                        .statusMessage("Error: Request body is empty")
                        .build();
            }

            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(baseUrl + endpoint)
                    .post(body)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                return ApiResponse.builder()
                        .httpCode(response.code())
                        .isSuccess(response.isSuccessful())
                        .responseBody(responseBody)
                        .statusMessage(response.message())
                        .build();
            }
        } catch (IOException e) {
            return ApiResponse.builder()
                    .httpCode(0)
                    .isSuccess(false)
                    .responseBody("")
                    .statusMessage("Error: " + e.getMessage())
                    .build();
        }
    }

    public static class ApiResponse {
        private final int httpCode;
        private final boolean isSuccess;
        private final String responseBody;
        private final String statusMessage;

        private ApiResponse(int httpCode, boolean isSuccess, String responseBody, String statusMessage) {
            this.httpCode = httpCode;
            this.isSuccess = isSuccess;
            this.responseBody = responseBody;
            this.statusMessage = statusMessage;
        }

        public static Builder builder() {
            return new Builder();
        }

        public int getHttpCode() {
            return httpCode;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public String getResponseCode() {
            try {
                JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                if (json.has("code")) {
                    return String.valueOf(json.get("code").getAsInt());
                }
            } catch (Exception e) {
                // Fallback to HTTP code
            }
            return String.valueOf(httpCode);
        }

        public static class Builder {
            private int httpCode;
            private boolean isSuccess;
            private String responseBody;
            private String statusMessage;

            public Builder httpCode(int httpCode) {
                this.httpCode = httpCode;
                return this;
            }

            public Builder isSuccess(boolean isSuccess) {
                this.isSuccess = isSuccess;
                return this;
            }

            public Builder responseBody(String responseBody) {
                this.responseBody = responseBody;
                return this;
            }

            public Builder statusMessage(String statusMessage) {
                this.statusMessage = statusMessage;
                return this;
            }

            public ApiResponse build() {
                return new ApiResponse(httpCode, isSuccess, responseBody, statusMessage);
            }
        }
    }
}
