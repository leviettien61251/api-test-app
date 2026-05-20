package com.example.apitestapp.services;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
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

    public ApiResponse callApi(String endpointOrUrl, String jsonBody) {
        return callApi("POST", endpointOrUrl, jsonBody);
    }

    public ApiResponse callApi(String method, String endpointOrUrl, String jsonBody) {
        return callApi(method, endpointOrUrl, jsonBody, Map.of());
    }

    public ApiResponse callApi(String method, String endpointOrUrl, String jsonBody, Map<String, String> queryParams) {
        return callApi(method, endpointOrUrl, jsonBody, queryParams, Map.of());
    }

    public ApiResponse callApi(String method,
                               String endpointOrUrl,
                               String jsonBody,
                               Map<String, String> queryParams,
                               Map<String, String> headers) {
        try {
            String normalizedMethod = method == null || method.isBlank() ? "POST" : method.trim().toUpperCase();
            boolean allowsEmptyBody = "GET".equals(normalizedMethod) || "DELETE".equals(normalizedMethod);
            if (!allowsEmptyBody && (jsonBody == null || jsonBody.trim().isEmpty())) {
                return new ApiResponse(0, false, "", "Error: Request body is empty");
            }

            RequestBody body = null;
            if (jsonBody != null && !jsonBody.trim().isEmpty()) {
                body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            }

            Request.Builder requestBuilder = new Request.Builder()
                    .url(resolveUrl(endpointOrUrl, queryParams))
                    .header("Content-Type", "application/json; charset=utf-8");

            if (headers != null) {
                headers.forEach((key, value) -> {
                    if (key != null && !key.isBlank()) {
                        requestBuilder.header(key, value == null ? "" : value);
                    }
                });
            }

            if ("GET".equals(normalizedMethod)) {
                requestBuilder.get();
            } else if ("DELETE".equals(normalizedMethod)) {
                if (body == null) {
                    requestBuilder.delete();
                } else {
                    requestBuilder.delete(body);
                }
            } else {
                requestBuilder.method(normalizedMethod, body);
            }

            Request request = requestBuilder.build();

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
        return resolveUrl(endpointOrUrl, Map.of());
    }

    private String resolveUrl(String endpointOrUrl, Map<String, String> queryParams) {
        String resolvedUrl;
        if (endpointOrUrl == null || endpointOrUrl.isBlank()) {
            resolvedUrl = baseUrl;
        } else if (endpointOrUrl.startsWith("http://") || endpointOrUrl.startsWith("https://")) {
            resolvedUrl = endpointOrUrl;
        } else if (endpointOrUrl.startsWith("/")) {
            resolvedUrl = baseUrl + endpointOrUrl;
        } else {
            resolvedUrl = baseUrl + "/" + endpointOrUrl;
        }
        return appendQueryParams(resolvedUrl, queryParams);
    }

    private String appendQueryParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        HttpUrl parsedUrl = HttpUrl.parse(url);
        if (parsedUrl == null) {
            return url;
        }

        HttpUrl.Builder builder = parsedUrl.newBuilder();
        queryParams.forEach((key, value) -> {
            if (key != null && !key.isBlank()) {
                builder.addQueryParameter(key, value == null ? "" : value);
            }
        });
        return builder.build().toString();
    }
}
