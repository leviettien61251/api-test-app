package com.example.apitestapp.controllers;


import com.example.apitestapp.config.AppRunConfig;
import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.config.SelectedRunContext;
import com.example.apitestapp.models.TestCaseRowModel;
import com.example.apitestapp.models.TestResult;
import com.example.apitestapp.models.TestRun;
import com.example.apitestapp.services.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TestcaseController implements Initializable {

    private final ObservableList<TestCaseRowModel> testData = FXCollections.observableArrayList();
    @FXML
    private TreeView<String> testSuiteTree;
    @FXML
    private TableView<TestCaseRowModel> testCaseTable;
    @FXML
    private TableColumn<TestCaseRowModel, Boolean> colCheck;
    @FXML
    private TableColumn<TestCaseRowModel, String> colName, colInput, colExpected, colStatus, colResult;
    @FXML
    private ComboBox<String> executionModeCombo, stopConditionCombo;
    @FXML
    private ListView<String> resultLogList;
    @FXML
    private Label summaryText;
    @FXML
    private Button runAllBtn, stopBtn, saveReportBtn;
    @FXML
    private TextField baseUrlField; // Thanh URL
    @FXML
    private TextArea bodyTextArea;   // Khung JSON Body
    private volatile boolean isRunning = false;
    private ApiTestService apiTestService;
    private ApiScenarioRegistry scenarioRegistry;
    private ApiScenarioDefinition currentDefinition;
    private final RunStorage runStorage = RunStorage.getInstance();
    private final List<TestResult> lastRunResults = new ArrayList<>();
    private int lastPassCount;
    private int lastFailCount;
    private Instant lastRunStartedAt;
    private boolean lastRunWasAll;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBoxes();

        apiTestService = new ApiTestService();
        scenarioRegistry = new ApiScenarioRegistry();
        initTreeView();

        testCaseTable.setItems(testData);
    }

    private void setupTable() {
        colCheck.setCellValueFactory(new PropertyValueFactory<>("selected"));
        colCheck.setCellFactory(tc -> new javafx.scene.control.cell.CheckBoxTableCell<>());
        testCaseTable.setEditable(true);

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colInput.setCellValueFactory(new PropertyValueFactory<>("input"));
        colExpected.setCellValueFactory(new PropertyValueFactory<>("expected"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        testCaseTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedTestCase) -> {
            if (selectedTestCase != null) {
                showRequestBody(selectedTestCase);
            }
        });
    }


    private void setupComboBoxes() {
        executionModeCombo.getItems().addAll("Lần lượt (Sequential)", "Song song (Parallel)");
        executionModeCombo.setValue("Lần lượt (Sequential)");

        stopConditionCombo.getItems().addAll("Chạy liên tục đến hết", "Dừng ngay khi có FAIL");
        stopConditionCombo.setValue("Dừng ngay khi có FAIL");
    }

    private void initTreeView() {
        List<ApiScenarioDefinition> definitions = scenarioRegistry.getDefinitions();
        String rootName = definitions.isEmpty() ? "Collections" : definitions.get(0).getCollectionName();
        TreeItem<String> root = new TreeItem<>(rootName);
        Map<String, TreeItem<String>> moduleNodes = new LinkedHashMap<>();

        for (ApiScenarioDefinition definition : definitions) {
            TreeItem<String> moduleNode = moduleNodes.computeIfAbsent(
                    definition.getModuleName(),
                    moduleName -> {
                        TreeItem<String> item = new TreeItem<>(moduleName);
                        root.getChildren().add(item);
                        return item;
                    }
            );
            moduleNode.getChildren().add(new TreeItem<>(definition.getApiLabel()));
        }

        testSuiteTree.setRoot(root);
        root.setExpanded(true);
        moduleNodes.values().forEach(node -> node.setExpanded(true));

        testSuiteTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isLeaf()) {
                handleApiSelection(newValue.getValue());
            }
        });
    }

    /**
     * Hàm xử lý ánh xạ kịch bản từ Service sang TableView
     */
    private void handleApiSelection(String apiName) {
        testData.clear();
        resultLogList.getItems().clear();
        summaryText.setText("Chưa có dữ liệu thực thi");

        scenarioRegistry.findByApiLabel(apiName)
                .ifPresentOrElse(this::loadScenarioDefinition, () -> {
                    currentDefinition = null;
                    baseUrlField.setText("");
                    bodyTextArea.setText("");
                    resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + apiName);
                });
    }

    private void loadScenarioDefinition(ApiScenarioDefinition definition) {
        currentDefinition = definition;
        List<ApiTestScenario> scenarios = definition.getScenarios();
        if (scenarios.isEmpty()) {
            baseUrlField.setText("");
            bodyTextArea.setText("");
            resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + definition.getApiLabel());
            return;
        }

        baseUrlField.setText("http://localhost:8080" + definition.getEndpoint());
        baseUrlField.setEditable(true);

        for (ApiTestScenario scenario : scenarios) {
            String requestBody = scenario.getRequestBody();
            testData.add(new TestCaseRowModel(
                    scenario.getDisplayName(),
                    requestBody,
                    scenario.getExpectedCode(),
                    definition.getEndpoint(),
                    requestBody,
                    scenario
            ));
        }

        String sampleRequestBody = definition.getSampleRequestBody();
        if (sampleRequestBody == null || sampleRequestBody.isBlank()) {
            sampleRequestBody = scenarios.get(0).getRequestBody();
        }
        bodyTextArea.setText(sampleRequestBody);
        bodyTextArea.setEditable(true);
        bodyTextArea.setWrapText(true);
        testCaseTable.getSelectionModel().selectFirst();
    }

    private void showRequestBody(TestCaseRowModel testCase) {
        String requestBody = testCase.getRequestBody();
        bodyTextArea.setText(requestBody == null ? "" : requestBody);
    }

    @FXML
    private void handleRunAll() {
        runTests(true);
    }

    @FXML
    private void handleRunSelected() {
        runTests(false);
    }

    @FXML
    private void handleStop() {
        isRunning = false;
        resultLogList.getItems().add("⏹ Đã dừng thực thi");
    }

    @FXML
    private void handleSaveReport() {
        if (lastRunResults.isEmpty()) {
            showInfo("Chưa có kết quả", "Hãy chạy testcase trước, sau đó bấm Lưu báo cáo.");
            return;
        }
        String runId = persistLastRun();
        if (runId == null || runId.isBlank()) {
            showInfo("Lỗi lưu", "Không ghi được file. Xem log console.");
            return;
        }
        showInfo("Đã lưu báo cáo",
                "File:\n" + runStorage.getStorageFile()
                        + "\nMã run: " + shortId(runId)
                        + "\nTổng run đã lưu: " + runStorage.count()
                        + "\n\nMở History / Dashboard / Report để xem.");
    }

    private String persistLastRun() {
        try {
            TestRun run = buildTestRunFromLastExecution();
            String runId = runStorage.saveCompleteRun(run);
            SelectedRunContext.setSelectedRunId(runId);
            return runId;
        } catch (Exception e) {
            System.err.println("Lưu run thất bại: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private TestRun buildTestRunFromLastExecution() {
        String apiLabel = currentDefinition != null ? currentDefinition.getApiLabel() : "API Test";
        String runName = apiLabel + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        TestRun run = new TestRun();
        run.setRunName(runName);
        run.setRunMode(lastRunWasAll ? "Run All" : "Run Selected");
        run.setFailureStrategy(stopConditionCombo.getValue());
        run.setStartedAt(lastRunStartedAt != null ? lastRunStartedAt : Instant.now());
        run.setCompletedAt(Instant.now());
        run.setTotalCases(lastPassCount + lastFailCount);
        run.setPassedCases(lastPassCount);
        run.setFailedCases(lastFailCount);
        run.setUser(AppSession.getUsername());
        run.setMachine(AppRunConfig.getMachineName());
        run.setOs(AppRunConfig.getOs());
        run.setResults(new ArrayList<>(lastRunResults));
        return run;
    }

    private void runTests(boolean all) {
        if (isRunning || testData.isEmpty()) return;
        isRunning = true;
        lastRunWasAll = all;
        lastRunStartedAt = Instant.now();
        lastRunResults.clear();
        lastPassCount = 0;
        lastFailCount = 0;
        resultLogList.getItems().clear();
        ApiScenarioDefinition definitionToRun = currentDefinition;

        new Thread(() -> {
            int pass = 0, fail = 0;
            boolean executedAny = false;
            String stopCond = stopConditionCombo.getValue();
            List<TestResult> collectedResults = new ArrayList<>();

            for (TestCaseRowModel tc : testData) {
                if (!isRunning) break;
                if (!all && !tc.isSelected()) continue;
                executedAny = true;

                Map<String, String> runtimeVariables = new LinkedHashMap<>();
                Platform.runLater(() -> tc.setResult("⏳ Đang test..."));

                boolean setupOk = runScenarioSetup(tc, runtimeVariables);
                CaseOutcome outcome = setupOk ? callActualApi(tc, runtimeVariables) : CaseOutcome.failed("Setup thất bại");
                boolean isPass = outcome.passed;

                TestResult result = toTestResult(tc, outcome);
                collectedResults.add(result);

                Platform.runLater(() -> {
                    tc.setResult(isPass ? "✅ PASS" : "❌ FAIL");
                    tc.setStatus(outcome.actualCodeText);
                    resultLogList.getItems().add((isPass ? "✅ " : "❌ ") + tc.getName());
                });

                if (isPass) pass++;
                else fail++;

                if (!isPass && "Dừng ngay khi có FAIL".equals(stopCond)) {
                    isRunning = false;
                    Platform.runLater(() -> resultLogList.getItems().add("⛔ DỪNG: Phát hiện lỗi tại " + tc.getName()));
                    break;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (executedAny) {
                runScenarioCleanup(definitionToRun);
            }

            final int p = pass, f = fail;
            final List<TestResult> resultsSnapshot = new ArrayList<>(collectedResults);
            Platform.runLater(() -> finishRun(p, f, resultsSnapshot));
        }).start();
    }

    private void finishRun(int pass, int fail, List<TestResult> resultsSnapshot) {
        lastRunResults.clear();
        lastRunResults.addAll(resultsSnapshot);
        lastPassCount = pass;
        lastFailCount = fail;
        isRunning = false;

        int total = pass + fail;
        if (total == 0) {
            summaryText.setText("Chưa chạy testcase nào");
        } else {
            summaryText.setText("Pass: " + pass + " | Fail: " + fail + " | Tổng: " + total
                    + " — Bấm «Lưu báo cáo» để ghi History");
        }
    }

    private CaseOutcome callActualApi(TestCaseRowModel tc, Map<String, String> runtimeVariables) {
        long started = System.currentTimeMillis();
        try {
            String requestBody = replaceVariables(tc.getRequestBody(), runtimeVariables);
            if (hasUnresolvedVariables(requestBody)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Request body has unresolved variables: " + requestBody));
                return CaseOutcome.failed("Biến chưa được thay thế");
            }
            String targetUrl = baseUrlField.getText().trim();
            if (targetUrl.isEmpty()) {
                targetUrl = tc.getEndpoint();
            }

            Platform.runLater(() -> resultLogList.getItems().add("  Sending: " + requestBody));

            ApiResponse response = apiTestService.callApi(targetUrl, requestBody);
            String expectedCode = tc.getExpected();
            String actualCode = response.getResponseCode();
            boolean isPass = expectedCode.equals(actualCode);
            long elapsed = System.currentTimeMillis() - started;
            String logMessage = String.format("Code: %s, HTTP: %d", actualCode, response.getHttpCode());

            Platform.runLater(() -> resultLogList.getItems().add("  " + logMessage));

            return new CaseOutcome(isPass, expectedCode, actualCode, elapsed, logMessage);
        } catch (Exception e) {
            Platform.runLater(() -> resultLogList.getItems().add("  Error: " + e.getMessage()));
            return CaseOutcome.failed(e.getMessage());
        }
    }

    private TestResult toTestResult(TestCaseRowModel tc, CaseOutcome outcome) {
        TestResult result = new TestResult();
        result.setCaseName(tc.getName());
        result.setStatus(outcome.passed ? "PASSED" : "FAILED");
        result.setMessage(outcome.message);
        result.setExpectedCode(parseCode(outcome.expectedCodeText));
        result.setActualCode(parseCode(outcome.actualCodeText));
        result.setResponseTimeMs(outcome.responseTimeMs);
        result.setExecutedAt(Instant.now());
        return result;
    }

    private static int parseCode(String code) {
        try {
            return Integer.parseInt(code);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8) + "...";
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final class CaseOutcome {
        final boolean passed;
        final String expectedCodeText;
        final String actualCodeText;
        final long responseTimeMs;
        final String message;

        CaseOutcome(boolean passed, String expectedCodeText, String actualCodeText, long responseTimeMs, String message) {
            this.passed = passed;
            this.expectedCodeText = expectedCodeText;
            this.actualCodeText = actualCodeText;
            this.responseTimeMs = responseTimeMs;
            this.message = message;
        }

        static CaseOutcome failed(String message) {
            return new CaseOutcome(false, "-", "-", 0, message);
        }
    }

    private void runScenarioCleanup(ApiScenarioDefinition definition) {
        if (definition == null || definition.getCleanupRequests().isEmpty()) {
            return;
        }

        for (ApiCleanupRequest cleanupRequest : definition.getCleanupRequests()) {
            String cleanupUrl = resolveHookUrl(cleanupRequest.getEndpoint(), definition.getEndpoint());
            Platform.runLater(() -> resultLogList.getItems().add("  Cleanup: " + cleanupRequest.getMethod() + " " + cleanupUrl));

            ApiResponse response = apiTestService.callApi(
                    cleanupRequest.getMethod(),
                    cleanupUrl,
                    cleanupRequest.getRequestBody()
            );
            String actualCode = response.getResponseCode();
            boolean accepted = cleanupRequest.accepts(actualCode);

            String logMessage = String.format("  Cleanup result: %s, Code: %s, HTTP: %d",
                    accepted ? "PASS" : "FAIL",
                    actualCode,
                    response.getHttpCode());
            Platform.runLater(() -> resultLogList.getItems().add(logMessage));

            if (!accepted && cleanupRequest.isRequired()) {
                Platform.runLater(() -> resultLogList.getItems().add("  Cleanup required but failed: " + cleanupRequest.getName()));
            }
        }
    }

    private boolean runScenarioSetup(TestCaseRowModel tc, Map<String, String> runtimeVariables) {
        ApiTestScenario scenario = tc.getScenario();
        if (scenario == null || scenario.getSetupRequests().isEmpty()) {
            return true;
        }

        for (ApiSetupRequest setupRequest : scenario.getSetupRequests()) {
            if (!isRunning) {
                return false;
            }

            Platform.runLater(() -> resultLogList.getItems().add("  Setup: " + setupRequest.getName()));

            ApiResponse response = apiTestService.callApi(
                    setupRequest.getMethod(),
                    resolveHookUrl(setupRequest.getEndpoint(), tc.getEndpoint()),
                    replaceVariables(setupRequest.getRequestBody(), runtimeVariables)
            );
            String actualCode = response.getResponseCode();
            boolean accepted = setupRequest.accepts(actualCode);

            String logMessage = String.format("  Setup result: %s, Code: %s, HTTP: %d",
                    accepted ? "PASS" : "FAIL",
                    actualCode,
                    response.getHttpCode());
            Platform.runLater(() -> resultLogList.getItems().add(logMessage));

            if (!accepted && setupRequest.isRequired()) {
                return false;
            }

            if (accepted && !captureResponseVariables(setupRequest, response, runtimeVariables)) {
                return false;
            }
        }

        return true;
    }

    private String resolveHookUrl(String endpointOrUrl, String testEndpoint) {
        if (endpointOrUrl == null || endpointOrUrl.isBlank()) {
            return endpointOrUrl;
        }
        if (endpointOrUrl.startsWith("http://") || endpointOrUrl.startsWith("https://")) {
            return endpointOrUrl;
        }

        String targetUrl = baseUrlField.getText() == null ? "" : baseUrlField.getText().trim();
        if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
            try {
                URI uri = URI.create(targetUrl);
                String baseUrl = uri.getScheme() + "://" + uri.getAuthority();
                return baseUrl + (endpointOrUrl.startsWith("/") ? endpointOrUrl : "/" + endpointOrUrl);
            } catch (IllegalArgumentException ignored) {
                // Fall back to ApiTestService URL resolution.
            }
        }

        if (targetUrl.endsWith(testEndpoint)) {
            String baseUrl = targetUrl.substring(0, targetUrl.length() - testEndpoint.length());
            return baseUrl + (endpointOrUrl.startsWith("/") ? endpointOrUrl : "/" + endpointOrUrl);
        }

        return endpointOrUrl;
    }

    private boolean captureResponseVariables(ApiSetupRequest setupRequest,
                                             ApiResponse response,
                                             Map<String, String> runtimeVariables) {
        boolean capturedAll = true;
        for (ApiResponseVariable variable : setupRequest.getResponseVariables()) {
            java.util.Optional<String> capturedValue = extractJsonValue(response.getResponseBody(), variable.getJsonPath());
            if (capturedValue.isPresent()) {
                String value = capturedValue.get();
                runtimeVariables.put(variable.getName(), value);
                Platform.runLater(() -> resultLogList.getItems().add(
                        "  Captured: " + variable.getName() + "=" + value
                ));
            } else {
                capturedAll = false;
                Platform.runLater(() -> resultLogList.getItems().add(
                        "  Capture missing: " + variable.getName() + " from " + variable.getJsonPath()
                ));
            }
        }
        return capturedAll;
    }

    private java.util.Optional<String> extractJsonValue(String responseBody, String jsonPath) {
        if (responseBody == null || responseBody.isBlank() || jsonPath == null || jsonPath.isBlank()) {
            return java.util.Optional.empty();
        }

        try {
            JsonElement current = JsonParser.parseString(responseBody);
            for (String part : jsonPath.split("\\.")) {
                if (current.isJsonObject()) {
                    if (!current.getAsJsonObject().has(part)) {
                        return java.util.Optional.empty();
                    }
                    current = current.getAsJsonObject().get(part);
                } else if (current.isJsonArray()) {
                    int index = Integer.parseInt(part);
                    if (index < 0 || index >= current.getAsJsonArray().size()) {
                        return java.util.Optional.empty();
                    }
                    current = current.getAsJsonArray().get(index);
                } else {
                    return java.util.Optional.empty();
                }
            }

            if (current == null || current.isJsonNull()) {
                return java.util.Optional.empty();
            }
            if (current.isJsonPrimitive()) {
                return java.util.Optional.of(current.getAsJsonPrimitive().getAsString());
            }
            return java.util.Optional.of(current.toString());
        } catch (Exception ignored) {
            return java.util.Optional.empty();
        }
    }

    private String replaceVariables(String requestBody, Map<String, String> runtimeVariables) {
        if (requestBody == null || requestBody.isBlank() || runtimeVariables.isEmpty()) {
            return requestBody;
        }

        String resolvedBody = requestBody;
        for (Map.Entry<String, String> entry : runtimeVariables.entrySet()) {
            resolvedBody = resolvedBody.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return resolvedBody;
    }

    private boolean hasUnresolvedVariables(String requestBody) {
        return requestBody != null && requestBody.matches("(?s).*\\$\\{[A-Za-z0-9_]+}.*");
    }
}
