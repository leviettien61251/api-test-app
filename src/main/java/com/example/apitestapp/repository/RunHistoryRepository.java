package com.example.apitestapp.repository;

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
import java.util.List;

public class RunHistoryRepository {

    private final Path storageFile;
    private final Gson gson;

    public RunHistoryRepository() {
        this.storageFile = resolveStorageFile();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }

    public List<TestRun> loadRuns() {
        try {
            Files.createDirectories(storageFile.getParent());
            if (!Files.exists(storageFile)) {
                saveRuns(List.of());
                return new ArrayList<>();
            }

            String raw = Files.readString(storageFile, StandardCharsets.UTF_8).trim();
            if (raw.isEmpty() || raw.equals("[") || raw.equals("]")) {
                saveRuns(List.of());
                return new ArrayList<>();
            }

            Type type = new TypeToken<List<TestRun>>() {
            }.getType();
            List<TestRun> loaded = gson.fromJson(raw, type);
            return loaded == null ? new ArrayList<>() : new ArrayList<>(loaded);
        } catch (Exception e) {
            System.err.println("[RunHistoryRepository] Không đọc được file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveRuns(List<TestRun> runs) {
        try {
            Files.createDirectories(storageFile.getParent());
            String json = gson.toJson(runs == null ? List.of() : runs);
            Files.writeString(storageFile, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (Exception e) {
            System.err.println("[RunHistoryRepository] Không ghi được file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Path getStorageFile() {
        return storageFile.toAbsolutePath();
    }

    /** Windows: %LOCALAPPDATA%\api-test-app\runs.json - không bị OneDrive khóa file. */
    private static Path resolveStorageFile() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = localAppData != null && !localAppData.isBlank()
                ? Path.of(localAppData, "api-test-app")
                : Path.of(System.getProperty("user.home"), ".api-test-app");
        return base.resolve("runs.json");
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
