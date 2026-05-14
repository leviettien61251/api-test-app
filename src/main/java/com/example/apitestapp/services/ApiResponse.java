package com.example.apitestapp.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ApiResponse {
    private final int httpCode;
    private final boolean success;
    private final String responseBody;
    private final String statusMessage;

    public ApiResponse(int httpCode, boolean success, String responseBody, String statusMessage) {
        this.httpCode = httpCode;
        this.success = success;
        this.responseBody = responseBody;
        this.statusMessage = statusMessage;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public boolean isSuccess() {
        return success;
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
        } catch (Exception ignored) {
            // Fallback to HTTP status code when response body is not the expected JSON shape.
        }
        return String.valueOf(httpCode);
    }
}
