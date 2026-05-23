package com.example.apitestapp.controllers;


import com.example.apitestapp.config.AppRunConfig;
import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.config.SelectedRunContext;
import com.example.apitestapp.models.*;
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

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TestcaseController implements Initializable {

    private static final String CONTINUE_ALERT_MODE = "Continue";
    private static final String RUN_UNTIL_FINISHED_LABEL = "Chạy liên tục đến hết";
    private static final String STOP_ON_FAIL_LABEL = "Dừng ngay khi có FAIL";

    private final ObservableList<TestCaseRowModel> testData = FXCollections.observableArrayList();
    private String baseUrl;
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
    private Button runAllBtn, stopBtn, saveReportBtn, addTestCaseBtn, editTestCaseBtn, deleteTestCaseBtn;
    @FXML
    private Button addTestSuiteBtn, editTestSuiteBtn, deleteTestSuiteBtn, editCleanupDataBtn;
    @FXML
    private TextField baseUrlField; // Thanh URL
    @FXML
    private TextArea headerTextArea; // Header cua testcase
    @FXML
    private TextArea bodyTextArea;   // Khung JSON Body
    private volatile boolean isRunning = false;
    private ApiTestService apiTestService;
    private final ApiPayloadAssertionEvaluator payloadAssertionEvaluator = new ApiPayloadAssertionEvaluator();
    private UserTestSuiteService userTestSuiteService;
    private UserTestCaseService userTestCaseService;
    private ApiScenarioRegistry scenarioRegistry;
    private ApiScenarioDefinition currentDefinition;
    private UserTestSuite currentUserSuite;
    private final Map<TreeItem<String>, UserTestSuite> userSuiteNodes = new IdentityHashMap<>();
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
        baseUrl = AppRunConfig.getBaseUrl();
        apiTestService = new ApiTestService();
        userTestSuiteService = new UserTestSuiteService();
        userTestCaseService = new UserTestCaseService();
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

        stopConditionCombo.getItems().addAll(RUN_UNTIL_FINISHED_LABEL, STOP_ON_FAIL_LABEL);
        stopConditionCombo.setValue(CONTINUE_ALERT_MODE.equals(AppRunConfig.getAlertMode())
                ? RUN_UNTIL_FINISHED_LABEL
                : STOP_ON_FAIL_LABEL);
    }

    private void initTreeView() {
        userSuiteNodes.clear();
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

        appendUserSuites(root);

        testSuiteTree.setRoot(root);
        root.setExpanded(true);
        moduleNodes.values().forEach(node -> node.setExpanded(true));

        testSuiteTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isLeaf()) {
                UserTestSuite suite = userSuiteNodes.get(newValue);
                if (suite != null) {
                    loadUserSuite(suite);
                } else {
                    handleApiSelection(newValue.getValue());
                }
            }
        });
    }

    private void appendUserSuites(TreeItem<String> root) {
        try {
            List<UserTestSuite> suites = userTestSuiteService.findForCurrentUser();
            if (suites.isEmpty()) {
                return;
            }

            TreeItem<String> userRoot = new TreeItem<>("User Test Suites");
            root.getChildren().add(userRoot);
            userRoot.setExpanded(true);

            for (UserTestSuite suite : suites) {
                TreeItem<String> item = new TreeItem<>(suite.getApiLabel());
                userRoot.getChildren().add(item);
                userSuiteNodes.put(item, suite);
            }
        } catch (Exception e) {
            resultLogList.getItems().add("Không nạp được testsuit user từ database: " + e.getMessage());
        }
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
                    headerTextArea.setText("");
                    bodyTextArea.setText("");
                    resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + apiName);
                });
    }

    private void loadScenarioDefinition(ApiScenarioDefinition definition) {
        currentDefinition = definition;
        currentUserSuite = null;
        List<ApiTestScenario> scenarios = definition.getScenarios();
        baseUrlField.setText("http://localhost:8080" + definition.getEndpoint());
        baseUrlField.setEditable(true);

        if (scenarios.isEmpty()) {
            headerTextArea.setText("");
            bodyTextArea.setText("");
            resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + definition.getApiLabel());
            appendUserTestCases(definition);
            return;
        }

        String apiMethod = resolveApiMethod(definition);
        for (ApiTestScenario scenario : scenarios) {
            String requestBody = scenario.getRequestBody();
            String testInput = buildTestInput(requestBody, scenario.getHeaders(), scenario.getQueryParams());
            testData.add(new TestCaseRowModel(
                    scenario.getDisplayName(),
                    testInput,
                    scenario.getExpectedCode(),
                    definition.getEndpoint(),
                    apiMethod,
                    scenario.getHeaders(),
                    requestBody,
                    scenario
            ));
        }

        String sampleRequestBody = definition.getSampleRequestBody();
        if (sampleRequestBody == null || sampleRequestBody.isBlank()) {
            sampleRequestBody = scenarios.get(0).getRequestBody();
        }
        headerTextArea.setText(formatHeaders(scenarios.get(0).getHeaders()));
        headerTextArea.setEditable(true);
        headerTextArea.setWrapText(true);
        bodyTextArea.setText(sampleRequestBody);
        bodyTextArea.setEditable(true);
        bodyTextArea.setWrapText(true);
        appendUserTestCases(definition);
        testCaseTable.getSelectionModel().selectFirst();
    }

    private void loadUserSuite(UserTestSuite suite) {
        currentUserSuite = suite;
        currentDefinition = ApiScenarioDefinition.builder()
                .collectionName("User Test Suites")
                .moduleName("User Test Suites")
                .apiLabel(suite.getApiLabel())
                .endpoint(suite.getEndpoint())
                .sampleRequestBody("")
                .scenarios(List.of())
                .cleanupRequests(suite.getCleanupRequests())
                .build();

        testData.clear();
        resultLogList.getItems().clear();
        summaryText.setText("Chưa có dữ liệu thực thi");
        baseUrlField.setText(suite.getEndpoint());
        baseUrlField.setEditable(true);
        headerTextArea.setText("");
        bodyTextArea.setText("");

        try {
            List<UserTestCase> testCases = userTestCaseService.findBySuite(suite.getId());
            for (UserTestCase testCase : testCases) {
                testData.add(toRowModel(testCase));
            }
            resultLogList.getItems().add("Đã nạp testsuit user: " + suite.getName());
        } catch (Exception e) {
            resultLogList.getItems().add("Không nạp được testcase của testsuit: " + e.getMessage());
        }

        if (!testData.isEmpty()) {
            testCaseTable.getSelectionModel().selectFirst();
        }
    }

    private void appendUserTestCases(ApiScenarioDefinition definition) {
        try {
            List<UserTestCase> userTestCases = userTestCaseService.findForCurrentUserAndApi(definition.getApiLabel());
            for (UserTestCase testCase : userTestCases) {
                testData.add(toRowModel(testCase));
            }
            if (!userTestCases.isEmpty()) {
                resultLogList.getItems().add("Đã nạp " + userTestCases.size() + " testcase user từ database.");
            }
        } catch (Exception e) {
            resultLogList.getItems().add("Không nạp được testcase user từ database: " + e.getMessage());
        }
    }

    private TestCaseRowModel toRowModel(UserTestCase testCase) {
        ApiTestScenario scenario = ApiTestScenario.builder()
                .scenario(testCase.getName())
                .description(testCase.getDescription())
                .setupRequests(testCase.getSetupRequests())
                .headers(testCase.getRequestHeaders())
                .queryParams(testCase.getQueryParams())
                .requestBody(testCase.getRequestBody())
                .expectedCode(String.valueOf(testCase.getExpectedStatusCode()))
                .expectedStatus("User testcase")
                .build();

        String input = buildTestInput(testCase.getRequestBody(), testCase.getRequestHeaders(), testCase.getQueryParams());
        return new TestCaseRowModel(
                "[User] " + testCase.getName(),
                input,
                String.valueOf(testCase.getExpectedStatusCode()),
                testCase.getEndpoint(),
                testCase.getMethod(),
                testCase.getRequestHeaders(),
                testCase.getRequestBody(),
                scenario,
                testCase.getId(),
                testCase.getCleanupRequests()
        );
    }

    private void showRequestBody(TestCaseRowModel testCase) {
        String requestBody = testCase.getRequestBody();
        headerTextArea.setText(formatHeaders(testCase.getHeaders()));
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
    private void handleAddTestSuite() {
        try {
            Optional<String> name = promptText("Thêm testsuit", "Tên testsuit", "Nhập tên testsuit:");
            if (name.isEmpty()) return;
            Optional<String> method = promptText("Thêm testsuit", "Method", "Nhập method:", "POST");
            if (method.isEmpty()) return;
            Optional<String> endpoint = promptText("Thêm testsuit", "Endpoint hoặc URL", "Nhập endpoint hoặc URL:", baseUrl);
            if (endpoint.isEmpty()) return;

            UserTestSuite suite = userTestSuiteService.create(name.get(), method.get(), endpoint.get(), "");
            initTreeView();
            selectUserSuite(suite.getId());
            showInfo("Đã thêm testsuit", "Testsuit đã được lưu vào database.");
        } catch (Exception e) {
            showInfo("Không thêm được testsuit", e.getMessage());
        }
    }

    @FXML
    private void handleEditTestSuite() {
        if (currentUserSuite == null) {
            showInfo("Không thể sửa", "Chỉ có thể sửa testsuit do user tạo.");
            return;
        }

        try {
            Optional<String> name = promptText("Sửa testsuit", "Tên testsuit", "Nhập tên testsuit:", currentUserSuite.getName());
            if (name.isEmpty()) return;
            Optional<String> method = promptText("Sửa testsuit", "Method", "Nhập method:", currentUserSuite.getMethod());
            if (method.isEmpty()) return;
            Optional<String> endpoint = promptText("Sửa testsuit", "Endpoint hoặc URL", "Nhập endpoint hoặc URL:", currentUserSuite.getEndpoint());
            if (endpoint.isEmpty()) return;

            UserTestSuite updated = userTestSuiteService.update(currentUserSuite.getId(), name.get(), method.get(), endpoint.get(), currentUserSuite.getDescription());
            initTreeView();
            selectUserSuite(updated.getId());
            showInfo("Đã sửa testsuit", "Testsuit đã được cập nhật.");
        } catch (Exception e) {
            showInfo("Không sửa được testsuit", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTestSuite() {
        if (currentUserSuite == null) {
            showInfo("Không thể xóa", "Chỉ có thể xóa testsuit do user tạo.");
            return;
        }
        if (!confirm("Xóa testsuit", "Bạn chắc chắn muốn xóa testsuit này?")) {
            return;
        }

        try {
            userTestSuiteService.delete(currentUserSuite.getId());
            currentUserSuite = null;
            currentDefinition = null;
            testData.clear();
            baseUrlField.setText("");
            headerTextArea.setText("");
            bodyTextArea.setText("");
            initTreeView();
            showInfo("Đã xóa testsuit", "Testsuit đã được xóa khỏi danh sách.");
        } catch (Exception e) {
            showInfo("Không xóa được testsuit", e.getMessage());
        }
    }

    @FXML
    private void handleEditCleanupData() {
        if (currentUserSuite == null) {
            showInfo("Không thể cấu hình cleanup", "Hãy chọn một testsuit do user tạo trước.");
            return;
        }

        try {
            List<ApiCleanupRequest> cleanupRequests = userTestCaseService.parseCleanupRequests(promptHookJson(
                    "Cleanup data",
                    "Cleanup requests JSON array - chạy một lần sau khi kết thúc lượt chạy testcase",
                    currentUserSuite.getCleanupRequests().isEmpty()
                            ? sampleCleanupRequestsJson()
                            : userTestCaseService.toJson(currentUserSuite.getCleanupRequests())
            ));
            currentUserSuite = userTestSuiteService.updateCleanupRequests(currentUserSuite.getId(), cleanupRequests);
            loadUserSuite(currentUserSuite);
            showInfo("Đã lưu cleanup data", "Cleanup data sẽ chạy sau khi kết thúc lượt chạy testcase.");
        } catch (Exception e) {
            showInfo("Không lưu được cleanup data", e.getMessage());
        }
    }

    @FXML
    private void handleAddTestCase() {
        if (currentDefinition == null) {
            showInfo("Chưa chọn API", "Hãy chọn một API trong cây Collections trước khi thêm testcase.");
            return;
        }

        try {
            Optional<String> name = promptText("Thêm testcase", "Tên testcase", "Nhập tên testcase:");
            if (name.isEmpty()) {
                return;
            }

            Optional<String> expectedStatus = promptText(
                    "Thêm testcase",
                    "Expected status code",
                    "Nhập HTTP status mong đợi:",
                    defaultExpectedStatus()
            );
            if (expectedStatus.isEmpty()) {
                return;
            }

            int statusCode = Integer.parseInt(expectedStatus.get().trim());
            List<ApiSetupRequest> setupRequests = userTestCaseService.parseSetupRequests(promptHookJson(
                    "Dữ liệu mồi",
                    "Setup requests JSON array",
                    sampleSetupRequestsJson()
            ));

            UserTestCase saved = userTestCaseService.create(
                    currentDefinition.getApiLabel(),
                    currentUserSuite == null ? null : currentUserSuite.getId(),
                    name.get(),
                    "",
                    resolveApiMethod(currentDefinition),
                    resolveEndpointFromUrl(),
                    parseHeaders(headerTextArea.getText()),
                    Map.of(),
                    bodyTextArea.getText(),
                    setupRequests,
                    List.of(),
                    statusCode
            );

            TestCaseRowModel row = toRowModel(saved);
            testData.add(row);
            testCaseTable.getSelectionModel().select(row);
            resultLogList.getItems().add("Đã lưu testcase user vào database: " + saved.getName());
            showInfo("Đã thêm testcase", "Testcase đã được lưu vào database.");
        } catch (NumberFormatException e) {
            showInfo("Status code không hợp lệ", "Expected status code phải là số, ví dụ: 200 hoặc 400.");
        } catch (Exception e) {
            showInfo("Không thêm được testcase", e.getMessage());
        }
    }

    @FXML
    private void handleEditTestCase() {
        TestCaseRowModel selected = testCaseTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isUserCreated()) {
            showInfo("Không thể sửa", "Chỉ có thể sửa testcase do user tạo.");
            return;
        }

        try {
            Optional<String> name = promptText("Sửa testcase", "Tên testcase", "Nhập tên testcase:", stripUserPrefix(selected.getName()));
            if (name.isEmpty()) return;
            Optional<String> expectedStatus = promptText(
                    "Sửa testcase",
                    "Expected status code",
                    "Nhập HTTP status mong đợi:",
                    selected.getExpected()
            );
            if (expectedStatus.isEmpty()) return;

            int statusCode = Integer.parseInt(expectedStatus.get().trim());
            List<ApiSetupRequest> setupRequests = userTestCaseService.parseSetupRequests(promptHookJson(
                    "Dữ liệu mồi",
                    "Setup requests JSON array",
                    userTestCaseService.toJson(selected.getScenario() == null ? List.of() : selected.getScenario().getSetupRequests())
            ));

            userTestCaseService.update(
                    selected.getUserTestCaseId(),
                    name.get(),
                    "",
                    selected.getMethod(),
                    resolveEndpointFromUrl(),
                    parseHeaders(headerTextArea.getText()),
                    Map.of(),
                    bodyTextArea.getText(),
                    setupRequests,
                    List.of(),
                    statusCode
            );
            reloadCurrentSelection();
            showInfo("Đã sửa testcase", "Testcase đã được cập nhật.");
        } catch (NumberFormatException e) {
            showInfo("Status code không hợp lệ", "Expected status code phải là số, ví dụ: 0, 200 hoặc 9999.");
        } catch (Exception e) {
            showInfo("Không sửa được testcase", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTestCase() {
        TestCaseRowModel selected = testCaseTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isUserCreated()) {
            showInfo("Không thể xóa", "Chỉ có thể xóa testcase do user tạo.");
            return;
        }
        if (!confirm("Xóa testcase", "Bạn chắc chắn muốn xóa testcase này?")) {
            return;
        }

        try {
            userTestCaseService.delete(selected.getUserTestCaseId());
            testData.remove(selected);
            showInfo("Đã xóa testcase", "Testcase đã được xóa khỏi danh sách.");
        } catch (Exception e) {
            showInfo("Không xóa được testcase", e.getMessage());
        }
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
            Map<String, String> cleanupRuntimeVariables = new LinkedHashMap<>();

            for (TestCaseRowModel tc : testData) {
                if (!isRunning) break;
                if (!all && !tc.isSelected()) continue;
                executedAny = true;

                Map<String, String> runtimeVariables = new LinkedHashMap<>();
                Platform.runLater(() -> tc.setResult("⏳ Đang test..."));

                boolean setupOk = runDefaultAuthSetup(tc, runtimeVariables) && runScenarioSetup(tc, runtimeVariables);
                CaseOutcome outcome = setupOk ? callActualApi(tc, runtimeVariables) : CaseOutcome.failed("Setup thất bại");
                cleanupRuntimeVariables.putAll(runtimeVariables);
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

                if (!isPass && STOP_ON_FAIL_LABEL.equals(stopCond)) {
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
                runScenarioCleanup(definitionToRun, cleanupRuntimeVariables);
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
            ApiTestScenario scenario = tc.getScenario();
            Map<String, String> queryParams = replaceVariables(
                    scenario == null ? Map.of() : scenario.getQueryParams(),
                    runtimeVariables
            );
            Map<String, String> headers = replaceVariables(tc.getHeaders(), runtimeVariables);
            if (hasUnresolvedVariables(queryParams)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Query params have unresolved variables: " + queryParams));
                return CaseOutcome.failed("Biến query params chưa được thay thế");
            }
            if (hasUnresolvedVariables(headers)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Headers have unresolved variables: " + headers));
                return CaseOutcome.failed("Biến header chưa được thay thế");
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
            if (!headers.isEmpty()) {
                Platform.runLater(() -> resultLogList.getItems().add("  Headers: " + headers));
            }

            ApiResponse response = apiTestService.callApi(tc.getMethod(), resolvedTargetUrl, requestBody, queryParams, headers);
            String expectedCode = tc.getExpected();
            String actualCode = response.getResponseCode();
            boolean codePassed = expectedCode.equals(actualCode);
            ApiPayloadAssertionEvaluator.Evaluation payloadEvaluation = payloadAssertionEvaluator.evaluate(
                    response.getResponseBody(),
                    scenario == null ? List.of() : scenario.getPayloadAssertions()
            );
            boolean isPass = codePassed && payloadEvaluation.passed();
            long elapsed = System.currentTimeMillis() - started;
            String logMessage = String.format("Code: %s, HTTP: %d", actualCode, response.getHttpCode());

            Platform.runLater(() -> resultLogList.getItems().add("  " + logMessage));
            for (String payloadMessage : payloadEvaluation.messages()) {
                Platform.runLater(() -> resultLogList.getItems().add("  Payload: " + payloadMessage));
            }

            String outcomeMessage = payloadEvaluation.passed() || payloadEvaluation.messages().isEmpty()
                    ? logMessage
                    : logMessage + " | " + String.join(" | ", payloadEvaluation.messages());
            return new CaseOutcome(isPass, expectedCode, actualCode, elapsed, outcomeMessage);
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

    private Optional<String> promptText(String title, String header, String content) {
        return promptText(title, header, content, "");
    }

    private Optional<String> promptText(String title, String header, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue == null ? "" : defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait().map(String::trim).filter(value -> !value.isBlank());
    }

    private String promptHookJson(String title, String header, String defaultValue) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea textArea = new TextArea(defaultValue == null ? "" : defaultValue);
        textArea.setPrefWidth(700);
        textArea.setPrefHeight(320);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Courier New';");
        dialog.getDialogPane().setContent(textArea);
        dialog.setResultConverter(button -> button == ButtonType.OK ? textArea.getText() : "");
        return dialog.showAndWait().orElse("");
    }

    private String sampleSetupRequestsJson() {
        return """
                [
                  {
                    "name": "Thêm dữ liệu mồi",
                    "method": "POST",
                    "endpoint": "/api/v1/insert-map-test",
                    "requestBody": "{\\n  \\"buildingCode\\": \\"B-TEST-001\\",\\n  \\"buildingName\\": \\"Building Test\\",\\n  \\"imageUrl\\": \\"https://example.com/test.jpg\\",\\n  \\"scaleX\\": 10,\\n  \\"scaleY\\": 10\\n}",
                    "headers": {},
                    "expectedCodes": ["1000", "200", "201"],
                    "required": true,
                    "responseVariables": [
                      { "name": "mapId", "jsonPath": "data.0.id" }
                    ]
                  }
                ]
                """;
    }

    private String sampleCleanupRequestsJson() {
        return """
                [
                  {
                    "name": "Dọn dữ liệu test",
                    "method": "DELETE",
                    "endpoint": "/api/v1/map/clean",
                    "requestBody": "",
                    "headers": {},
                    "expectedCodes": ["1000", "200", "204"],
                    "required": true
                  }
                ]
                """;
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    private void reloadCurrentSelection() {
        if (currentUserSuite != null) {
            loadUserSuite(currentUserSuite);
        } else if (currentDefinition != null) {
            loadScenarioDefinition(currentDefinition);
        }
    }

    private void selectUserSuite(String suiteId) {
        for (Map.Entry<TreeItem<String>, UserTestSuite> entry : userSuiteNodes.entrySet()) {
            if (entry.getValue().getId().equals(suiteId)) {
                testSuiteTree.getSelectionModel().select(entry.getKey());
                return;
            }
        }
    }

    private String stripUserPrefix(String name) {
        if (name == null) {
            return "";
        }
        return name.startsWith("[User] ") ? name.substring("[User] ".length()) : name;
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

    private void runScenarioCleanup(ApiScenarioDefinition definition, Map<String, String> runtimeVariables) {
        if (definition == null || definition.getCleanupRequests().isEmpty()) {
            return;
        }

        for (ApiCleanupRequest cleanupRequest : definition.getCleanupRequests()) {
            String cleanupUrl = resolveHookUrl(cleanupRequest.getEndpoint(), definition.getEndpoint());
            Platform.runLater(() -> resultLogList.getItems().add("  Cleanup: " + cleanupRequest.getMethod() + " " + cleanupUrl));

            ApiResponse response = apiTestService.callApi(
                    cleanupRequest.getMethod(),
                    cleanupUrl,
                    replaceVariables(cleanupRequest.getRequestBody(), runtimeVariables),
                    Map.of(),
                    replaceVariables(cleanupRequest.getHeaders(), runtimeVariables)
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
                    replaceVariables(setupRequest.getRequestBody(), runtimeVariables),
                    Map.of(),
                    replaceVariables(setupRequest.getHeaders(), runtimeVariables)
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

    private boolean runDefaultAuthSetup(TestCaseRowModel tc, Map<String, String> runtimeVariables) {
        if (!isRunning) {
            return false;
        }

        String phoneNumber = buildAuthPhoneNumber();
        String password = "111111";
        runtimeVariables.put("phoneNumber", phoneNumber);
        runtimeVariables.put("authPhoneNumber", phoneNumber);
        runtimeVariables.put("password", password);
        runtimeVariables.put("authPassword", password);

        Platform.runLater(() -> resultLogList.getItems().add("  Auth setup: signup + login mặc định"));

        String signupBody = """
                {
                  "phoneNumber": "%s",
                  "password": "%s"
                }
                """.formatted(phoneNumber, password);

        ApiResponse signupResponse = apiTestService.callApi(
                "POST",
                resolveHookUrl("/api/v1/signup", tc.getEndpoint()),
                signupBody,
                Map.of(),
                Map.of()
        );

        String signupCode = signupResponse.getResponseCode();
        boolean signupAccepted = List.of("1000", "200", "201", "3006").contains(signupCode);
        Platform.runLater(() -> resultLogList.getItems().add(
                "  Auth signup: " + (signupAccepted ? "PASS" : "FAIL")
                        + ", Code: " + signupCode
                        + ", HTTP: " + signupResponse.getHttpCode()
        ));
        if (!signupAccepted) {
            return false;
        }

        String loginBody = """
                {
                  "phoneNumber": "%s",
                  "password": "%s"
                }
                """.formatted(phoneNumber, password);

        ApiResponse loginResponse = apiTestService.callApi(
                "POST",
                resolveHookUrl("/api/v1/login", tc.getEndpoint()),
                loginBody,
                Map.of(),
                Map.of()
        );

        String loginCode = loginResponse.getResponseCode();
        boolean loginAccepted = List.of("1000", "200").contains(loginCode);
        Platform.runLater(() -> resultLogList.getItems().add(
                "  Auth login: " + (loginAccepted ? "PASS" : "FAIL")
                        + ", Code: " + loginCode
                        + ", HTTP: " + loginResponse.getHttpCode()
        ));
        if (!loginAccepted) {
            return false;
        }

        java.util.Optional<String> token = extractJsonValue(loginResponse.getResponseBody(), "token");
        if (token.isEmpty() || token.get().isBlank()) {
            Platform.runLater(() -> resultLogList.getItems().add("  Auth token: không lấy được token từ response login"));
            return false;
        }

        runtimeVariables.put("token", token.get());
        runtimeVariables.put("authorizationHeader", "Bearer " + token.get());
        Platform.runLater(() -> resultLogList.getItems().add("  Auth token: " + shortId(token.get())));
        return true;
    }

    private String buildAuthPhoneNumber() {
        long suffix = Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10_000_000L;
        return "098" + String.format("%07d", suffix);
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

    private String buildTestInput(String requestBody, Map<String, String> headers, Map<String, String> queryParams) {
        boolean hasRequestBody = requestBody != null && !requestBody.isBlank();
        boolean hasHeaders = headers != null && !headers.isEmpty();
        boolean hasQueryParams = queryParams != null && !queryParams.isEmpty();
        List<String> inputParts = new ArrayList<>();

        if (hasHeaders) {
            inputParts.add("Headers: " + headers);
        }
        if (hasQueryParams) {
            inputParts.add("Query Params: " + queryParams);
        }
        if (hasRequestBody) {
            inputParts.add(requestBody);
        }
        return String.join("\n", inputParts);
    }

    private Map<String, String> parseHeaders(String rawHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (rawHeaders == null || rawHeaders.isBlank()) {
            return headers;
        }

        for (String line : rawHeaders.split("\\R")) {
            if (line == null || line.isBlank()) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new IllegalArgumentException("Header không hợp lệ: " + line + ". Định dạng đúng: Key: Value");
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (!key.isBlank()) {
                headers.put(key, value);
            }
        }
        return headers;
    }

    private String resolveEndpointFromUrl() {
        String targetUrl = baseUrlField.getText() == null ? "" : baseUrlField.getText().trim();
        if (targetUrl.isBlank()) {
            return currentDefinition.getEndpoint();
        }
        return targetUrl;
    }

    private String defaultExpectedStatus() {
        TestCaseRowModel selected = testCaseTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getExpected() != null && selected.getExpected().matches("\\d{3}")) {
            return selected.getExpected();
        }
        return "200";
    }

    private String formatHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        headers.forEach((key, value) -> {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(key).append(": ").append(value == null ? "" : value);
        });
        return builder.toString();
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
