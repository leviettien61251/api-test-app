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

import java.net.URL;
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
                    baseUrlField.setText("");
                    bodyTextArea.setText("");
                    resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + apiName);
                });
    }

    private void loadScenarioDefinition(ApiScenarioDefinition definition) {
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
                    requestBody
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

        new Thread(() -> {
            int pass = 0, fail = 0;
            String stopCond = stopConditionCombo.getValue();

            for (TestCaseRowModel tc : testData) {
                if (!isRunning) break;
                if (!all && !tc.isSelected()) continue;

                Platform.runLater(() -> tc.setResult("⏳ Đang test..."));

                // Call actual API
                boolean isPass = callActualApi(tc);

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
}
