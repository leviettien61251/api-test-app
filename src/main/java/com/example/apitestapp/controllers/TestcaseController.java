package com.example.apitestapp.controllers;


import javafx.application.Platform;
import javafx.beans.property.*;
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
import java.util.List;
import java.util.ResourceBundle;

public class TestcaseController implements Initializable {

    @FXML private TreeView<String> testSuiteTree;
    @FXML private TableView<TestCaseModel> testCaseTable;
    @FXML private TableColumn<TestCaseModel, Boolean> colCheck;
    @FXML private TableColumn<TestCaseModel, String> colName, colInput, colExpected, colStatus, colResult;

    @FXML private ComboBox<String> executionModeCombo, stopConditionCombo;
    @FXML private ListView<String> resultLogList;
    @FXML private Label summaryText;
    @FXML private Button runAllBtn, stopBtn;

    @FXML private TextField baseUrlField; // Thanh URL
    @FXML private TextArea bodyTextArea;   // Khung JSON Body

    private ObservableList<TestCaseModel> testData = FXCollections.observableArrayList();
    private volatile boolean isRunning = false;
    private ApiTestService apiTestService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBoxes();
        initTreeView();
        
        // Initialize API test service
        apiTestService = new ApiTestService();

        // Gán list dữ liệu vào bảng
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
        TreeItem<String> root = new TreeItem<>("Collections");
        TreeItem<String> authModule = new TreeItem<>("Auth Module");

        // Các API Key phải khớp với Key trong ScenarioDataService
        TreeItem<String> signupApi = new TreeItem<>("POST /api/v1/signup");
        TreeItem<String> loginApi = new TreeItem<>("POST /api/v1/login");

        authModule.getChildren().addAll(signupApi, loginApi);
        root.getChildren().add(authModule);
        testSuiteTree.setRoot(root);
        root.setExpanded(true);
        authModule.setExpanded(true);

        // Lắng nghe sự kiện chọn trên cây thư mục
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
        // 1. Dọn sạch bảng và log
        testData.clear();
        resultLogList.getItems().clear();
        summaryText.setText("Chưa có dữ liệu thực thi");

        // 2. Load test scenarios based on API selection
        if (apiName.contains("signup")) {
            List<SignupTestData> scenarios = SignupTestScenarios.getSignupScenarios();

            if (!scenarios.isEmpty()) {
                // Cập nhật Base URL - allow editing
                baseUrlField.setText("http://localhost:8080/api/v1/signup");
                baseUrlField.setEditable(true);

                // Nạp kịch bản vào bảng
                for (SignupTestData s : scenarios) {
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

            for (TestCaseModel tc : testData) {
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

                if (isPass) pass++; else fail++;

                if (!isPass && "Dừng ngay khi có FAIL".equals(stopCond)) {
                    isRunning = false;
                    Platform.runLater(() -> resultLogList.getItems().add("⛔ DỪNG: Phát hiện lỗi tại " + tc.getName()));
                    break;
                }

                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }

            final int p = pass, f = fail;
            Platform.runLater(() -> {
                summaryText.setText("Pass: " + p + " | Fail: " + f + " | Tổng: " + (p+f));
                isRunning = false;
            });
        }).start();
    }

    private boolean callActualApi(TestCaseModel tc) {
        try {
            String phone = tc.getPhone() != null ? tc.getPhone() : "";
            String password = tc.getPassword() != null ? tc.getPassword() : "";
            
            // Always build JSON from phone/password
            String requestBody = "{\"phoneNumber\":\"" + phone + "\",\"password\":\"" + password + "\"}";
            
            // Get endpoint from URL field
            String urlText = baseUrlField.getText().trim();
            String endpoint = "/api/v1/signup"; // default
            
            if (!urlText.isEmpty()) {
                if (urlText.startsWith("http")) {
                    // Extract path from full URL
                    int apiIndex = urlText.indexOf("/api");
                    if (apiIndex >= 0) {
                        endpoint = urlText.substring(apiIndex);
                    }
                } else {
                    endpoint = urlText;
                }
            }

            // Log what we're sending
            Platform.runLater(() -> resultLogList.getItems().add("  Sending: " + requestBody));

            ApiTestService.ApiResponse response = apiTestService.callApi(endpoint, requestBody);
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

    // --- Model with additional fields for API testing ---
    public static class TestCaseModel {
        private final BooleanProperty selected = new SimpleBooleanProperty(true);
        private final StringProperty name, input, expected, status, result;
        private String phone;
        private String password;

        public TestCaseModel(String name, String input, String expected) {
            this.name = new SimpleStringProperty(name);
            this.input = new SimpleStringProperty(input);
            this.expected = new SimpleStringProperty(expected);
            this.status = new SimpleStringProperty("-");
            this.result = new SimpleStringProperty("⚪ Chờ");
        }

        public TestCaseModel(String name, String input, String expected, String phone, String password) {
            this(name, input, expected);
            this.phone = phone;
            this.password = password;
        }

        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public StringProperty nameProperty() { return name; }
        public String getName() { return name.get(); }
        public StringProperty inputProperty() { return input; }
        public StringProperty expectedProperty() { return expected; }
        public String getExpected() { return expected.get(); }
        public StringProperty statusProperty() { return status; }
        public void setStatus(String s) { status.set(s); }
        public StringProperty resultProperty() { return result; }
        public void setResult(String r) { result.set(r); }
        public String getPhone() { return phone; }
        public String getPassword() { return password; }
    }
}