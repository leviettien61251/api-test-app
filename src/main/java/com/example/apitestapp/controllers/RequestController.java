package com.example.apitestapp.controllers;

import com.example.apitestapp.config.AppRunConfig;
import com.google.gson.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.*;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestController implements Initializable {

    private final OkHttpClient client = new OkHttpClient();
    // Kho lưu trữ biến dùng cho scripts (giống Environment Variables của Postman)
    private final Map<String, String> variables = new HashMap<>();

    @FXML private ComboBox<String> methodComboBox;
    @FXML private TextField urlField;
    @FXML private Button sendButton;
    @FXML private ComboBox<String> authTypeComboBox;
    @FXML private VBox authConfigContainer;
    @FXML private ToggleButton rawBtn;
    @FXML private ToggleButton formDataBtn;
    @FXML private ComboBox<String> rawTypeComboBox;
    @FXML private VBox bodyContentContainer;
    @FXML private TextArea bodyTextArea;
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    @FXML private TextArea responseBodyTextArea;
    @FXML private TextArea preRequestScriptTextArea;
    @FXML private TextArea testScriptTextArea;
    @FXML private VBox testsResultContainer;

    @FXML private VBox formDataContainer;
    @FXML private TableView<DataRowModel> formDataTableView;
    @FXML private TableColumn<DataRowModel, String> formKeyCol;
    @FXML private TableColumn<DataRowModel, String> formValueCol;
    @FXML private TableColumn<DataRowModel, String> formDescCol;
    @FXML private Button addFormRowBtn;
    @FXML private Button deleteFormRowBtn;

    @FXML private TableView<HeaderModel> headersTableView;
    @FXML private TableColumn<HeaderModel, String> headerKeyCol;
    @FXML private TableColumn<HeaderModel, String> headerValueCol;

    @FXML private TableView<DataRowModel> paramsTableView;
    @FXML private TableColumn<DataRowModel, String> paramKeyCol;
    @FXML private TableColumn<DataRowModel, String> paramValueCol;
    @FXML private TableColumn<DataRowModel, String> paramDescCol;
    @FXML private Button addParamBtn;
    @FXML private Button deleteParamBtn;

    @FXML private TableView<DataRowModel> requestHeadersTableView;
    @FXML private TableColumn<DataRowModel, String> reqHeaderKeyCol;
    @FXML private TableColumn<DataRowModel, String> reqHeaderValueCol;
    @FXML private TableColumn<DataRowModel, String> reqHeaderDescCol;
    @FXML private Button addHeaderBtn;
    @FXML private Button deleteHeaderBtn;

    private final ObservableList<DataRowModel> paramDataList = FXCollections.observableArrayList();
    private final ObservableList<DataRowModel> requestHeaderDataList = FXCollections.observableArrayList();
    private final ObservableList<DataRowModel> formDataList = FXCollections.observableArrayList();

    private boolean isUpdatingUrlFromTable = false;
    private boolean isUpdatingTableFromUrl = false;

    private final TextField authUserField = new TextField();
    private final PasswordField authPasswordField = new PasswordField();
    private final TextField authTokenField = new TextField();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGeneralControls();
        initDynamicTables();
        initUrlSyncLogic();
        initAuthTab();
        initBodyTab();
        initResponseTable();
        sendButton.setOnAction(e -> handleSendRequest());
    }

    private void initGeneralControls() {
        methodComboBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodComboBox.setValue("GET");
        urlField.setText("http://group3.it4788.sukkaito.id.vn/api/map/nodes?floor_id=1");

        preRequestScriptTextArea.setText(
                "// Ví dụ: set(\"token\", \"12345\");\n" +
                        "// Sau đó dùng {{token}} ở URL, Headers hoặc Body\n" +
                        "set(\"my_param\", \"1\");"
        );
        testScriptTextArea.setText(
                "// Cú pháp kiểm thử mẫu:\n" +
                        "assert status == 200 : \"Kiểm tra mã trạng thái HTTP 200\";\n" +
                        "assert body contains \"1000\" : \"Kiểm tra mã phản hồi hệ thống 1000\";\n" +
                        "// setFromResponse(\"new_id\", \"data.0.id\");"
        );
    }

    // ========== LOGIC XỬ LÝ SCRIPT & BIẾN SỐ ==========

    private String replaceVariables(String text) {
        if (text == null || text.isEmpty() || variables.isEmpty()) return text;
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, entry.getValue());
            }
        }
        return result;
    }

    private void runPreRequestScript() {
        String script = preRequestScriptTextArea.getText();
        if (script == null || script.isBlank()) return;

        String[] lines = script.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("//") || line.isEmpty()) continue;

            if (line.startsWith("set(")) {
                try {
                    Pattern p = Pattern.compile("set\\s*\\(\\s*\"([^\"]+)\"\\s*,\\s*\"([^\"]+)\"\\s*\\)");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        variables.put(m.group(1), m.group(2));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void runTestScripts(int statusCode, long duration, String responseBody) {
        Platform.runLater(() -> {
            String scriptText = testScriptTextArea.getText();
            JsonElement root = null;
            try {
                root = JsonParser.parseString(responseBody);
            } catch (Exception ignored) {}

            if (scriptText == null || scriptText.trim().isEmpty() || scriptText.contains("// Viết các hàm")) {
                renderTestResult(statusCode >= 200 && statusCode < 300, "Mặc định: HTTP Status " + statusCode);
                return;
            }

            String[] lines = scriptText.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("//") || line.isEmpty()) continue;

                if (line.startsWith("assert")) {
                    handleAssert(line, statusCode, duration, responseBody);
                } else if (line.startsWith("setFromResponse(")) {
                    handleSetFromResponse(line, root);
                }
            }
        });
    }

    private void handleAssert(String line, int statusCode, long duration, String responseBody) {
        try {
            boolean pass = false;
            String conditionText = line.substring(6, line.contains(":") ? line.indexOf(":") : line.length()).trim();
            String desc = line.contains(":") ? line.substring(line.indexOf(":") + 1).replace("\"", "").replace(";", "").trim() : conditionText;

            if (conditionText.contains("status == 200")) {
                pass = (statusCode == 200);
            } else if (conditionText.contains("duration <")) {
                int threshold = Integer.parseInt(conditionText.replaceAll("[^0-9]", ""));
                pass = (duration < threshold);
            } else if (conditionText.contains("body contains")) {
                String target = conditionText.substring(conditionText.indexOf("\"") + 1, conditionText.lastIndexOf("\""));
                pass = responseBody.contains(target);
            }
            renderTestResult(pass, desc);
        } catch (Exception ex) {
            renderTestResult(false, "Lỗi cú pháp Script: " + line);
        }
    }

    private void handleSetFromResponse(String line, JsonElement root) {
        try {
            Pattern p = Pattern.compile("setFromResponse\\s*\\(\\s*\"([^\"]+)\"\\s*,\\s*\"([^\"]+)\"\\s*\\)");
            Matcher m = p.matcher(line);
            if (m.find() && root != null) {
                String varName = m.group(1);
                String jsonPath = m.group(2);
                String value = findValueByPath(root, jsonPath);
                if (value != null) {
                    variables.put(varName, value);
                    renderTestResult(true, "[VAR] " + varName + " = " + value);
                } else {
                    renderTestResult(false, "Không tìm thấy đường dẫn JSON: " + jsonPath);
                }
            }
        } catch (Exception ex) {
            renderTestResult(false, "Lỗi trích xuất: " + ex.getMessage());
        }
    }

    private String findValueByPath(JsonElement root, String path) {
        try {
            JsonElement current = root;
            for (String part : path.split("\\.")) {
                if (current.isJsonObject()) {
                    current = current.getAsJsonObject().get(part);
                } else if (current.isJsonArray()) {
                    current = current.getAsJsonArray().get(Integer.parseInt(part));
                } else return null;
                if (current == null || current.isJsonNull()) return null;
            }
            return current.isJsonPrimitive() ? current.getAsString() : current.toString();
        } catch (Exception e) { return null; }
    }

    // ========== XỬ LÝ GỬI REQUEST ==========

    private void handleSendRequest() {
        runPreRequestScript();

        String rawUrl = urlField.getText();
        String processedUrl = replaceVariables(rawUrl);
        String finalUrl = resolveRequestUrl(processedUrl);

        if (finalUrl.isEmpty()) {
            statusLabel.setText("URL trống!");
            return;
        }

        String method = methodComboBox.getValue();
        String bodyText = replaceVariables(bodyTextArea.getText());
        MediaType mediaType = MediaType.parse(resolveMediaType());

        sendButton.setDisable(true);
        statusLabel.setText("Đang gửi...");
        responseBodyTextArea.clear();
        testsResultContainer.getChildren().clear();

        long startTime = System.currentTimeMillis();

        Task<Response> task = new Task<>() {
            @Override
            protected Response call() throws Exception {
                Request.Builder builder = new Request.Builder().url(finalUrl);

                for (DataRowModel headerRow : requestHeaderDataList) {
                    String k = replaceVariables(headerRow.getKey());
                    String v = replaceVariables(headerRow.getValue());
                    if (k != null && !k.trim().isEmpty()) {
                        builder.addHeader(k.trim(), v != null ? v.trim() : "");
                    }
                }

                String authType = authTypeComboBox.getValue();
                if ("Basic Auth".equals(authType)) {
                    String user = replaceVariables(authUserField.getText());
                    String pass = replaceVariables(authPasswordField.getText());
                    String credentials = user + ":" + pass;
                    String base64 = Base64.getEncoder().encodeToString(credentials.getBytes());
                    builder.header("Authorization", "Basic " + base64);
                } else if ("Bearer Token".equals(authType)) {
                    builder.header("Authorization", "Bearer " + replaceVariables(authTokenField.getText().trim()));
                }

                if (!method.equals("GET") && !method.equals("DELETE")) {
                    RequestBody requestBody;
                    if (formDataBtn.isSelected()) {
                        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                        boolean hasData = false;
                        for (DataRowModel row : formDataList) {
                            String k = replaceVariables(row.getKey());
                            String v = replaceVariables(row.getValue());
                            if (k != null && !k.trim().isEmpty()) {
                                multipartBuilder.addFormDataPart(k.trim(), v != null ? v.trim() : "");
                                hasData = true;
                            }
                        }
                        requestBody = hasData ? multipartBuilder.build() : RequestBody.create("", null);
                    } else {
                        requestBody = RequestBody.create(bodyText, mediaType);
                    }
                    builder.method(method, requestBody);
                } else {
                    builder.method(method, null);
                }

                return client.newCall(builder.build()).execute();
            }
        };

        task.setOnSucceeded(e -> {
            Response response = task.getValue();
            long duration = System.currentTimeMillis() - startTime;
            timeLabel.setText("Time: " + duration + "ms");
            updateStatus(response.code(), response.message());
            try {
                String respBody = response.body() != null ? response.body().string() : "";
                responseBodyTextArea.setText(formatJson(respBody));
                headersTableView.getItems().clear();
                response.headers().forEach(pair -> headersTableView.getItems().add(new HeaderModel(pair.getFirst(), pair.getSecond())));

                runTestScripts(response.code(), duration, respBody);
            } catch (IOException ex) {
                responseBodyTextArea.setText("Lỗi: " + ex.getMessage());
            } finally {
                response.close();
                sendButton.setDisable(false);
            }
        });

        task.setOnFailed(e -> {
            sendButton.setDisable(false);
            statusLabel.setText("Lỗi kết nối");
            responseBodyTextArea.setText(task.getException() != null ? task.getException().getMessage() : "Không rõ nguyên nhân");
        });

        new Thread(task).start();
    }

    // ========== CÁC HÀM TIỆN ÍCH UI ==========

    private void renderTestResult(boolean isPassed, String message) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(35.0);
        box.setSpacing(5.0);
        box.setStyle(isPassed ?
                "-fx-background-color: #f3faf5; -fx-background-radius: 5; -fx-padding: 0 10 0 10; -fx-border-color: #e6f4ea; -fx-border-radius: 5;" :
                "-fx-background-color: #fce8e6; -fx-background-radius: 5; -fx-padding: 0 10 0 10; -fx-border-color: #fad2cf; -fx-border-radius: 5;");

        Label symbol = new Label(isPassed ? "[PASS] " : "[FAIL] ");
        symbol.setStyle("-fx-text-fill: " + (isPassed ? "#1e8e3e" : "#d93025") + "; -fx-font-weight: bold;");
        Label desc = new Label(message);
        desc.setStyle("-fx-text-fill: " + (isPassed ? "#1e8e3e" : "#d93025") + ";");

        box.getChildren().addAll(symbol, desc);
        testsResultContainer.getChildren().add(box);
    }

    private void updateStatus(int code, String message) {
        statusLabel.setText(code + " " + message);
        statusLabel.setStyle("-fx-text-fill: " + (code >= 200 && code < 300 ? "#16a34a" : "#dc2626") + "; -fx-font-weight: bold;");
    }

    private String formatJson(String json) {
        if (json == null || json.isBlank()) return "";
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(JsonParser.parseString(json));
        } catch (Exception e) { return json; }
    }

    private String resolveRequestUrl(String url) {
        if (url == null || url.trim().isEmpty()) return "";
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
        String baseUrl = AppRunConfig.normalizeBaseUrl(AppRunConfig.getBaseUrl());
        return trimmed.startsWith("/") ? baseUrl + trimmed : baseUrl + "/" + trimmed;
    }

    private String resolveMediaType() {
        String rawType = rawTypeComboBox.getValue();
        if ("Text".equals(rawType)) return "text/plain; charset=utf-8";
        if ("XML".equals(rawType)) return "application/xml; charset=utf-8";
        return "application/json; charset=utf-8";
    }

    private void initAuthTab() {
        authTypeComboBox.getItems().addAll("No Auth", "Basic Auth", "Bearer Token");
        authTypeComboBox.setValue("No Auth");
        authTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateAuthUI(newVal));
        authUserField.setPromptText("Username");
        authPasswordField.setPromptText("Password");
        authTokenField.setPromptText("Bearer Token String");
    }

    private void updateAuthUI(String authType) {
        authConfigContainer.getChildren().clear();
        if ("Basic Auth".equals(authType)) {
            authConfigContainer.getChildren().addAll(new Label("Username"), authUserField, new Label("Password"), authPasswordField);
        } else if ("Bearer Token".equals(authType)) {
            authConfigContainer.getChildren().addAll(new Label("Token"), authTokenField);
        }
    }

    private void initBodyTab() {
        ToggleGroup group = new ToggleGroup();
        rawBtn.setToggleGroup(group);
        formDataBtn.setToggleGroup(group);
        rawBtn.setSelected(true);
        rawTypeComboBox.getItems().addAll("JSON", "Text", "XML");
        rawTypeComboBox.setValue("JSON");
        rawBtn.setOnAction(e -> toggleBodyMode(true));
        formDataBtn.setOnAction(e -> toggleBodyMode(false));
    }

    private void toggleBodyMode(boolean isRaw) {
        bodyTextArea.setVisible(isRaw); bodyTextArea.setManaged(isRaw);
        rawTypeComboBox.setVisible(isRaw); rawTypeComboBox.setManaged(isRaw);
        formDataContainer.setVisible(!isRaw); formDataContainer.setManaged(!isRaw);
    }

    private void initResponseTable() {
        headerKeyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        headerValueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue()));
    }

    private void initDynamicTables() {
        // --- Params Table ---
        paramsTableView.setItems(paramDataList);
        paramsTableView.setEditable(true);
        if (paramDataList.isEmpty()) paramDataList.add(new DataRowModel("", "", ""));
        setupEditableColumnWithPlaceholder(paramKeyCol, "key", "Key");
        setupEditableColumnWithPlaceholder(paramValueCol, "value", "Value");
        setupEditableColumnWithPlaceholder(paramDescCol, "description", "Mô tả");
        addParamBtn.setOnAction(e -> paramDataList.add(new DataRowModel("", "", "")));
        deleteParamBtn.setOnAction(e -> {
            DataRowModel s = paramsTableView.getSelectionModel().getSelectedItem();
            if (s != null) paramDataList.remove(s);
            if (paramDataList.isEmpty()) paramDataList.add(new DataRowModel("", "", ""));
            syncTableToUrl();
        });

        // --- Headers Table ---
        requestHeadersTableView.setItems(requestHeaderDataList);
        requestHeadersTableView.setEditable(true);
        if (requestHeaderDataList.isEmpty()) requestHeaderDataList.add(new DataRowModel("", "", ""));
        setupEditableColumnWithPlaceholder(reqHeaderKeyCol, "key", "Header");
        setupEditableColumnWithPlaceholder(reqHeaderValueCol, "value", "Value");
        setupEditableColumnWithPlaceholder(reqHeaderDescCol, "description", "Mô tả");
        addHeaderBtn.setOnAction(e -> requestHeaderDataList.add(new DataRowModel("", "", "")));
        deleteHeaderBtn.setOnAction(e -> {
            DataRowModel s = requestHeadersTableView.getSelectionModel().getSelectedItem();
            if (s != null) requestHeaderDataList.remove(s);
            if (requestHeaderDataList.isEmpty()) requestHeaderDataList.add(new DataRowModel("", "", ""));
        });

        // --- Form-Data Table ---
        formDataTableView.setItems(formDataList);
        formDataTableView.setEditable(true);
        if (formDataList.isEmpty()) formDataList.add(new DataRowModel("", "", ""));
        setupEditableColumnWithPlaceholder(formKeyCol, "key", "Field");
        setupEditableColumnWithPlaceholder(formValueCol, "value", "Value");
        setupEditableColumnWithPlaceholder(formDescCol, "description", "Mô tả");
        addFormRowBtn.setOnAction(e -> formDataList.add(new DataRowModel("", "", "")));
        deleteFormRowBtn.setOnAction(e -> {
            DataRowModel s = formDataTableView.getSelectionModel().getSelectedItem();
            if (s != null) formDataList.remove(s);
            if (formDataList.isEmpty()) formDataList.add(new DataRowModel("", "", ""));
        });
    }

    private void setupEditableColumnWithPlaceholder(TableColumn<DataRowModel, String> column, String field, String placeholder) {
        column.setCellFactory(col -> new TableCell<>() {
            private TextField textField;
            @Override public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    textField = new TextField(getItem());
                    textField.setOnAction(e -> commitEdit(textField.getText()));
                    textField.focusedProperty().addListener((o, ov, nv) -> { if (!nv) commitEdit(textField.getText()); });
                }
                textField.setText(getItem());
                setText(null); setGraphic(textField); textField.requestFocus();
            }
            @Override public void cancelEdit() { super.cancelEdit(); setText(getItem()); setGraphic(null); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    setText(item == null || item.isEmpty() ? placeholder : item);
                    setStyle(item == null || item.isEmpty() ? "-fx-text-fill: gray; -fx-font-style: italic;" : "-fx-text-fill: black; -fx-font-style: normal;");
                    setGraphic(null);
                }
            }
        });
        column.setCellValueFactory(data -> "key".equals(field) ? data.getValue().keyProperty() : "value".equals(field) ? data.getValue().valueProperty() : data.getValue().descriptionProperty());
        column.setOnEditCommit(event -> {
            DataRowModel row = event.getRowValue();
            String val = event.getNewValue();
            if ("key".equals(field)) {
                row.setKey(val);
                if (column.getTableView() == paramsTableView) syncTableToUrl();
            }
            else if ("value".equals(field)) {
                row.setValue(val);
                if (column.getTableView() == paramsTableView) syncTableToUrl();
            }
            else row.setDescription(val);
        });
    }

    private void initUrlSyncLogic() {
        urlField.textProperty().addListener((o, ov, nv) -> {
            if (!isUpdatingUrlFromTable) { isUpdatingTableFromUrl = true; parseUrlToTable(nv); isUpdatingTableFromUrl = false; }
        });
        parseUrlToTable(urlField.getText());
    }

    private void parseUrlToTable(String urlStr) {
        paramDataList.clear();
        if (urlStr == null || !urlStr.contains("?")) { paramDataList.add(new DataRowModel("", "", "")); return; }
        String query = urlStr.substring(urlStr.indexOf("?") + 1);
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length > 0 && !kv[0].isEmpty()) paramDataList.add(new DataRowModel(kv[0], kv.length > 1 ? kv[1] : "", ""));
        }
        if (paramDataList.isEmpty()) paramDataList.add(new DataRowModel("", "", ""));
    }

    private void syncTableToUrl() {
        if (isUpdatingTableFromUrl) return;
        isUpdatingUrlFromTable = true;
        String url = urlField.getText();
        if (url == null) url = "";
        if (url.contains("?")) url = url.substring(0, url.indexOf("?"));
        StringBuilder sb = new StringBuilder();
        for (DataRowModel r : paramDataList) {
            if (r.getKey() != null && !r.getKey().isEmpty()) {
                if (sb.length() > 0) sb.append("&");
                sb.append(r.getKey()).append("=").append(r.getValue());
            }
        }
        urlField.setText(sb.length() > 0 ? url + "?" + sb.toString() : url);
        isUpdatingUrlFromTable = false;
    }

    public static class DataRowModel {
        private final SimpleStringProperty key, value, description;
        public DataRowModel(String k, String v, String d) { this.key = new SimpleStringProperty(k); this.value = new SimpleStringProperty(v); this.description = new SimpleStringProperty(d); }
        public String getKey() { return key.get(); } public void setKey(String v) { key.set(v); } public SimpleStringProperty keyProperty() { return key; }
        public String getValue() { return value.get(); } public void setValue(String v) { value.set(v); } public SimpleStringProperty valueProperty() { return value; }
        public String getDescription() { return description.get(); } public void setDescription(String v) { description.set(v); } public SimpleStringProperty descriptionProperty() { return description; }
    }

    public static class HeaderModel {
        private final String key, value;
        public HeaderModel(String k, String v) { this.key = k; this.value = v; }
        public String getKey() { return key; } public String getValue() { return value; }
    }
}