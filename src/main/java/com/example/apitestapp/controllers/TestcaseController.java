package com.example.apitestapp.controllers;


import com.example.apitestapp.config.AppRunConfig;
import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.config.SelectedRunContext;
import com.example.apitestapp.models.dto.*;
import com.example.apitestapp.models.entity.UserTestCase;
import com.example.apitestapp.models.entity.UserTestSuite;
import com.example.apitestapp.models.view.TestCaseRowModel;
import com.example.apitestapp.services.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
    private static final PseudoClass TEST_PASS_STATE = PseudoClass.getPseudoClass("test-pass");
    private static final PseudoClass TEST_FAIL_STATE = PseudoClass.getPseudoClass("test-fail");
    private static final PseudoClass TEST_RUNNING_STATE = PseudoClass.getPseudoClass("test-running");
    private static final PseudoClass TEST_PENDING_STATE = PseudoClass.getPseudoClass("test-pending");
    private static final PseudoClass LOG_SUCCESS_STATE = PseudoClass.getPseudoClass("log-success");
    private static final PseudoClass LOG_ERROR_STATE = PseudoClass.getPseudoClass("log-error");

    private final ObservableList<TestCaseRowModel> testData = FXCollections.observableArrayList();
    private final ApiPayloadAssertionEvaluator payloadAssertionEvaluator = new ApiPayloadAssertionEvaluator();
    private final Map<TreeItem<String>, UserTestSuite> userSuiteNodes = new IdentityHashMap<>();
    private final RunStorage runStorage = RunStorage.getInstance();
    private final List<TestResult> lastRunResults = new ArrayList<>();
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
    private Label summaryText, requestMethodLabel;
    @FXML
    private Button runAllBtn, runSelectedBtn, stopBtn, saveReportBtn, addTestCaseBtn, editTestCaseBtn, deleteTestCaseBtn;
    @FXML
    private Button addTestSuiteBtn, editTestSuiteBtn, deleteTestSuiteBtn, editCleanupDataBtn;
    @FXML
    private SplitPane testcaseContent;
    @FXML
    private StackPane runningOverlay;
    @FXML
    private TextField mainBaseUrlField;
    @FXML
    private TextField baseUrlField; // Thanh URL
    @FXML
    private TextArea headerTextArea; // Header cua testcase
    @FXML
    private TextArea bodyTextArea;   // Khung JSON Body
    private volatile boolean isRunning = false;
    private ApiTestService apiTestService;
    private UserTestSuiteService userTestSuiteService;
    private UserTestCaseService userTestCaseService;
    private ApiScenarioRegistry scenarioRegistry;
    private ApiScenarioDefinition currentDefinition;
    private UserTestSuite currentUserSuite;
    private int lastPassCount;
    private int lastFailCount;
    private Instant lastRunStartedAt;
    private boolean lastRunWasAll;

    /** Chuyển mã phản hồi dạng chuỗi sang số; trả về 0 nếu giá trị không hợp lệ. */
    private static int parseCode(String code) {
        try {
            return Integer.parseInt(code);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Rút gọn ID dài để hiển thị trên giao diện nhưng vẫn giữ phần đầu dễ nhận biết. */
    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8) + "...";
    }

    /**
     * Khởi tạo controller sau khi FXML được nạp: cấu hình bảng, combobox, service,
     * cây testsuite, style trạng thái và các tooltip trên giao diện.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBoxes();
        baseUrl = resolveConfiguredBaseUrl();
        if (mainBaseUrlField != null) {
            mainBaseUrlField.setText(baseUrl);
        }
        baseUrlField.setText(baseUrl);
        apiTestService = new ApiTestService();
        userTestSuiteService = new UserTestSuiteService();
        userTestCaseService = new UserTestCaseService();
        scenarioRegistry = new ApiScenarioRegistry();
        setupExecutionLogStyles();
        initTreeView();
        setRunningUiState(false);

//        tooltip
        testCaseTable.setItems(testData);
        testCaseTable.setTooltip(new Tooltip("Danh sách các Testcase"));
        headerTextArea.setTooltip(new Tooltip("Header"));
        bodyTextArea.setTooltip(new Tooltip("Body"));
        runAllBtn.setTooltip(new Tooltip("Khi nhấn sẽ chạy toán bộ các Testcase trong Testsuit hiện tại"));
        runSelectedBtn.setTooltip(new Tooltip("Khi nhấn sẽ chạy các Testcase được chọn trong Testsuit hiện tại"));
        stopBtn.setTooltip(new Tooltip("Khi nhấn sẽ dừng chạy test nhưng vẫn sẽ chạy cleanup request"));
        saveReportBtn.setTooltip(new Tooltip(""));
        addTestCaseBtn.setTooltip(new Tooltip("Thêm Testcase"));
        editTestCaseBtn.setTooltip(new Tooltip("Sửa Testcase"));
        deleteTestCaseBtn.setTooltip(new Tooltip("Xóa Testcase"));
        addTestSuiteBtn.setTooltip(new Tooltip("Thêm Testsuit"));
        editTestSuiteBtn.setTooltip(new Tooltip("Sửa Testsuit"));
        deleteTestSuiteBtn.setTooltip(new Tooltip("Xóa Testsuit"));

    }

    /**
     * Liên kết các cột với thuộc tính của {@link TestCaseRowModel}, bật checkbox
     * lựa chọn và hiển thị request của testcase khi người dùng chọn một dòng.
     */
    private void setupTable() {
        colCheck.setCellValueFactory(new PropertyValueFactory<>("selected"));
        colCheck.setCellFactory(tc -> new javafx.scene.control.cell.CheckBoxTableCell<>());
        testCaseTable.setEditable(true);

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colInput.setCellValueFactory(new PropertyValueFactory<>("input"));
        colExpected.setCellValueFactory(new PropertyValueFactory<>("expected"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        setupTestCaseRowStyles();

        testCaseTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedTestCase) -> {
            if (selectedTestCase != null) {
                showRequestBody(selectedTestCase);
            }
        });
    }

    /** Theo dõi kết quả từng testcase và gắn pseudo-class CSS tương ứng cho dòng. */
    private void setupTestCaseRowStyles() {
        testCaseTable.setRowFactory(table -> new TableRow<>() {
            private final ChangeListener<String> resultListener = (observable, oldValue, newValue) ->
                    updateTestCaseRowState(this, newValue);

            /** Gỡ listener khỏi item cũ, gắn vào item mới và đồng bộ style khi cell được tái sử dụng. */
            @Override
            protected void updateItem(TestCaseRowModel item, boolean empty) {
                TestCaseRowModel previousItem = getItem();
                if (previousItem != null) {
                    previousItem.resultProperty().removeListener(resultListener);
                }

                super.updateItem(item, empty);

                if (empty || item == null) {
                    updateTestCaseRowState(this, null);
                    return;
                }

                item.resultProperty().addListener(resultListener);
                updateTestCaseRowState(this, item.resultProperty().get());
            }
        });
    }

    /** Cập nhật trạng thái CSS PASS, FAIL, đang chạy hoặc đang chờ của một dòng. */
    private void updateTestCaseRowState(TableRow<TestCaseRowModel> row, String result) {
        String normalizedResult = result == null ? "" : result;
        row.pseudoClassStateChanged(TEST_PASS_STATE, normalizedResult.contains("PASS"));
        row.pseudoClassStateChanged(TEST_FAIL_STATE, normalizedResult.contains("FAIL"));
        row.pseudoClassStateChanged(TEST_RUNNING_STATE, normalizedResult.contains("Đang test"));
        row.pseudoClassStateChanged(TEST_PENDING_STATE, !normalizedResult.contains("PASS")
                && !normalizedResult.contains("FAIL")
                && !normalizedResult.contains("Đang test")
                && !normalizedResult.isBlank());
    }

    /** Tạo cell tùy chỉnh để các dòng kết quả chính trong execution log được tô màu. */
    private void setupExecutionLogStyles() {
        resultLogList.setCellFactory(list -> new ListCell<>() {
            /** Cập nhật nội dung và trạng thái màu mỗi khi ListView tái sử dụng cell. */
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                updateExecutionLogState(this, empty ? null : item);
            }
        });
    }
    /** Chỉ tô màu các dòng kết quả testcase bắt đầu bằng biểu tượng PASS hoặc FAIL. */
    private void updateExecutionLogState(ListCell<String> cell, String message) {
        String normalizedMessage = message == null ? "" : message;

        // Chỉ bôi màu cho dòng chính của testcase (bắt đầu bằng ✅ hoặc ❌)
        // và không bôi màu cho các dòng con (có dấu cách ở đầu hoặc chứa "Auth", "Setup", "Sending", v.v.)
        boolean isMainResultLine = (normalizedMessage.startsWith("✅") || normalizedMessage.startsWith("❌"))
                && !normalizedMessage.startsWith("  ");

        boolean hasError = isMainResultLine && normalizedMessage.contains("❌");
        boolean hasSuccess = isMainResultLine && normalizedMessage.contains("✅");

        // Reset styles cho các dòng không phải kết quả chính
        cell.pseudoClassStateChanged(LOG_SUCCESS_STATE, hasSuccess);
        cell.pseudoClassStateChanged(LOG_ERROR_STATE, hasError);
    }

    /** Nạp các chế độ dừng và khôi phục lựa chọn từ cấu hình ứng dụng. */
    private void setupComboBoxes() {
        // Đã xóa executionModeCombo (chạy tuần tự mặc định)
        stopConditionCombo.getItems().addAll(RUN_UNTIL_FINISHED_LABEL, STOP_ON_FAIL_LABEL);
        stopConditionCombo.setValue(CONTINUE_ALERT_MODE.equals(AppRunConfig.getAlertMode())
                ? RUN_UNTIL_FINISHED_LABEL
                : STOP_ON_FAIL_LABEL);
    }

    /**
     * Dựng lại cây Collections từ registry và database. Khi chọn node lá,
     * controller sẽ nạp kịch bản hệ thống hoặc testsuite do người dùng tạo.
     */
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

    /** Đọc testsuite của người dùng từ database và thêm chúng vào cây Collections. */
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
     * Xử lý khi chọn một API hệ thống: xóa dữ liệu cũ, tìm definition theo nhãn
     * và ánh xạ kịch bản sang TableView; nếu không tìm thấy thì reset vùng request.
     */
    private void handleApiSelection(String apiName) {
        testData.clear();
        resultLogList.getItems().clear();
        summaryText.setText("Chưa có dữ liệu thực thi");

        scenarioRegistry.findByApiLabel(apiName)
                .ifPresentOrElse(this::loadScenarioDefinition, () -> {
                    currentDefinition = null;
                    updateRequestMethodLabel("");
                    baseUrlField.setText("");
                    headerTextArea.setText("");
                    bodyTextArea.setText("");
                    resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + apiName);
                });
    }

    /**
     * Nạp definition hệ thống vào giao diện, tạo từng dòng testcase từ scenario,
     * điền request mẫu và ghép thêm các testcase user thuộc cùng API.
     */
    private void loadScenarioDefinition(ApiScenarioDefinition definition) {
        currentDefinition = definition;
        currentUserSuite = null;
        List<ApiTestScenario> scenarios = definition.getScenarios();
        String apiMethod = resolveApiMethod(definition);
        updateRequestMethodLabel(apiMethod);
        baseUrlField.setText(resolveScenarioTargetUrl(definition.getEndpoint()));
        baseUrlField.setEditable(true);

        if (scenarios.isEmpty()) {
            headerTextArea.setText("");
            bodyTextArea.setText("");
            resultLogList.getItems().add("⚠️ Hệ thống chưa nạp kịch bản cho: " + definition.getApiLabel());
            appendUserTestCases(definition);
            return;
        }

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

    /** Tạo definition tạm cho testsuite user rồi nạp toàn bộ testcase của suite từ database. */
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
        updateRequestMethodLabel(suite.getMethod());
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

    /** Ghép các testcase do user tạo vào danh sách scenario hệ thống đang hiển thị. */
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

    /** Chuyển entity testcase trong database thành model dùng bởi TableView và bộ chạy test. */
    private TestCaseRowModel toRowModel(UserTestCase testCase) {
        ApiTestScenario scenario = ApiTestScenario.builder()
                .scenario(testCase.getName())
                .description(testCase.getDescription())
                .setupRequests(testCase.getSetupRequests())
                .headers(testCase.getRequestHeaders())
                .requestBody(testCase.getRequestBody())
                .payloadAssertions(testCase.getPayloadAssertions())
                .expectedResponseBody(testCase.getExpectedResponseBody())
                .expectedCode(String.valueOf(testCase.getExpectedStatusCode()))
                .expectedStatus("User testcase")
                .build();

        String input = buildTestInput(
                testCase.getRequestBody(),
                testCase.getRequestHeaders(),
                testCase.getQueryParams(),
                testCase.getPathParams()
        );
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
                testCase.getCleanupRequests(),
                testCase.getQueryParams(),
                testCase.getPathParams()
        );
    }

    /** Hiển thị method, header và body của testcase đang chọn lên vùng chỉnh sửa request. */
    private void showRequestBody(TestCaseRowModel testCase) {
        String requestBody = testCase.getRequestBody();
        updateRequestMethodLabel(testCase.getMethod());
        headerTextArea.setText(formatHeaders(testCase.getHeaders()));
        bodyTextArea.setText(requestBody == null ? "" : requestBody);
    }

    /** Chuẩn hóa method và cập nhật nhãn method cùng màu nhận diện tương ứng. */
    private void updateRequestMethodLabel(String method) {
        if (requestMethodLabel == null) {
            return;
        }

        String normalizedMethod = method == null || method.isBlank() ? "POST" : method.trim().toUpperCase();
        requestMethodLabel.setText(normalizedMethod);
        requestMethodLabel.setStyle("-fx-background-color: " + methodColor(normalizedMethod)
                + "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 15;");
    }

    /** Trả về mã màu dùng để hiển thị từng HTTP method. */
    private String methodColor(String method) {
        return switch (method) {
            case "GET" -> "#2ecc71";
            case "POST" -> "#3498db";
            case "PUT", "PATCH" -> "#f39c12";
            case "DELETE" -> "#e74c3c";
            default -> "#7f8c8d";
        };
    }

    /** Chuẩn hóa và lưu base URL mới, cập nhật URL hiện tại rồi thông báo kết quả. */
    @FXML
    private void handleSaveRun() {
        try {
            mainBaseUrlField.setTooltip(new Tooltip("Thay đổi base_url tại đây"));
            String newBaseUrl = AppRunConfig.normalizeBaseUrl(mainBaseUrlField == null ? baseUrl : mainBaseUrlField.getText());
            baseUrl = newBaseUrl;

            if (mainBaseUrlField != null) {
                mainBaseUrlField.setText(baseUrl);
            }

            AppRunConfig.configure(baseUrl, AppRunConfig.getAlertMode());
            refreshCurrentTargetUrl();

            // Hiển thị dialog thông báo thành công - Cách 1: Dùng Alert trực tiếp
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText("Base URL đã được lưu");
            alert.setContentText(baseUrl);
            alert.showAndWait();

            // Hoặc cách 2: Dùng showInfo (bỏ comment dòng dưới và comment cách 1 nếu muốn dùng)
            // showInfo("Thành công", "Base URL đã được lưu:\n" + baseUrl);

        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Lỗi");
            errorAlert.setHeaderText("Không thể lưu Base URL");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }

    /** Chạy toàn bộ testcase đang hiển thị. */
    @FXML
    private void handleRunAll() {
        runTests(true);
    }

    /** Chỉ chạy các testcase đã được đánh dấu chọn. */
    @FXML
    private void handleRunSelected() {
        runTests(false);
    }

    /** Yêu cầu dừng vòng lặp test; cleanup vẫn được thực hiện sau khi vòng chạy kết thúc. */
    @FXML
    private void handleStop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        resultLogList.getItems().add("⏹ Đang dừng thực thi, cleanup data sẽ tiếp tục chạy nếu có.");
    }

    /** Mở form tạo testsuite, lưu suite và cleanup request vào database rồi chọn suite mới. */
    @FXML
    private void handleAddTestSuite() {
        try {
            Optional<TestSuiteDialogData> data = showTestSuiteDialog(
                    "Thêm testsuit",
                    "Nhập thông tin testsuit",
                    "",
                    "POST",
                    baseUrl,
                    sampleCleanupRequestsJson()
            );
            if (data.isEmpty()) return;

            List<ApiCleanupRequest> cleanupRequests = userTestCaseService.parseCleanupRequests(data.get().cleanupRequestsJson());
            UserTestSuite suite = userTestSuiteService.create(data.get().name(), data.get().method(), data.get().endpoint(), "");
            if (!cleanupRequests.isEmpty()) {
                suite = userTestSuiteService.updateCleanupRequests(suite.getId(), cleanupRequests);
            }
            initTreeView();
            selectUserSuite(suite.getId());
            showInfo("Đã thêm testsuit", "Testsuit đã được lưu vào database.");
        } catch (Exception e) {
            showInfo("Không thêm được testsuit", e.getMessage());
        }
    }

    /** Cập nhật thông tin và cleanup request của testsuite user đang chọn. */
    @FXML
    private void handleEditTestSuite() {
        if (currentUserSuite == null) {
            showInfo("Không thể sửa", "Chỉ có thể sửa testsuit do user tạo.");
            return;
        }

        try {
            Optional<TestSuiteDialogData> data = showTestSuiteDialog(
                    "Sửa testsuit",
                    "Cập nhật thông tin testsuit",
                    currentUserSuite.getName(),
                    currentUserSuite.getMethod(),
                    currentUserSuite.getEndpoint(),
                    currentUserSuite.getCleanupRequests().isEmpty()
                            ? sampleCleanupRequestsJson()
                            : userTestCaseService.toJson(currentUserSuite.getCleanupRequests())
            );
            if (data.isEmpty()) return;

            List<ApiCleanupRequest> cleanupRequests = userTestCaseService.parseCleanupRequests(data.get().cleanupRequestsJson());
            UserTestSuite updated = userTestSuiteService.update(
                    currentUserSuite.getId(),
                    data.get().name(),
                    data.get().method(),
                    data.get().endpoint(),
                    currentUserSuite.getDescription()
            );
            updated = userTestSuiteService.updateCleanupRequests(updated.getId(), cleanupRequests);
            initTreeView();
            selectUserSuite(updated.getId());
            showInfo("Đã sửa testsuit", "Testsuit đã được cập nhật.");
        } catch (Exception e) {
            showInfo("Không sửa được testsuit", e.getMessage());
        }
    }

    /** Xác nhận và xóa testsuite user đang chọn, sau đó làm mới cây Collections. */
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

    /** Chỉnh sửa danh sách cleanup request chạy một lần sau lượt thực thi testsuite. */
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

    /**
     * Thu thập cấu hình testcase từ dialog, parse setup/params/assertion,
     * lưu entity vào database và thêm dòng mới vào bảng.
     */
    @FXML
    private void handleAddTestCase() {
        if (currentDefinition == null) {
            showInfo("Chưa chọn API", "Hãy chọn một API trong cây Collections trước khi thêm testcase.");
            return;
        }

        try {
            Optional<TestCaseDialogData> data = showTestCaseDialog(
                    "Thêm testcase",
                    "Nhập thông tin testcase",
                    "",
                    defaultExpectedStatus(),
                    sampleSetupRequestsJson(),
                    sampleRequestParamsJson(),
                    samplePayloadAssertionsJson(),
                    ""
            );
            if (data.isEmpty()) {
                return;
            }

            int statusCode = Integer.parseInt(data.get().expectedStatusCode().trim());
            List<ApiSetupRequest> setupRequests = userTestCaseService.parseSetupRequests(data.get().setupRequestsJson());
            UserTestCaseService.RequestParams requestParams = userTestCaseService.parseRequestParams(data.get().requestParamsJson());
            List<ApiPayloadAssertion> payloadAssertions = userTestCaseService.parsePayloadAssertions(data.get().payloadAssertionsJson());

            UserTestCase saved = userTestCaseService.create(
                    currentDefinition.getApiLabel(),
                    currentUserSuite == null ? null : currentUserSuite.getId(),
                    data.get().name(),
                    "",
                    resolveApiMethod(currentDefinition),
                    resolveEndpointFromUrl(),
                    parseHeaders(headerTextArea.getText()),
                    requestParams.queryParams(),
                    requestParams.pathParams(),
                    bodyTextArea.getText(),
                    setupRequests,
                    List.of(),
                    payloadAssertions,
                    data.get().expectedResponseBody(),
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

    /** Nạp dữ liệu testcase user vào dialog, lưu thay đổi và tải lại selection hiện tại. */
    @FXML
    private void handleEditTestCase() {
        TestCaseRowModel selected = testCaseTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isUserCreated()) {
            showInfo("Không thể sửa", "Chỉ có thể sửa testcase do user tạo.");
            return;
        }

        try {
            Optional<TestCaseDialogData> data = showTestCaseDialog(
                    "Sửa testcase",
                    "Cập nhật thông tin testcase",
                    stripUserPrefix(selected.getName()),
                    selected.getExpected(),
                    userTestCaseService.toJson(selected.getScenario() == null ? List.of() : selected.getScenario().getSetupRequests()),
                    userTestCaseService.requestParamsToJson(selected.getQueryParams(), selected.getPathParams()),
                    userTestCaseService.payloadAssertionsToJson(selected.getScenario() == null
                            ? List.of()
                            : selected.getScenario().getPayloadAssertions()),
                    selected.getScenario() == null ? "" : selected.getScenario().getExpectedResponseBody()
            );
            if (data.isEmpty()) return;

            int statusCode = Integer.parseInt(data.get().expectedStatusCode().trim());
            List<ApiSetupRequest> setupRequests = userTestCaseService.parseSetupRequests(data.get().setupRequestsJson());
            UserTestCaseService.RequestParams requestParams = userTestCaseService.parseRequestParams(data.get().requestParamsJson());
            List<ApiPayloadAssertion> payloadAssertions = userTestCaseService.parsePayloadAssertions(data.get().payloadAssertionsJson());

            userTestCaseService.update(
                    selected.getUserTestCaseId(),
                    data.get().name(),
                    "",
                    selected.getMethod(),
                    resolveEndpointFromUrl(),
                    parseHeaders(headerTextArea.getText()),
                    requestParams.queryParams(),
                    requestParams.pathParams(),
                    bodyTextArea.getText(),
                    setupRequests,
                    List.of(),
                    payloadAssertions,
                    data.get().expectedResponseBody(),
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

    /** Xác nhận và xóa testcase user đang chọn khỏi database và TableView. */
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

    /** Lưu kết quả của lượt chạy gần nhất và đặt run vừa lưu làm run được chọn trong History. */
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
        showInfo("✅ Đã lưu báo cáo",
                "Mã run: " + shortId(runId) + "\n" +
                        "📁 Mở History để xem chi tiết");
    }

    /** Tạo và ghi đầy đủ dữ liệu lượt chạy xuống storage; trả về ID rỗng nếu thất bại. */
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

    /** Tổng hợp metadata, số liệu PASS/FAIL và kết quả chi tiết thành một {@link TestRun}. */
    private TestRun buildTestRunFromLastExecution() {
        String apiLabel = currentDefinition != null ? currentDefinition.getApiLabel() : "API Test";
        String testSuite = currentUserSuite != null ? currentUserSuite.getName() :
                (currentDefinition != null ? currentDefinition.getModuleName() : "User Test Suites");
        String runName = apiLabel + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        TestRun run = new TestRun();
        run.setRunName(runName);
        run.setTestSuite(testSuite);
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

    /**
     * Chạy testcase trên background thread để không khóa JavaFX UI. Mỗi testcase
     * lần lượt chạy auth setup, scenario setup, API chính và ghi nhận kết quả.
     * Khi kết thúc hoặc bị dừng, cleanup của scenario vẫn được gọi nếu đã có test chạy.
     *
     * @param all {@code true} để chạy tất cả; {@code false} để chỉ chạy dòng được chọn
     */
    private void runTests(boolean all) {
        if (isRunning || testData.isEmpty()) return;
        isRunning = true;
        setRunningUiState(true);
        lastRunWasAll = all;
        lastRunStartedAt = Instant.now();
        lastRunResults.clear();
        lastPassCount = 0;
        lastFailCount = 0;
        resultLogList.getItems().clear();
        ApiScenarioDefinition definitionToRun = currentDefinition;
        String stopConditionToRun = stopConditionCombo.getValue();

        Thread runnerThread = new Thread(() -> {
            int pass = 0, fail = 0;
            boolean executedAny = false;
            List<TestResult> collectedResults = new ArrayList<>();
            Map<String, String> cleanupRuntimeVariables = new LinkedHashMap<>();

            try {
                for (TestCaseRowModel tc : testData) {
                    if (!isRunning) break;
                    if (!all && !tc.isSelected()) continue;
                    executedAny = true;

                    Map<String, String> runtimeVariables = new LinkedHashMap<>();
                    Platform.runLater(() -> tc.setResult("⏳ Đang test..."));

                    boolean setupOk = runDefaultAuthSetupIfNeeded(tc, runtimeVariables) && runScenarioSetup(tc, runtimeVariables);
                    CaseOutcome outcome = setupOk ? callActualApi(tc, runtimeVariables) : CaseOutcome.failed("Setup thất bại");
                    cleanupRuntimeVariables.putAll(runtimeVariables);
                    runDefaultCleanupAuthSetupIfNeeded(definitionToRun, tc, cleanupRuntimeVariables);
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

                    if (!isPass && STOP_ON_FAIL_LABEL.equals(stopConditionToRun)) {
                        isRunning = false;
                        Platform.runLater(() -> resultLogList.getItems().add("⛔ DỪNG: Phát hiện lỗi tại " + tc.getName()));
                        break;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (executedAny) {
                    runScenarioCleanup(definitionToRun, cleanupRuntimeVariables);
                }
            } catch (Exception e) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: " + e.getMessage()));
            } finally {
                final int p = pass, f = fail;
                final List<TestResult> resultsSnapshot = new ArrayList<>(collectedResults);
                Platform.runLater(() -> finishRun(p, f, resultsSnapshot));
            }
        }, "api-test-runner");
        runnerThread.setDaemon(true);
        runnerThread.start();
    }

    /** Chốt thống kê lượt chạy, cập nhật UI, lưu snapshot kết quả và mở lại các nút thao tác. */
    private void finishRun(int pass, int fail, List<TestResult> resultsSnapshot) {
        lastRunResults.clear();
        lastRunResults.addAll(resultsSnapshot);
        lastPassCount = pass;
        lastFailCount = fail;
        isRunning = false;
        setRunningUiState(false);

        int total = pass + fail;
        if (total == 0) {
            summaryText.setText("Chưa chạy testcase nào");
        } else {
            summaryText.setText("Pass: " + pass + " | Fail: " + fail + " | Tổng: " + total
                    + " — Bấm «Lưu báo cáo» để ghi History");
        }
    }

    /** Khóa/mở các control và overlay theo trạng thái đang thực thi. */
    private void setRunningUiState(boolean running) {
        if (runningOverlay != null) {
            runningOverlay.setVisible(running);
            runningOverlay.setManaged(running);
        }
        if (testcaseContent != null) {
            testcaseContent.setDisable(running);
            testcaseContent.setEffect(running ? new GaussianBlur(8) : null);
        }
    }

    /**
     * Thay biến runtime trong URL, params, header, body và assertion; kiểm tra biến
     * còn thiếu; gọi API chính; sau đó so sánh response code và payload mong đợi.
     *
     * @return kết quả chuẩn hóa gồm PASS/FAIL, code, thời gian phản hồi và thông báo
     */
    private CaseOutcome callActualApi(TestCaseRowModel tc, Map<String, String> runtimeVariables) {
        long started = System.currentTimeMillis();
        try {
            String requestBody = replaceVariables(tc.getRequestBody(), runtimeVariables);
            if (hasUnresolvedVariables(requestBody)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Request body has unresolved variables: " + requestBody));
                return CaseOutcome.failed("Biến chưa được thay thế");
            }
            ApiTestScenario scenario = tc.getScenario();
            Map<String, List<String>> resolvedQueryParams = replaceQueryVariables(
                    tc.isUserCreated() ? tc.getQueryParams() : toMultiValueMap(scenario == null ? Map.of() : scenario.getQueryParams()),
                    runtimeVariables
            );
            Map<String, String> resolvedPathParams = replaceVariables(tc.getPathParams(), runtimeVariables);
            Map<String, String> headers = replaceVariables(tc.getHeaders(), runtimeVariables);
            if (hasUnresolvedQueryVariables(resolvedQueryParams)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Query params have unresolved variables: " + resolvedQueryParams));
                return CaseOutcome.failed("Biến query params chưa được thay thế");
            }
            if (hasUnresolvedVariables(headers)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Headers have unresolved variables: " + headers));
                return CaseOutcome.failed("Biến header chưa được thay thế");
            }
            if (hasUnresolvedVariables(resolvedPathParams)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Path params have unresolved variables: " + resolvedPathParams));
                return CaseOutcome.failed("Biến path params chưa được thay thế");
            }
            String targetUrl = baseUrlField.getText().trim();
            if (targetUrl.isEmpty()) {
                targetUrl = tc.getEndpoint();
            }
            Map<String, List<String>> queryParams = targetUrl.contains("?") ? Map.of() : resolvedQueryParams;

            String resolvedTargetUrl = replacePathParams(targetUrl, resolvedPathParams);
            if (hasUnresolvedPathParams(resolvedTargetUrl)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: URL has unresolved path params: " + resolvedTargetUrl));
                return CaseOutcome.failed("Path params chưa được thay thế");
            }
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
            List<ApiPayloadAssertion> resolvedAssertions = replaceAssertionVariables(
                    scenario == null ? List.of() : scenario.getPayloadAssertions(),
                    runtimeVariables
            );
            String resolvedExpectedResponse = replaceVariables(
                    scenario == null ? null : scenario.getExpectedResponseBody(),
                    runtimeVariables
            );
            if (hasUnresolvedAssertionVariables(resolvedAssertions) || hasUnresolvedVariables(resolvedExpectedResponse)) {
                Platform.runLater(() -> resultLogList.getItems().add("  Error: Expected response còn biến chưa được thay thế."));
                return CaseOutcome.failed("Biến expected response chưa được thay thế");
            }
            ApiPayloadAssertionEvaluator.Evaluation payloadEvaluation = payloadAssertionEvaluator.evaluate(
                    response.getResponseBody(),
                    resolvedAssertions,
                    resolvedExpectedResponse
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

    /** Chuyển kết quả nội bộ của một case thành DTO dùng để lưu báo cáo. */
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

    /** Hiển thị hộp thoại thông tin đồng bộ trên JavaFX UI. */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Tạo dialog nhập tên, method, endpoint và cleanup JSON của testsuite.
     * Chỉ trả dữ liệu khi người dùng bấm OK và các trường bắt buộc không rỗng.
     */
    private Optional<TestSuiteDialogData> showTestSuiteDialog(String title,
                                                              String header,
                                                              String defaultName,
                                                              String defaultMethod,
                                                              String defaultEndpoint,
                                                              String defaultCleanupRequests) {
        Dialog<TestSuiteDialogData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(defaultName == null ? "" : defaultName);
        nameField.setPromptText("Tên testsuit");
        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("GET", "POST", "PUT", "PATCH", "DELETE");
        methodCombo.setEditable(true);
        methodCombo.setValue(defaultMethod == null || defaultMethod.isBlank() ? "POST" : defaultMethod.trim().toUpperCase());
        TextField endpointField = new TextField(defaultEndpoint == null ? "" : defaultEndpoint);
        endpointField.setPromptText("/api/v1/resource hoặc https://...");
        TextArea cleanupRequestsArea = jsonTextArea(defaultCleanupRequests, 260);

//      tooltip
        nameField.setTooltip(new Tooltip("Điền tên Testsuit vào đây"));
        methodCombo.setTooltip(new Tooltip("Chọn method cho Testsuit"));
        endpointField.setTooltip(new Tooltip("Điền endpoint cho Testsuit"));
        cleanupRequestsArea.setTooltip(new Tooltip("Điền mẫu cleanup request vào đây"));

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(new Label("Tên suit"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Method"), 0, 1);
        form.add(methodCombo, 1, 1);
        form.add(new Label("Endpoint/URL"), 0, 2);
        form.add(endpointField, 1, 2);

        VBox content = new VBox(10, form, new Label("Cleanup request JSON array"), cleanupRequestsArea);
        content.setPadding(new Insets(8));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(780);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            return new TestSuiteDialogData(
                    nameField.getText() == null ? "" : nameField.getText().trim(),
                    methodCombo.getValue() == null ? "" : methodCombo.getValue().trim(),
                    endpointField.getText() == null ? "" : endpointField.getText().trim(),
                    cleanupRequestsArea.getText() == null ? "" : cleanupRequestsArea.getText()
            );
        });
        return dialog.showAndWait().filter(data ->
                !data.name().isBlank() && !data.method().isBlank() && !data.endpoint().isBlank());
    }

    /**
     * Tạo dialog cấu hình testcase gồm status mong đợi, setup request, params,
     * payload assertion và response body mong đợi.
     */
    private Optional<TestCaseDialogData> showTestCaseDialog(String title,
                                                            String header,
                                                            String defaultName,
                                                            String defaultExpectedStatus,
                                                            String defaultSetupRequests,
                                                            String defaultRequestParams,
                                                            String defaultPayloadAssertions,
                                                            String defaultExpectedResponseBody) {
        Dialog<TestCaseDialogData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(defaultName == null ? "" : defaultName);
        nameField.setPromptText("Tên testcase");
        TextField expectedStatusField = new TextField(defaultExpectedStatus == null || defaultExpectedStatus.isBlank()
                ? "200"
                : defaultExpectedStatus);
        expectedStatusField.setPromptText("200");

        TextArea setupRequestsArea = jsonTextArea(defaultSetupRequests, 220);
        TextArea requestParamsArea = jsonTextArea(defaultRequestParams, 150);
        TextArea payloadAssertionsArea = jsonTextArea(defaultPayloadAssertions, 180);
        TextArea expectedResponseBodyArea = jsonTextArea(defaultExpectedResponseBody, 140);
        expectedResponseBodyArea.setPromptText("Để trống nếu không cần so sánh toàn bộ response");
//        tooltip
        nameField.setTooltip(new Tooltip("Nhập tên testcase vào đây"));
        expectedStatusField.setTooltip(new Tooltip("Nhập status mong đợi vào đây"));
        setupRequestsArea.setTooltip(new Tooltip("Nhập setup request vào đây"));
        requestParamsArea.setTooltip(new Tooltip("Nhập setup param vào đây"));
        payloadAssertionsArea.setTooltip(new Tooltip("Nhập so sánh payload vào đây"));
        expectedResponseBodyArea.setTooltip(new Tooltip("Nhập tên testcase vào đây"));

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(new Label("Tên testcase"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Status code mong đợi"), 0, 1);
        form.add(expectedStatusField, 1, 1);

        VBox content = new VBox(
                10,
                form,
                new Label("Setup request JSON array"),
                setupRequestsArea,
                new Label("Params JSON object"),
                requestParamsArea,
                new Label("Path assertion JSON array"),
                payloadAssertionsArea,
                new Label("Expected full response JSON"),
                expectedResponseBodyArea
        );
        content.setPadding(new Insets(8));
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(720);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(820);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            return new TestCaseDialogData(
                    nameField.getText() == null ? "" : nameField.getText().trim(),
                    expectedStatusField.getText() == null ? "" : expectedStatusField.getText().trim(),
                    setupRequestsArea.getText() == null ? "" : setupRequestsArea.getText(),
                    requestParamsArea.getText() == null ? "" : requestParamsArea.getText(),
                    payloadAssertionsArea.getText() == null ? "" : payloadAssertionsArea.getText(),
                    expectedResponseBodyArea.getText() == null ? "" : expectedResponseBodyArea.getText()
            );
        });
        return dialog.showAndWait().filter(data ->
                !data.name().isBlank() && !data.expectedStatusCode().isBlank());
    }

    /** Tạo TextArea định dạng monospace dùng để nhập JSON. */
    private TextArea jsonTextArea(String defaultValue, double height) {
        TextArea textArea = new TextArea(defaultValue == null ? "" : defaultValue);
        textArea.setPrefWidth(720);
        textArea.setPrefHeight(height);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Courier New';");
        return textArea;
    }

    /** Mở hộp nhập chuỗi bắt buộc với giá trị mặc định rỗng. */
    private Optional<String> promptText(String title, String header, String content) {
        return promptText(title, header, content, "");
    }

    /** Mở hộp nhập chuỗi và loại bỏ kết quả rỗng sau khi trim. */
    private Optional<String> promptText(String title, String header, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue == null ? "" : defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait().map(String::trim).filter(value -> !value.isBlank());
    }

    /** Mở dialog nhập JSON cho setup/cleanup hook; trả chuỗi rỗng khi hủy. */
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

    /** Mở dialog nhập params JSON và parse thành query params cùng path params. */
    private Optional<UserTestCaseService.RequestParams> promptRequestParams(String title,
                                                                            String header,
                                                                            String defaultValue) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea textArea = new TextArea(defaultValue == null ? "" : defaultValue);
        textArea.setPrefWidth(700);
        textArea.setPrefHeight(240);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Courier New';");
        dialog.getDialogPane().setContent(textArea);
        dialog.setResultConverter(button -> button == ButtonType.OK ? textArea.getText() : null);
        return dialog.showAndWait().map(userTestCaseService::parseRequestParams);
    }

    /** Mở dialog nhập JSON tùy chọn và giữ nguyên nội dung người dùng nhập. */
    private Optional<String> promptOptionalJson(String title, String header, String defaultValue, double height) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea textArea = new TextArea(defaultValue == null ? "" : defaultValue);
        textArea.setPrefWidth(700);
        textArea.setPrefHeight(height);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Courier New';");
        dialog.getDialogPane().setContent(textArea);
        dialog.setResultConverter(button -> button == ButtonType.OK ? textArea.getText() : null);
        return dialog.showAndWait();
    }

    /** Trả JSON mẫu giúp người dùng cấu hình setup request và capture biến response. */
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

    /** Trả JSON mẫu cho cleanup request chạy sau lượt test. */
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

    /** Trả JSON mẫu cho query params và path params. */
    private String sampleRequestParamsJson() {
        return """
                {
                  "queryParams": {
                    "filter": "active",
                    "tag": ["indoor", "priority"]
                  },
                  "pathParams": {
                    "id": "123"
                  }
                }
                """;
    }

    /** Trả JSON mẫu cho các phép kiểm tra payload response. */
    private String samplePayloadAssertionsJson() {
        return """
                [
                  {
                    "jsonPath": "data.id",
                    "operator": "EQUALS",
                    "expectedValue": "${mapId}"
                  },
                  {
                    "jsonPath": "data.items",
                    "operator": "ARRAY_LENGTH",
                    "expectedValue": "1"
                  }
                ]
                """;
    }

    /** Hiển thị hộp xác nhận và chỉ trả về true khi người dùng bấm OK. */
    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    /** Nạp lại testsuite hoặc definition hiện tại sau khi dữ liệu được chỉnh sửa. */
    private void reloadCurrentSelection() {
        if (currentUserSuite != null) {
            loadUserSuite(currentUserSuite);
        } else if (currentDefinition != null) {
            loadScenarioDefinition(currentDefinition);
        }
    }

    /** Tìm node theo suite ID và chọn node đó trên TreeView. */
    private void selectUserSuite(String suiteId) {
        for (Map.Entry<TreeItem<String>, UserTestSuite> entry : userSuiteNodes.entrySet()) {
            if (entry.getValue().getId().equals(suiteId)) {
                testSuiteTree.getSelectionModel().select(entry.getKey());
                return;
            }
        }
    }

    /** Loại bỏ tiền tố hiển thị "[User] " trước khi đưa tên vào form chỉnh sửa. */
    private String stripUserPrefix(String name) {
        if (name == null) {
            return "";
        }
        return name.startsWith("[User] ") ? name.substring("[User] ".length()) : name;
    }

    /**
     * Chạy tuần tự các cleanup request sau lượt test, thay biến runtime trong body
     * và header, rồi ghi log PASS/FAIL. Cleanup bắt buộc thất bại chỉ được ghi log.
     */
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

    /**
     * Chạy các setup request trước API chính. Setup bắt buộc thất bại hoặc không
     * capture đủ response variable sẽ ngăn testcase tiếp tục.
     */
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

    /**
     * Tạo tài khoản test ngẫu nhiên, đăng nhập, lấy token và lưu các giá trị auth
     * vào runtimeVariables để các request sau có thể dùng placeholder.
     */
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

    /** Chỉ chạy auth setup mặc định khi testcase có sử dụng token hoặc authorizationHeader. */
    private boolean runDefaultAuthSetupIfNeeded(TestCaseRowModel tc, Map<String, String> runtimeVariables) {
        if (!usesAuthToken(tc)) {
            return true;
        }
        return runDefaultAuthSetup(tc, runtimeVariables);
    }

    /** Đảm bảo cleanup có token khi cleanup request dùng biến auth mà lượt test chưa tạo token. */
    private void runDefaultCleanupAuthSetupIfNeeded(ApiScenarioDefinition definition,
                                                    TestCaseRowModel tc,
                                                    Map<String, String> runtimeVariables) {
        if (!cleanupUsesAuthToken(definition) || runtimeVariables.containsKey("token")) {
            return;
        }

        if (!runDefaultAuthSetup(tc, runtimeVariables)) {
            Platform.runLater(() -> resultLogList.getItems().add("  Cleanup auth setup: FAIL"));
        }
    }

    /** Kiểm tra endpoint, body và header của cleanup có tham chiếu biến auth hay không. */
    private boolean cleanupUsesAuthToken(ApiScenarioDefinition definition) {
        return definition != null && definition.getCleanupRequests().stream().anyMatch(cleanupRequest ->
                containsAuthToken(cleanupRequest.getEndpoint())
                        || containsAuthToken(cleanupRequest.getRequestBody())
                        || containsAuthToken(cleanupRequest.getHeaders()));
    }

    /** Kiểm tra toàn bộ request và setup request của testcase có dùng biến auth hay không. */
    private boolean usesAuthToken(TestCaseRowModel tc) {
        if (tc == null) {
            return false;
        }

        ApiTestScenario scenario = tc.getScenario();
        return containsAuthToken(tc.getRequestBody())
                || containsAuthToken(tc.getHeaders())
                || containsAuthTokenValues(tc.getQueryParams())
                || containsAuthToken(tc.getPathParams())
                || containsAuthToken(scenario == null ? Map.of() : scenario.getQueryParams())
                || (scenario != null && scenario.getSetupRequests().stream().anyMatch(setupRequest ->
                containsAuthToken(setupRequest.getEndpoint())
                        || containsAuthToken(setupRequest.getRequestBody())
                        || containsAuthToken(setupRequest.getHeaders())));
    }

    /** Kiểm tra chuỗi có placeholder token hoặc authorizationHeader. */
    private boolean containsAuthToken(String value) {
        return value != null
                && (value.contains("${token}") || value.contains("${authorizationHeader}"));
    }

    /** Kiểm tra có giá trị nào trong map chứa placeholder auth. */
    private boolean containsAuthToken(Map<String, String> values) {
        return values != null && values.values().stream().anyMatch(this::containsAuthToken);
    }

    /** Sinh số điện thoại test ngẫu nhiên theo định dạng 10 chữ số bắt đầu bằng 032. */
    private String buildAuthPhoneNumber() {
        long suffix = Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10_000_000L;
        return "032" + String.format("%07d", suffix);
    }

    /**
     * Chuyển endpoint setup/cleanup thành URL đầy đủ dựa trên URL đang chạy.
     * URL tuyệt đối được giữ nguyên; nếu không suy ra được base URL thì trả endpoint gốc.
     */
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

    /** Lấy base URL trên giao diện hoặc cấu hình ứng dụng rồi chuẩn hóa định dạng. */
    private String resolveConfiguredBaseUrl() {
        if (mainBaseUrlField != null && mainBaseUrlField.getText() != null && !mainBaseUrlField.getText().isBlank()) {
            return AppRunConfig.normalizeBaseUrl(mainBaseUrlField.getText());
        }
        return AppRunConfig.normalizeBaseUrl(AppRunConfig.getBaseUrl());
    }

    /** Ghép base URL cấu hình với endpoint tương đối; URL tuyệt đối được giữ nguyên. */
    private String resolveScenarioTargetUrl(String endpoint) {
        if (endpoint != null && (endpoint.startsWith("http://") || endpoint.startsWith("https://"))) {
            return endpoint;
        }
        String configuredBaseUrl = resolveConfiguredBaseUrl();
        if (configuredBaseUrl.contains("?")) {
            return configuredBaseUrl;
        }
        return configuredBaseUrl + normalizeEndpoint(endpoint);
    }

    /** Tính lại URL đích theo definition hoặc testsuite đang chọn sau khi đổi base URL. */
    private void refreshCurrentTargetUrl() {
        if (currentDefinition != null) {
            baseUrlField.setText(resolveScenarioTargetUrl(currentDefinition.getEndpoint()));
        } else if (currentUserSuite != null) {
            baseUrlField.setText(resolveScenarioTargetUrl(currentUserSuite.getEndpoint()));
        } else {
            baseUrlField.setText(baseUrl);
        }
    }

    /** Chuẩn hóa endpoint tương đối để luôn bắt đầu bằng dấu gạch chéo. */
    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return "";
        }
        String trimmed = endpoint.trim();
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }

    /** Lấy HTTP method từ từ đầu tiên của API label; mặc định POST nếu không hợp lệ. */
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

    /** Tạo chuỗi input hiển thị từ body, header và query params. */
    private String buildTestInput(String requestBody, Map<String, String> headers, Map<String, ?> queryParams) {
        return buildTestInput(requestBody, headers, queryParams, Map.of());
    }

    /** Ghép các phần request có dữ liệu thành nội dung nhiều dòng hiển thị trong bảng. */
    private String buildTestInput(String requestBody,
                                  Map<String, String> headers,
                                  Map<String, ?> queryParams,
                                  Map<String, String> pathParams) {
        boolean hasRequestBody = requestBody != null && !requestBody.isBlank();
        boolean hasHeaders = headers != null && !headers.isEmpty();
        boolean hasQueryParams = queryParams != null && !queryParams.isEmpty();
        boolean hasPathParams = pathParams != null && !pathParams.isEmpty();
        List<String> inputParts = new ArrayList<>();

        if (hasHeaders) {
            inputParts.add("Headers: " + headers);
        }
        if (hasQueryParams) {
            inputParts.add("Query Params: " + queryParams);
        }
        if (hasPathParams) {
            inputParts.add("Path Params: " + pathParams);
        }
        if (hasRequestBody) {
            inputParts.add(requestBody);
        }
        return String.join("\n", inputParts);
    }

    /** Parse mỗi dòng "Key: Value" thành map header và báo lỗi nếu sai định dạng. */
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

    /** Lấy URL người dùng đang nhập; dùng endpoint definition nếu ô URL trống. */
    private String resolveEndpointFromUrl() {
        String targetUrl = baseUrlField.getText() == null ? "" : baseUrlField.getText().trim();
        if (targetUrl.isBlank()) {
            return currentDefinition.getEndpoint();
        }
        return targetUrl;
    }

    /** Lấy expected status ba chữ số của dòng đang chọn hoặc dùng 200 làm mặc định. */
    private String defaultExpectedStatus() {
        TestCaseRowModel selected = testCaseTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getExpected() != null && selected.getExpected().matches("\\d{3}")) {
            return selected.getExpected();
        }
        return "200";
    }

    /** Chuyển map header thành nhiều dòng "Key: Value" để hiển thị trong TextArea. */
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

    /**
     * Đọc các giá trị được khai báo trong responseVariables từ JSON response và
     * lưu vào runtimeVariables; trả false nếu thiếu bất kỳ biến nào.
     */
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

    /**
     * Duyệt JSON theo đường dẫn phân tách bởi dấu chấm; hỗ trợ property object và
     * chỉ số array. Trả Optional rỗng nếu JSON/path không hợp lệ hoặc không tồn tại.
     */
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

    /** Thay các placeholder dạng ${name} trong chuỗi bằng giá trị runtime tương ứng. */
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

    /** Thay placeholder trong toàn bộ giá trị của map và giữ nguyên key. */
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

    /** Thay placeholder trong từng giá trị của query param đa trị. */
    private Map<String, List<String>> replaceQueryVariables(Map<String, List<String>> values,
                                                            Map<String, String> runtimeVariables) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> resolvedValues = new LinkedHashMap<>();
        values.forEach((key, items) -> resolvedValues.put(
                key,
                items == null ? List.of() : items.stream()
                                            .map(value -> replaceVariables(value, runtimeVariables))
                                            .toList()
        ));
        return resolvedValues;
    }

    /** Tạo danh sách assertion mới với expected value đã được thay biến runtime. */
    private List<ApiPayloadAssertion> replaceAssertionVariables(List<ApiPayloadAssertion> assertions,
                                                                Map<String, String> runtimeVariables) {
        if (assertions == null || assertions.isEmpty()) {
            return List.of();
        }
        return assertions.stream()
                .map(assertion -> new ApiPayloadAssertion(
                        assertion.getJsonPath(),
                        assertion.getOperator(),
                        replaceVariables(assertion.getExpectedValue(), runtimeVariables)
                ))
                .toList();
    }

    /** Kiểm tra expected value của assertion còn placeholder chưa được thay hay không. */
    private boolean hasUnresolvedAssertionVariables(List<ApiPayloadAssertion> assertions) {
        return assertions != null && assertions.stream()
                .map(ApiPayloadAssertion::getExpectedValue)
                .anyMatch(this::hasUnresolvedVariables);
    }

    /** Chuyển map một giá trị thành map danh sách để dùng chung luồng query param. */
    private Map<String, List<String>> toMultiValueMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> multiValueMap = new LinkedHashMap<>();
        values.forEach((key, value) -> multiValueMap.put(key, List.of(value == null ? "" : value)));
        return multiValueMap;
    }

    /** Thay cả cú pháp {name} và ${name} trong URL bằng path param tương ứng. */
    private String replacePathParams(String targetUrl, Map<String, String> pathParams) {
        String resolvedUrl = targetUrl;
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            resolvedUrl = resolvedUrl.replace("{" + entry.getKey() + "}", value);
            resolvedUrl = resolvedUrl.replace("${" + entry.getKey() + "}", value);
        }
        return resolvedUrl;
    }

    /** Phát hiện path param dạng {name} hoặc ${name} còn sót lại trong URL. */
    private boolean hasUnresolvedPathParams(String targetUrl) {
        return targetUrl != null && targetUrl.matches("(?s).*(\\$\\{[A-Za-z0-9_]+}|\\{[A-Za-z0-9_]+}).*");
    }

    /** Phát hiện placeholder runtime dạng ${name} còn sót lại trong chuỗi. */
    private boolean hasUnresolvedVariables(String requestBody) {
        return requestBody != null && requestBody.matches("(?s).*\\$\\{[A-Za-z0-9_]+}.*");
    }

    /** Phát hiện placeholder runtime còn sót lại trong các giá trị của map. */
    private boolean hasUnresolvedVariables(Map<String, String> values) {
        return values != null && values.values().stream().anyMatch(this::hasUnresolvedVariables);
    }

    /** Phát hiện placeholder runtime còn sót lại trong query param đa trị. */
    private boolean hasUnresolvedQueryVariables(Map<String, List<String>> values) {
        return values != null && values.values().stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .anyMatch(this::hasUnresolvedVariables);
    }

    /** Kiểm tra query param đa trị có tham chiếu placeholder auth hay không. */
    private boolean containsAuthTokenValues(Map<String, List<String>> values) {
        return values != null && values.values().stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .anyMatch(this::containsAuthToken);
    }

    /** Dữ liệu được thu thập từ dialog tạo hoặc sửa testsuite. */
    private record TestSuiteDialogData(String name,
                                       String method,
                                       String endpoint,
                                       String cleanupRequestsJson) {
    }

    /** Dữ liệu được thu thập từ dialog tạo hoặc sửa testcase. */
    private record TestCaseDialogData(String name,
                                      String expectedStatusCode,
                                      String setupRequestsJson,
                                      String requestParamsJson,
                                      String payloadAssertionsJson,
                                      String expectedResponseBody) {
    }

    /** Kết quả nội bộ sau khi thực thi và đánh giá một testcase. */
    private static final class CaseOutcome {
        final boolean passed;
        final String expectedCodeText;
        final String actualCodeText;
        final long responseTimeMs;
        final String message;

        /** Khởi tạo kết quả với trạng thái, mã phản hồi, thời gian và thông báo chi tiết. */
        CaseOutcome(boolean passed, String expectedCodeText, String actualCodeText, long responseTimeMs, String message) {
            this.passed = passed;
            this.expectedCodeText = expectedCodeText;
            this.actualCodeText = actualCodeText;
            this.responseTimeMs = responseTimeMs;
            this.message = message;
        }

        /** Tạo kết quả thất bại mặc định khi chưa có response code hoặc thời gian phản hồi. */
        static CaseOutcome failed(String message) {
            return new CaseOutcome(false, "-", "-", 0, message);
        }
    }
}
