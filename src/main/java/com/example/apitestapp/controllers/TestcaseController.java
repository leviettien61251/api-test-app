package com.example.apitestapp.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class TestcaseController implements Initializable {

    private static final String DEFAULT_ENDPOINT = "http://localhost:8080/api/v1";
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    @FXML private TextField endpointField;
    @FXML private TextField timeoutField;
    @FXML private TextField phoneKeyField;
    @FXML private TextField passwordKeyField;
    @FXML private TextArea paramsArea;
    @FXML private TextArea bodyArea;
    @FXML private ComboBox<String> contentTypeCombo;
    @FXML private ComboBox<String> testcasePicker;

    @FXML private TableView<TestResultRow> resultsTable;
    @FXML private TableColumn<TestResultRow, String> idColumn;
    @FXML private TableColumn<TestResultRow, String> caseColumn;
    @FXML private TableColumn<TestResultRow, String> phoneColumn;
    @FXML private TableColumn<TestResultRow, String> expectedColumn;
    @FXML private TableColumn<TestResultRow, String> httpColumn;
    @FXML private TableColumn<TestResultRow, String> verdictColumn;
    @FXML private TableColumn<TestResultRow, String> messageColumn;

    @FXML private Label summaryLabel;
    @FXML private TextArea logArea;

    private final ObservableList<TestResultRow> resultRows = FXCollections.observableArrayList();
    private final List<TestCaseDefinition> testDefinitions = new ArrayList<>();
    private final Random random = new Random();

    private volatile boolean running = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable();
        configureInputs();
        buildTestDefinitions();
    }

    @FXML
    private void handleRunSelected() {
        if (running) {
            return;
        }
        String selectedName = testcasePicker.getValue();
        if (selectedName == null || selectedName.isBlank()) {
            appendLog("Chua chon testcase de chay.");
            return;
        }

        TestCaseDefinition selected = testDefinitions.stream()
                .filter(t -> t.name.equals(selectedName))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            appendLog("Khong tim thay testcase da chon.");
            return;
        }

        runCases(List.of(selected), false, captureRunConfig());
    }

    @FXML
    private void handleRunAll() {
        if (running) {
            return;
        }
        runCases(new ArrayList<>(testDefinitions), true, captureRunConfig());
    }

    @FXML
    private void handleClearResults() {
        if (running) {
            return;
        }
        resultRows.clear();
        logArea.clear();
        summaryLabel.setText("Cleared. Ready.");
    }

    private void configureTable() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().id));
        caseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().testcase));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().phone));
        expectedColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().expected));
        httpColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().httpCode));
        verdictColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().result));
        messageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().message));
        resultsTable.setItems(resultRows);
    }

    private void configureInputs() {
        endpointField.setText(DEFAULT_ENDPOINT);
        timeoutField.setText("15");
        contentTypeCombo.getItems().addAll("application/json", "application/x-www-form-urlencoded");
        contentTypeCombo.setValue("application/json");
    }

    private void buildTestDefinitions() {
        testDefinitions.clear();

        testDefinitions.add(new TestCaseDefinition(
                "TC01",
                "Scenario 1: Valid data, not yet registered -> SUCCESS",
                this::newValidSignupInput,
                ExpectType.SUCCESS,
                false
        ));

        testDefinitions.add(new TestCaseDefinition(
                "TC02",
                "Scenario 2: Valid data, already registered -> FAILURE",
                this::newValidSignupInput,
                ExpectType.FAILURE,
                true
        ));

        testDefinitions.add(new TestCaseDefinition(
                "TC03",
                "Scenario 3: Valid phone, no password -> FAILURE",
                () -> new TestInput(newValidViettelPhone(), null),
                ExpectType.FAILURE,
                false
        ));

        testDefinitions.add(new TestCaseDefinition(
                "TC04",
                "Scenario 4: Invalid phone, has password -> FAILURE",
                () -> new TestInput("0912345678", "Abc@12345"),
                ExpectType.FAILURE,
                false
        ));

        testDefinitions.add(new TestCaseDefinition(
                "TC05",
                "Scenario 5: Invalid phone, has password, already registered -> FAILURE",
                () -> new TestInput("0911111111", "Abc@12345"),
                ExpectType.FAILURE,
                true
        ));

        testDefinitions.add(new TestCaseDefinition(
                "TC06",
                "Additional: Different valid Viettel phone prefixes -> SUCCESS",
                this::newValidPrefixInput,
                ExpectType.SUCCESS,
                false
        ));

        testDefinitions.add(new TestCaseDefinition(
                "TC07",
                "Additional: Invalid password format (too short) -> FAILURE",
                () -> new TestInput(newValidViettelPhone(), "Ab1!"),
                ExpectType.FAILURE,
                false
        ));

        testDefinitions.add(new TestCaseDefinition(
                "TC08",
                "Additional: Valid password with special characters -> SUCCESS",
                () -> new TestInput(newValidViettelPhone(), "P@ss#2026!"),
                ExpectType.SUCCESS,
                false
        ));

        testcasePicker.setItems(FXCollections.observableArrayList(
                testDefinitions.stream().map(t -> t.name).toList()
        ));
        testcasePicker.getSelectionModel().selectFirst();
    }

    private void runCases(List<TestCaseDefinition> cases, boolean clearBeforeRun, RunConfig config) {
        running = true;
        if (clearBeforeRun) {
            resultRows.clear();
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int passedCount = 0;
                int total = cases.size();

                appendLog("Start run " + total + " testcase(s).");

                for (TestCaseDefinition def : cases) {
                    appendLog("Running " + def.id + " - " + def.name);

                    TestResultRow row = executeTestCase(def, config);
                    if ("PASS".equals(row.result)) {
                        passedCount++;
                    }

                    int finalPassed = passedCount;
                    Platform.runLater(() -> {
                        resultRows.add(row);
                        summaryLabel.setText("Passed: " + finalPassed + " / " + resultRows.size());
                    });
                }

                appendLog("Done. Passed " + passedCount + " / " + total);
                return null;
            }
        };

        task.setOnSucceeded(e -> running = false);
        task.setOnFailed(e -> {
            running = false;
            Throwable ex = task.getException();
            appendLog("Run failed: " + (ex == null ? "Unknown error" : ex.getMessage()));
        });

        Thread thread = new Thread(task, "signup-test-runner");
        thread.setDaemon(true);
        thread.start();
    }

    private RunConfig captureRunConfig() {
        String endpoint = endpointField.getText() == null || endpointField.getText().isBlank()
                ? DEFAULT_ENDPOINT
                : endpointField.getText().trim();
        String phoneKey = safeKey(phoneKeyField.getText(), "phonenumber");
        String passwordKey = safeKey(passwordKeyField.getText(), "password");
        String contentType = contentTypeCombo.getValue() == null ? "application/json" : contentTypeCombo.getValue();
        int timeoutSeconds = parseTimeout(timeoutField.getText());
        String rawParams = paramsArea.getText();
        String rawBody = bodyArea.getText();
        return new RunConfig(endpoint, phoneKey, passwordKey, contentType, timeoutSeconds, rawParams, rawBody);
    }

    private TestResultRow executeTestCase(TestCaseDefinition def, RunConfig config) {
        TestInput input = def.inputSupplier.get();

        if (def.preRegister && input.password != null && !input.password.isBlank()) {
            sendSignup(input.phone, input.password, config);
        }

        HttpResult result = sendSignup(input.phone, input.password, config);

        boolean pass = def.expectType == ExpectType.SUCCESS
                ? (result.code >= 200 && result.code < 300)
                : (result.code < 200 || result.code >= 300);

        String expected = def.expectType == ExpectType.SUCCESS ? "SUCCESS" : "FAILURE";
        String msg = result.message;
        if (msg.length() > 180) {
            msg = msg.substring(0, 180) + "...";
        }

        return new TestResultRow(
                def.id,
                def.name,
                input.phone,
                expected,
                String.valueOf(result.code),
                pass ? "PASS" : "FAIL",
                msg
        );
    }

    private HttpResult sendSignup(String phone, String password, RunConfig config) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
                .readTimeout(Duration.ofSeconds(config.timeoutSeconds))
                .writeTimeout(Duration.ofSeconds(config.timeoutSeconds))
                .build();

        String payload;
        RequestBody requestBody;
        String endpointWithParams = buildUrlWithParams(config.endpoint, config.rawParams);
        boolean hasCustomBody = config.rawBody != null && !config.rawBody.isBlank();

        if (hasCustomBody) {
            payload = config.rawBody;
            if ("application/x-www-form-urlencoded".equals(config.contentType)) {
                requestBody = RequestBody.create(payload, FORM_MEDIA_TYPE);
            } else {
                requestBody = RequestBody.create(payload, JSON_MEDIA_TYPE);
            }
        } else {
            if ("application/x-www-form-urlencoded".equals(config.contentType)) {
                payload = config.phoneKey + "=" + urlEncode(phone);
                if (password != null) {
                    payload += "&" + config.passwordKey + "=" + urlEncode(password);
                }
                requestBody = RequestBody.create(payload, FORM_MEDIA_TYPE);
            } else {
                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"").append(escapeJson(config.phoneKey)).append("\":\"").append(escapeJson(phone)).append("\"");
                if (password != null) {
                    json.append(",\"").append(escapeJson(config.passwordKey)).append("\":\"").append(escapeJson(password)).append("\"");
                }
                json.append("}");
                payload = json.toString();
                requestBody = RequestBody.create(payload, JSON_MEDIA_TYPE);
            }
        }

        Request request = new Request.Builder()
                .url(endpointWithParams)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            String message = responseBody.isBlank() ? response.message() : responseBody.replaceAll("\\s+", " ").trim();
            appendLog(" -> HTTP " + response.code() + " | url=" + endpointWithParams + " | payload=" + payload);
            return new HttpResult(response.code(), message);
        } catch (IOException e) {
            appendLog(" -> Request error: " + e.getMessage());
            return new HttpResult(0, "Request error: " + e.getMessage());
        }
    }

    private TestInput newValidSignupInput() {
        return new TestInput(newValidViettelPhone(), "Aa@12345");
    }

    private TestInput newValidPrefixInput() {
        String[] prefixes = {"032", "033", "034", "035", "036", "037", "038", "039", "086", "096", "097", "098"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        return new TestInput(prefix + randomDigits(7), "Aa@12345");
    }

    private String newValidViettelPhone() {
        String[] prefixes = {"032", "033", "034", "035", "036", "037", "038", "039", "086", "096", "097", "098"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String seed = String.valueOf(System.currentTimeMillis());
        String tail = seed.substring(Math.max(0, seed.length() - 7));
        if (tail.length() < 7) {
            tail = ("0000000" + tail).substring(tail.length());
        }
        return prefix + tail;
    }

    private String randomDigits(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private int parseTimeout(String raw) {
        try {
            int val = Integer.parseInt(raw == null ? "" : raw.trim());
            if (val >= 3 && val <= 120) {
                return val;
            }
        } catch (NumberFormatException ignored) {
        }
        return 15;
    }

    private String safeKey(String input, String fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }
        return input.trim();
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String urlEncode(String text) {
        return java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String buildUrlWithParams(String endpoint, String rawParams) {
        String query = buildEncodedQuery(rawParams);
        if (query.isBlank()) {
            return endpoint;
        }
        if (endpoint.contains("?")) {
            String separator = endpoint.endsWith("?") || endpoint.endsWith("&") ? "" : "&";
            return endpoint + separator + query;
        }
        return endpoint + "?" + query;
    }

    private String buildEncodedQuery(String rawParams) {
        if (rawParams == null || rawParams.isBlank()) {
            return "";
        }

        String normalized = rawParams.replace("\r", "").trim();
        if (normalized.startsWith("?")) {
            normalized = normalized.substring(1);
        }

        String[] chunks = normalized.replace("\n", "&").split("&");
        StringBuilder query = new StringBuilder();
        for (String chunk : chunks) {
            String token = chunk.trim();
            if (token.isBlank()) {
                continue;
            }
            if (token.startsWith("?")) {
                token = token.substring(1);
            }

            int idx = token.indexOf('=');
            String key;
            String value;
            if (idx < 0) {
                key = token;
                value = "";
            } else {
                key = token.substring(0, idx).trim();
                value = token.substring(idx + 1).trim();
            }
            if (key.isBlank()) {
                continue;
            }

            if (!query.isEmpty()) {
                query.append("&");
            }
            query.append(urlEncode(key)).append("=").append(urlEncode(value));
        }
        return query.toString();
    }

    private void appendLog(String message) {
        String line = "[" + LocalDateTime.now().format(LOG_TIME_FORMAT) + "] " + message;
        Platform.runLater(() -> logArea.appendText(line + "\n"));
    }

    private enum ExpectType {
        SUCCESS,
        FAILURE
    }

    private static class TestCaseDefinition {
        private final String id;
        private final String name;
        private final Supplier<TestInput> inputSupplier;
        private final ExpectType expectType;
        private final boolean preRegister;

        private TestCaseDefinition(String id, String name, Supplier<TestInput> inputSupplier, ExpectType expectType, boolean preRegister) {
            this.id = id;
            this.name = name;
            this.inputSupplier = inputSupplier;
            this.expectType = expectType;
            this.preRegister = preRegister;
        }
    }

    private static class TestInput {
        private final String phone;
        private final String password;

        private TestInput(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }
    }

    private static class HttpResult {
        private final int code;
        private final String message;

        private HttpResult(int code, String message) {
            this.code = code;
            this.message = message == null ? "" : message;
        }
    }

    private static class RunConfig {
        private final String endpoint;
        private final String phoneKey;
        private final String passwordKey;
        private final String contentType;
        private final int timeoutSeconds;
        private final String rawParams;
        private final String rawBody;

        private RunConfig(String endpoint, String phoneKey, String passwordKey, String contentType, int timeoutSeconds, String rawParams, String rawBody) {
            this.endpoint = endpoint;
            this.phoneKey = phoneKey;
            this.passwordKey = passwordKey;
            this.contentType = contentType;
            this.timeoutSeconds = timeoutSeconds;
            this.rawParams = rawParams;
            this.rawBody = rawBody;
        }
    }

    public static class TestResultRow {
        private final String id;
        private final String testcase;
        private final String phone;
        private final String expected;
        private final String httpCode;
        private final String result;
        private final String message;

        private TestResultRow(String id, String testcase, String phone, String expected, String httpCode, String result, String message) {
            this.id = id;
            this.testcase = testcase;
            this.phone = phone;
            this.expected = expected;
            this.httpCode = httpCode;
            this.result = result;
            this.message = message;
        }
    }
}
