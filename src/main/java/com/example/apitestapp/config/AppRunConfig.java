package com.example.apitestapp.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

public final class AppRunConfig {
    public static final String DEFAULT_BASE_URL = "http://localhost:8080";
    public static final String DEFAULT_RUN_MODE = "ALL";
    public static final String DEFAULT_ALERT_MODE = "Stop on fail";
    public static final String DEFAULT_USER = System.getProperty("user.name", "Unknown");
    public static final String DEFAULT_OS = System.getProperty("os.name", "Unknown OS");

    private static String baseUrl = "";
    private static String runMode = DEFAULT_RUN_MODE;
    private static String alertMode = DEFAULT_ALERT_MODE;
    private static String runner = DEFAULT_USER;
    private static LocalDateTime configuredAt;
    private static boolean configured;

    private AppRunConfig() {
    }


    public static void reset() {
        baseUrl = "";
        runMode = DEFAULT_RUN_MODE;
        alertMode = DEFAULT_ALERT_MODE;
        runner = DEFAULT_USER;
        configured = false;
        configuredAt = null;
    }
    public static void configure(String selectedBaseUrl, String selectedRunMode, String selectedAlertMode, String selectedRunner) {
        baseUrl = normalizeBaseUrl(selectedBaseUrl);
        runMode = selectedRunMode;
        alertMode = selectedAlertMode;
        runner = selectedRunner == null || selectedRunner.isBlank() ? DEFAULT_USER : selectedRunner.trim();
        configuredAt = LocalDateTime.now();
        configured = true;
    }

    public static boolean isConfigured() {
        return configured;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getRunMode() {
        return runMode;
    }

    public static String getAlertMode() {
        return alertMode;
    }

    public static String getRunner() {
        return runner;
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
