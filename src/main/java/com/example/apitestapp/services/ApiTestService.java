package com.example.apitestapp.services;

import com.example.apitestapp.models.dto.ApiResponse;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiTestService {

    private final OkHttpClient client;
    private String baseUrl = "http://localhost:8080";

    /**
     * Khởi tạo service với URL cơ sở mặc định và cấu hình timeout 10 giây
     * cho các thao tác kết nối, đọc và ghi.
     */
    public ApiTestService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Khởi tạo service với URL cơ sở được chỉ định.
     *
     * @param baseUrl URL cơ sở dùng để ghép với các endpoint tương đối
     */
    public ApiTestService(String baseUrl) {
        this();
        this.baseUrl = baseUrl;
    }

    /**
     * Gửi yêu cầu POST với nội dung JSON tới endpoint hoặc URL được chỉ định.
     *
     * @param endpointOrUrl endpoint tương đối hoặc URL đầy đủ
     * @param jsonBody      nội dung request ở định dạng JSON
     * @return kết quả phản hồi từ API
     */
    public ApiResponse callApi(String endpointOrUrl, String jsonBody) {
        return callApi("POST", endpointOrUrl, jsonBody);
    }

    /**
     * Gửi yêu cầu HTTP với phương thức và nội dung JSON được chỉ định.
     *
     * @param method        phương thức HTTP; mặc định là POST nếu để trống
     * @param endpointOrUrl endpoint tương đối hoặc URL đầy đủ
     * @param jsonBody      nội dung request ở định dạng JSON
     * @return kết quả phản hồi từ API
     */
    public ApiResponse callApi(String method, String endpointOrUrl, String jsonBody) {
        return callApi(method, endpointOrUrl, jsonBody, Map.of());
    }

    /**
     * Gửi yêu cầu HTTP kèm các tham số truy vấn.
     *
     * @param method        phương thức HTTP; mặc định là POST nếu để trống
     * @param endpointOrUrl endpoint tương đối hoặc URL đầy đủ
     * @param jsonBody      nội dung request ở định dạng JSON
     * @param queryParams   các tham số truy vấn cần thêm vào URL
     * @return kết quả phản hồi từ API
     */
    public ApiResponse callApi(String method, String endpointOrUrl, String jsonBody, Map<String, ?> queryParams) {
        return callApi(method, endpointOrUrl, jsonBody, queryParams, Map.of());
    }

    /**
     * Gửi yêu cầu HTTP kèm nội dung JSON, tham số truy vấn và header tùy chỉnh.
     * GET và DELETE được phép không có body; các phương thức khác yêu cầu body hợp lệ.
     *
     * @param method        phương thức HTTP; mặc định là POST nếu để trống
     * @param endpointOrUrl endpoint tương đối hoặc URL đầy đủ
     * @param jsonBody      nội dung request ở định dạng JSON
     * @param queryParams   các tham số truy vấn cần thêm vào URL
     * @param headers       các header tùy chỉnh; ghi đè header mặc định nếu trùng tên
     * @return kết quả phản hồi từ API, hoặc phản hồi lỗi nếu request không hợp lệ hay xảy ra lỗi I/O
     */
    public ApiResponse callApi(String method,
                               String endpointOrUrl,
                               String jsonBody,
                               Map<String, ?> queryParams,
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

    /**
     * Chuyển endpoint tương đối hoặc URL đầy đủ thành URL có thể sử dụng để gửi request.
     *
     * @param endpointOrUrl endpoint tương đối hoặc URL đầy đủ
     * @return URL đã được chuẩn hóa
     */
    private String resolveUrl(String endpointOrUrl) {
        return resolveUrl(endpointOrUrl, Map.of());
    }

    /**
     * Chuẩn hóa URL và thêm các tham số truy vấn được chỉ định.
     *
     * @param endpointOrUrl endpoint tương đối hoặc URL đầy đủ
     * @param queryParams   các tham số truy vấn cần thêm vào URL
     * @return URL đã được chuẩn hóa và bổ sung tham số truy vấn
     */
    private String resolveUrl(String endpointOrUrl, Map<String, ?> queryParams) {
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

    /**
     * Thêm các tham số truy vấn vào URL. Giá trị dạng Iterable được thêm thành
     * nhiều tham số có cùng tên.
     *
     * @param url         URL cần bổ sung tham số truy vấn
     * @param queryParams các tham số truy vấn cần thêm
     * @return URL sau khi thêm tham số, hoặc URL ban đầu nếu không thể phân tích
     */
    private String appendQueryParams(String url, Map<String, ?> queryParams) {
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
                if (value instanceof Iterable<?> values) {
                    values.forEach(item -> builder.addQueryParameter(key, item == null ? "" : item.toString()));
                } else {
                    builder.addQueryParameter(key, value == null ? "" : value.toString());
                }
            }
        });
        return builder.build().toString();
    }
}
