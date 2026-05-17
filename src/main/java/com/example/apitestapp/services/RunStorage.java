package com.example.apitestapp.services;

import com.example.apitestapp.models.TestRun;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    private final Path storageFile;
    private final Gson gson;
    private final List<TestRun> runs = new ArrayList<>();

    private RunStorage() {
        this.storageFile = resolveStorageFile();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
        loadFromDisk();
        System.out.println("[RunStorage] Đã nạp " + runs.size() + " lần chạy từ: " + storageFile);
    }

    public static synchronized RunStorage getInstance() {
        if (instance == null) {
            instance = new RunStorage();
        }
        return instance;
    }

    /** Windows: %LOCALAPPDATA%\api-test-app\runs.json — không bị OneDrive khóa file. */
    private static Path resolveStorageFile() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = localAppData != null && !localAppData.isBlank()
                ? Path.of(localAppData, "api-test-app")
                : Path.of(System.getProperty("user.home"), ".api-test-app");
        return base.resolve("runs.json");
    }

    private void loadFromDisk() {
        runs.clear();
        try {
            Files.createDirectories(storageFile.getParent());
            if (!Files.exists(storageFile)) {
                flushToDisk();
                return;
            }
            String raw = Files.readString(storageFile, StandardCharsets.UTF_8).trim();
            if (raw.isEmpty() || raw.equals("[") || raw.equals("]")) {
                flushToDisk();
                return;
            }
            Type type = new TypeToken<List<TestRun>>() {
            }.getType();
            List<TestRun> loaded = gson.fromJson(raw, type);
            if (loaded != null) {
                runs.addAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("[RunStorage] Không đọc được file: " + e.getMessage());
        }
    }

    private synchronized void flushToDisk() {
        try {
            Files.createDirectories(storageFile.getParent());
            String json = gson.toJson(runs);
            Files.writeString(storageFile, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (Exception e) {
            System.err.println("[RunStorage] Không ghi được file: " + e.getMessage());
            e.printStackTrace();
        }
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
        return storageFile.toAbsolutePath();
    }

    public synchronized int count() {
        return runs.size();
    }

    private static final class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Instant.parse(json.getAsString());
        }
    }
}
