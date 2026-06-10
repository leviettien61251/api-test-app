package com.example.apitestapp.config;

/**
 * Giữ id của lần chạy đang được xem trên màn Report (chọn từ History hoặc Dashboard).
 */
public final class SelectedRunContext {

    private static String selectedRunId;

    private SelectedRunContext() {
    }

    public static String getSelectedRunId() {
        return selectedRunId;
    }

    public static void setSelectedRunId(String runId) {
        selectedRunId = runId;
    }

    public static void clear() {
        selectedRunId = null;
    }
}
