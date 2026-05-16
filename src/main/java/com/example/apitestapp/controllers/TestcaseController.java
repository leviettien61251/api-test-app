package com.example.apitestapp.controllers;


import com.example.apitestapp.models.TestCaseRowModel;
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
    private Button runAllBtn, stopBtn;
    @FXML
    private TextField baseUrlField; // Thanh URL
    @FXML
    private TextArea bodyTextArea;   // Khung JSON Body
    private volatile boolean isRunning = false;
    private ApiTestService apiTestService;
    private ApiScenarioRegistry scenarioRegistry;
    private ApiScenarioDefinition currentDefinition;

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

        String apiMethod = resolveApiMethod(definition);
        for (ApiTestScenario scenario : scenarios) {
            String requestBody = scenario.getRequestBody();
            String testInput = buildTestInput(requestBody, scenario.getQueryParams());
            testData.add(new TestCaseRowModel(
                    scenario.getDisplayName(),
                    testInput,
                    scenario.getExpectedCode(),
                    definition.getEndpoint(),
                    apiMethod,
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

    private void runTests(boolean all) {
        if (isRunning || testData.isEmpty()) return;
        isRunning = true;
        resultLogList.getItems().clear();
        ApiScenarioDefinition definitionToRun = currentDefinition;

        new Thread(() -> {
            int pass = 0, fail = 0;
            boolean executedAny = false;
            String stopCond = stopConditionCombo.getValue();

            for (TestCaseRowModel tc : testData) {
                if (!isRunning) break;
                if (!all && !tc.isSelected()) continue;
                executedAny = true;

                Map<String, String> runtimeVariables = new LinkedHashMap<>();
                Platform.runLater(() -> tc.setResult("⏳ Đang test..."));

                boolean isPass = runScenarioSetup(tc, runtimeVariables) && callActualApi(tc, runtimeVariables);

                Platform.runLater(() -> {
                    tc.setResult(isPass ? "✅ PASS" : "❌ FAIL");
                    tc.setStatus(tc.getExpected());
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
            Platform.runLater(() -> {
                summaryText.setText("Pass: " + p + " | Fail: " + f + " | Tổng: " + (p + f));
                isRunning = false;
            });
        }).start();
    }

    private boolean callActualApi(TestCaseRowModel tc, Map<String, String> runtimeVariables) {
        try {
            String requestBody = replaceVariables(tc.getRequestBody(), runtimeVariables);
            if (hasUnresolvedVariables(requestBody)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Request body has unresolved variables: " + requestBody));
                return false;
            }
            ApiTestScenario scenario = tc.getScenario();
            Map<String, String> queryParams = replaceVariables(
                    scenario == null ? Map.of() : scenario.getQueryParams(),
                    runtimeVariables
            );
            if (hasUnresolvedVariables(queryParams)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Query params have unresolved variables: " + queryParams));
                return false;
            }
            String targetUrl = baseUrlField.getText().trim();
            if (targetUrl.isEmpty()) {
                targetUrl = tc.getEndpoint();
            }

            String resolvedTargetUrl = targetUrl;
            Platform.runLater(() -> resultLogList.getItems().add("  Sending: " + tc.getMethod() + " " + resolvedTargetUrl));
            if (!queryParams.isEmpty()) {
                Platform.runLater(() -> resultLogList.getItems().add("  Query Params: " + queryParams));
            }

            ApiResponse response = apiTestService.callApi(tc.getMethod(), resolvedTargetUrl, requestBody, queryParams);
            String expectedCode = tc.getExpected();
            String actualCode = response.getResponseCode();
            boolean isPass = expectedCode.equals(actualCode);
            String logMessage = String.format("Code: %s, HTTP: %d",
                    actualCode, response.getHttpCode());

            Platform.runLater(() -> resultLogList.getItems().add("  " + logMessage));

            return isPass;
        } catch (Exception e) {
            Platform.runLater(() -> resultLogList.getItems().add("  Error: " + e.getMessage()));
            e.printStackTrace();
            return false;
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

    private String resolveApiMethod(ApiScenarioDefinition definition) {
        if (definition == null || definition.getApiLabel() == null || definition.getApiLabel().isBlank()) {
            return "POST";
        }

        String apiLabel = definition.getApiLabel().trim();
        int firstSpace = apiLabel.indexOf(' ');
        if (firstSpace <= 0) {
            return "POST";
        }

        String method = apiLabel.substring(0, firstSpace).trim().toUpperCase();
        return switch (method) {
            case "GET", "POST", "PUT", "PATCH", "DELETE" -> method;
            default -> "POST";
        };
    }

    private String buildTestInput(String requestBody, Map<String, String> queryParams) {
        boolean hasRequestBody = requestBody != null && !requestBody.isBlank();
        boolean hasQueryParams = queryParams != null && !queryParams.isEmpty();

        if (hasRequestBody && hasQueryParams) {
            return requestBody + "\nQuery Params: " + queryParams;
        }
        if (hasQueryParams) {
            return "Query Params: " + queryParams;
        }
        return requestBody;
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

    private Map<String, String> replaceVariables(Map<String, String> values, Map<String, String> runtimeVariables) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        if (runtimeVariables.isEmpty()) {
            return values;
        }

        Map<String, String> resolvedValues = new LinkedHashMap<>();
        values.forEach((key, value) -> resolvedValues.put(key, replaceVariables(value, runtimeVariables)));
        return resolvedValues;
    }

    private boolean hasUnresolvedVariables(String requestBody) {
        return requestBody != null && requestBody.matches("(?s).*\\$\\{[A-Za-z0-9_]+}.*");
    }

    private boolean hasUnresolvedVariables(Map<String, String> values) {
        return values != null && values.values().stream().anyMatch(this::hasUnresolvedVariables);
    }
}
