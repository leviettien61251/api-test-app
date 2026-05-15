package com.example.apitestapp.controllers;


import com.example.apitestapp.models.TestCaseRowModel;
import com.example.apitestapp.services.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.apitestapp.services.ApiTestService;
import com.example.apitestapp.services.SignupTestData;
import com.example.apitestapp.services.SignupTestScenarios;
import com.example.apitestapp.services.LoginTestData;
import com.example.apitestapp.services.LoginTestScenarios;

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

                // Hiển thị Body mẫu - allow editing
                if (!scenarios.isEmpty()) {
                    SignupTestData first = scenarios.get(0);
                    bodyTextArea.setText(String.format("{\n  \"phoneNumber\": \"%s\",\n  \"password\": \"%s\"\n}",
                            first.getPhoneNumber(), first.getPassword()));
                    bodyTextArea.setEditable(true);
                    bodyTextArea.setWrapText(true);
                }
            } else {
                baseUrlField.setText("");
                bodyTextArea.setText("");
                resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + apiName);
            }
        } else if (apiName.contains("login")) {
            List<LoginTestData> scenarios = LoginTestScenarios.getLoginScenarios();

            if (!scenarios.isEmpty()) {
                // Cập nhật Base URL - allow editing
                baseUrlField.setText("http://localhost:8080/api/v1/login");
                baseUrlField.setEditable(true);

                // Nạp kịch bản vào bảng
                for (LoginTestData s : scenarios) {
                    String input = String.format("{ \"phoneNumber\": \"%s\", \"password\": \"%s\" }", s.getPhoneNumber(), s.getPassword());
                    testData.add(new TestCaseModel(
                            s.getScenario() + " - " + s.getDescription(),
                            input,
                            s.getExpectedCode(),
                            s.getPhoneNumber(),
                            s.getPassword()
                    ));
                }

                // Hiển thị Body mẫu - allow editing
                LoginTestData first = scenarios.get(0);
                bodyTextArea.setText(String.format("{\n  \"phoneNumber\": \"%s\",\n  \"password\": \"%s\"\n}",
                        first.getPhoneNumber(), first.getPassword()));
                bodyTextArea.setEditable(true);
                bodyTextArea.setWrapText(true);
            } else {
                baseUrlField.setText("");
                bodyTextArea.setText("");
                resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + apiName);
            }
        } else {
            baseUrlField.setText("");
            bodyTextArea.setText("");
            resultLogList.getItems().add("⚠️ Unknown API selection: " + apiName);
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

                Platform.runLater(() -> tc.setResult("⏳ Đang test..."));

                boolean isPass = runScenarioSetup(tc) && callActualApi(tc);

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

    private boolean callActualApi(TestCaseRowModel tc) {
        try {
            String requestBody = tc.getRequestBody();
            String targetUrl = baseUrlField.getText().trim();
            if (targetUrl.isEmpty()) {
                targetUrl = tc.getEndpoint();
            }

            Platform.runLater(() -> resultLogList.getItems().add("  Sending: " + requestBody));

            ApiResponse response = apiTestService.callApi(targetUrl, requestBody);
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

    private boolean runScenarioSetup(TestCaseRowModel tc) {
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
                    setupRequest.getRequestBody()
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
}
