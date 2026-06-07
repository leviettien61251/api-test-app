package com.example.apitestapp.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;


public final class AppRunConfig {
    public static final String DEFAULT_BASE_URL ="http://group3.it4788.sukkaito.id.vn/api";
    public static final String DEFAULT_RUN_MODE = "ALL";
    public static final String DEFAULT_ALERT_MODE = "Stop on fail";
    public static final String DEFAULT_USER = System.getProperty("user.name", "Unknown");
    public static final String DEFAULT_OS = System.getProperty("os.name", "Unknown OS");

    private static String baseUrl = "";
    // ĐÃ XÓA: runMode
    private static String alertMode = DEFAULT_ALERT_MODE;
    // ĐÃ XÓA: private static String runner = DEFAULT_USER;
    private static LocalDateTime configuredAt;
    private static boolean configured;

    private AppRunConfig() {
    }

    public static void reset() {
        baseUrl = "";
        // ĐÃ XÓA: runMode = DEFAULT_RUN_MODE;
        alertMode = DEFAULT_ALERT_MODE;
        // ĐÃ XÓA: runner = DEFAULT_USER;
        configured = false;
        configuredAt = null;
    }

    // ĐÃ XÓA tham số selectedRunner
    public static void configure(String selectedBaseUrl, String selectedAlertMode) {
        baseUrl = normalizeBaseUrl(selectedBaseUrl);
        // ĐÃ XÓA gán runMode
        alertMode = selectedAlertMode;
        // ĐÃ XÓA: runner = selectedRunner == null || selectedRunner.isBlank() ? DEFAULT_USER : selectedRunner.trim();
        configuredAt = LocalDateTime.now();
        configured = true;
    }

    public static boolean isConfigured() {
        return configured;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    // ĐÃ XÓA: hàm getRunMode()

    public static String getAlertMode() {
        return alertMode;
    }

    // ĐÃ XÓA: hàm getRunner()
    // Thay thế bằng phương thức trả về DEFAULT_USER hoặc AppSession username
    public static String getRunner() {
        // Sử dụng username từ AppSession nếu có, ngược lại dùng DEFAULT_USER
        try {
            String username = AppSession.getUsername();
            if (username != null && !username.isBlank()) {
                return username;
            }
        } catch (Exception e) {
            // Ignore
        }
        return DEFAULT_USER;
    }

    public static String getOs() {
        return DEFAULT_OS;
    }

    public static LocalDateTime getConfiguredAt() {
        return configuredAt;
    }

    public static String getMachineName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown machine";
        }
    }

    public static String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}