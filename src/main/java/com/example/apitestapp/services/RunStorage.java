package com.example.apitestapp.services;

import com.example.apitestapp.models.TestRun;
import com.example.apitestapp.repository.RunHistoryRepository;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lưu lịch sử chạy test: bộ nhớ + file JSON (tránh lỗi OneDrive trên thư mục project).
 */
public class RunStorage {

    private static RunStorage instance;

    private final RunHistoryRepository repository;
    private final List<TestRun> runs = new ArrayList<>();

    private RunStorage() {
        this.repository = new RunHistoryRepository();
        runs.addAll(repository.loadRuns());
        System.out.println("[RunStorage] Đã nạp " + runs.size() + " lần chạy từ: " + repository.getStorageFile());
    }

    public static synchronized RunStorage getInstance() {
        if (instance == null) {
            instance = new RunStorage();
        }
        return instance;
    }

    private synchronized void flushToDisk() {
        repository.saveRuns(runs);
    }

    public synchronized List<TestRun> getAllRuns() {
        return new ArrayList<>(runs);
    }

    public synchronized Optional<TestRun> getById(String runId) {
        if (runId == null || runId.isBlank()) {
            return Optional.empty();
        }
        return runs.stream().filter(r -> runId.equals(r.getId())).findFirst();
    }

    public synchronized String saveCompleteRun(TestRun run) {
        if (run.getId() == null || run.getId().isBlank()) {
            run.setId(UUID.randomUUID().toString());
        }
        if (run.getStartedAt() == null) {
            run.setStartedAt(Instant.now());
        }
        if (run.getCompletedAt() == null) {
            run.setCompletedAt(Instant.now());
        }
        if (run.getResults() == null) {
            run.setResults(Collections.emptyList());
        }

        runs.removeIf(r -> run.getId().equals(r.getId()));
        runs.add(0, run);
        flushToDisk();
        return run.getId();
    }

    public synchronized boolean deleteRun(String runId) {
        boolean removed = runs.removeIf(r -> runId.equals(r.getId()));
        if (removed) {
            flushToDisk();
        }
        return removed;
    }

    public Path getStorageFile() {
        return repository.getStorageFile();
    }

    public synchronized int count() {
        return runs.size();
    }

}
