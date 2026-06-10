package com.example.apitestapp.controllers;

import com.example.apitestapp.config.AppRunConfig;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.*;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class RequestController implements Initializable {

    private final OkHttpClient client = new OkHttpClient();

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

    // --- Cấu hình cho giao diện Form-Data ---
    @FXML private VBox formDataContainer;
    @FXML private TableView<DataRowModel> formDataTableView;
    @FXML private TableColumn<DataRowModel, String> formKeyCol;
    @FXML private TableColumn<DataRowModel, String> formValueCol;
    @FXML private TableColumn<DataRowModel, String> formDescCol;
    @FXML private Button addFormRowBtn;
    @FXML private Button deleteFormRowBtn;

    // TableView hiển thị danh sách Response Header nhận về
    @FXML private TableView<HeaderModel> headersTableView;
    @FXML private TableColumn<HeaderModel, String> headerKeyCol;
    @FXML private TableColumn<HeaderModel, String> headerValueCol;

    // Cấu hình TableView động cho Params đầu vào
    @FXML private TableView<DataRowModel> paramsTableView;
    @FXML private TableColumn<DataRowModel, String> paramKeyCol;
    @FXML private TableColumn<DataRowModel, String> paramValueCol;
    @FXML private TableColumn<DataRowModel, String> paramDescCol;
    @FXML private Button addParamBtn;
    @FXML private Button deleteParamBtn;

    // Cấu hình TableView động cho Request Headers đầu vào
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

    // Node lưu giữ thông tin Auth tạm thời
    private TextField authUserField = new TextField();
    private PasswordField authPasswordField = new PasswordField();
    private TextField authTokenField = new TextField();

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

        // Cài đặt sẵn đoạn script mẫu để sinh viên dễ test thử nghiệm
        testScriptTextArea.setText(
                "// Cú pháp kiểm thử mẫu:\n" +
                        "assert status == 200 : \"Kiểm tra mã trạng thái HTTP 200\";\n" +
                        "assert body contains \"1000\" : \"Kiểm tra mã phản hồi hệ thống 1000\";"
        );
    }

    private void initResponseTable() {
        headerKeyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        headerValueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue()));
    }

    private void initDynamicTables() {
        // ========== CẤU HÌNH BẢNG PARAMS ==========
        paramsTableView.setItems(paramDataList);
        paramsTableView.setEditable(true);
        paramsTableView.setPlaceholder(new Label("Nhấn '+ Add Param' để thêm tham số"));

        // Thêm hàng trống mặc định để hiển thị khung nhập
        if (paramDataList.isEmpty()) {
            paramDataList.add(new DataRowModel("", "", ""));
        }

        setupEditableColumnWithPlaceholder(paramKeyCol, "key", "Nhập key...");
        setupEditableColumnWithPlaceholder(paramValueCol, "value", "Nhập value...");
        setupEditableColumnWithPlaceholder(paramDescCol, "description", "Mô tả (không bắt buộc)");

        addParamBtn.setOnAction(e -> {
            paramDataList.add(new DataRowModel("", "", ""));
            paramsTableView.scrollTo(paramDataList.size() - 1);
        });

        deleteParamBtn.setOnAction(e -> {
            DataRowModel selected = paramsTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                paramDataList.remove(selected);
                // Luôn giữ ít nhất 1 hàng trống
                if (paramDataList.isEmpty()) {
                    paramDataList.add(new DataRowModel("", "", ""));
                }
            }
        });

        // ========== CẤU HÌNH BẢNG REQUEST HEADERS ==========
        requestHeadersTableView.setItems(requestHeaderDataList);
        requestHeadersTableView.setEditable(true);
        requestHeadersTableView.setPlaceholder(new Label("Nhấn '+ Add Header' để thêm header"));

        if (requestHeaderDataList.isEmpty()) {
            requestHeaderDataList.add(new DataRowModel("", "", ""));
        }

        setupEditableColumnWithPlaceholder(reqHeaderKeyCol, "key", "Header name...");
        setupEditableColumnWithPlaceholder(reqHeaderValueCol, "value", "Header value...");
        setupEditableColumnWithPlaceholder(reqHeaderDescCol, "description", "Mô tả...");

        addHeaderBtn.setOnAction(e -> requestHeaderDataList.add(new DataRowModel("", "", "")));
        deleteHeaderBtn.setOnAction(e -> {
            DataRowModel selected = requestHeadersTableView.getSelectionModel().getSelectedItem();
            if (selected != null) requestHeaderDataList.remove(selected);
            if (requestHeaderDataList.isEmpty()) {
                requestHeaderDataList.add(new DataRowModel("", "", ""));
            }
        });

        // ========== CẤU HÌNH BẢNG FORM-DATA ==========
        formDataTableView.setItems(formDataList);
        formDataTableView.setEditable(true);
        formDataTableView.setPlaceholder(new Label("Nhấn '+ Add Form Data' để thêm field"));

        if (formDataList.isEmpty()) {
            formDataList.add(new DataRowModel("", "", ""));
        }

        setupEditableColumnWithPlaceholder(formKeyCol, "key", "Field name...");
        setupEditableColumnWithPlaceholder(formValueCol, "value", "Field value...");
        setupEditableColumnWithPlaceholder(formDescCol, "description", "Mô tả...");

        addFormRowBtn.setOnAction(e -> formDataList.add(new DataRowModel("", "", "")));
        deleteFormRowBtn.setOnAction(e -> {
            DataRowModel selected = formDataTableView.getSelectionModel().getSelectedItem();
            if (selected != null) formDataList.remove(selected);
            if (formDataList.isEmpty()) {
                formDataList.add(new DataRowModel("", "", ""));
            }
        });
    }

    /**
     * Cấu hình TableColumn với Placeholder và khả năng edit trực quan
     */
    private void setupEditableColumnWithPlaceholder(TableColumn<DataRowModel, String> column, String field, String placeholder) {
        column.setCellFactory(col -> new TableCell<DataRowModel, String>() {
            private TextField textField;

            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createTextField();
                    setText(null);
                    setGraphic(textField);
                    if (textField != null) {
                        textField.requestFocus();
                        textField.selectAll();
                    }
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItemDisplay());
                setGraphic(null);
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getItemDisplay());
                    setGraphic(null);
                }
            }

            private void createTextField() {
                textField = new TextField(getString());
                textField.setPromptText(placeholder);
                textField.setStyle("-fx-prompt-text-fill: #999999;");
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

                textField.setOnAction(e -> {
                    String newValue = textField.getText();
                    commitEdit(newValue);
                });

                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        String newValue = textField.getText();
                        commitEdit(newValue);
                    }
                });
            }

            private String getString() {
                return getItem() == null ? "" : getItem();
            }

            private String getItemDisplay() {
                String item = getItem();
                if (item == null || item.isEmpty()) {
                    return placeholder;
                }
                return item;
            }
        });

        column.setCellValueFactory(data -> {
            if ("key".equals(field)) return data.getValue().keyProperty();
            if ("value".equals(field)) return data.getValue().valueProperty();
            return data.getValue().descriptionProperty();
        });

        column.setOnEditCommit(event -> {
            DataRowModel row = event.getRowValue();
            String newValue = event.getNewValue() != null ? event.getNewValue() : "";

            if ("key".equals(field)) {
                row.setKey(newValue);
                // Khi key thay đổi ở Params, cập nhật URL
                if (column.getTableView() == paramsTableView) {
                    syncTableToUrl();
                }
            }
            if ("value".equals(field)) {
                row.setValue(newValue);
                if (column.getTableView() == paramsTableView) {
                    syncTableToUrl();
                }
            }
            if ("description".equals(field)) row.setDescription(newValue);

            column.getTableView().refresh();
        });
    }

    private void initUrlSyncLogic() {
        // Tự động phân tích URL điền xuống bảng Params
        urlField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdatingUrlFromTable) return;
            isUpdatingTableFromUrl = true;
            parseUrlToTable(newValue);
            isUpdatingTableFromUrl = false;
        });

        // Kích hoạt phân tích ngay từ URL mặc định ban đầu
        parseUrlToTable(urlField.getText());
    }

    private void parseUrlToTable(String urlStr) {
        paramDataList.clear();

        if (urlStr == null || !urlStr.contains("?")) {
            // Nếu không có params, thêm 1 hàng trống
            paramDataList.add(new DataRowModel("", "", ""));
            return;
        }

        try {
            String queryString = urlStr.substring(urlStr.indexOf("?") + 1);
            String[] pairs = queryString.split("&");
            boolean hasParams = false;

            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                String key = kv.length > 0 ? kv[0] : "";
                String value = kv.length > 1 ? kv[1] : "";
                if (!key.isEmpty()) {
                    paramDataList.add(new DataRowModel(key, value, ""));
                    hasParams = true;
                }
            }

            // Nếu không có params nào, thêm 1 hàng trống
            if (!hasParams) {
                paramDataList.add(new DataRowModel("", "", ""));
            }
        } catch (Exception ignored) {
            paramDataList.add(new DataRowModel("", "", ""));
        }
    }

    private void syncTableToUrl() {
        if (isUpdatingTableFromUrl) return;
        isUpdatingUrlFromTable = true;

        String currentUrl = urlField.getText();
        if (currentUrl == null) currentUrl = "";

        // Xóa phần query cũ nếu có
        if (currentUrl.contains("?")) {
            currentUrl = currentUrl.substring(0, currentUrl.indexOf("?"));
        }

        // Xây dựng query string từ các row có key không rỗng
        StringBuilder queryBuilder = new StringBuilder();
        for (DataRowModel row : paramDataList) {
            if (row.getKey() != null && !row.getKey().trim().isEmpty()) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                String value = row.getValue() != null ? row.getValue().trim() : "";
                queryBuilder.append(row.getKey().trim()).append("=").append(value);
            }
        }

        if (queryBuilder.length() > 0) {
            urlField.setText(currentUrl + "?" + queryBuilder.toString());
        } else {
            urlField.setText(currentUrl);
        }

        isUpdatingUrlFromTable = false;
    }

    private void handleSendRequest() {
        String requestUrl = resolveRequestUrl();
        if (requestUrl.isEmpty()) {
            statusLabel.setText("URL trống!");
            return;
        }

        String method = methodComboBox.getValue();
        String bodyText = bodyTextArea.getText();
        MediaType mediaType = MediaType.parse(resolveMediaType());

        sendButton.setDisable(true);
        statusLabel.setText("Đang gửi...");
        responseBodyTextArea.clear();
        testsResultContainer.getChildren().clear();

        long startTime = System.currentTimeMillis();

        Task<Response> task = new Task<>() {
            @Override
            protected Response call() throws Exception {
                Request.Builder builder = new Request.Builder().url(requestUrl);

                // Nạp tất cả các custom header từ bảng dữ liệu đầu vào của Tab Headers
                for (DataRowModel headerRow : requestHeaderDataList) {
                    if (headerRow.getKey() != null && !headerRow.getKey().trim().isEmpty()) {
                        builder.addHeader(headerRow.getKey().trim(),
                                headerRow.getValue() != null ? headerRow.getValue().trim() : "");
                    }
                }

                // Xử lý chèn tự động Auth tùy thuộc loại chọn lựa
                String authType = authTypeComboBox.getValue();
                if ("Basic Auth".equals(authType)) {
                    String credentials = authUserField.getText() + ":" + authPasswordField.getText();
                    String base64 = Base64.getEncoder().encodeToString(credentials.getBytes());
                    builder.header("Authorization", "Basic " + base64);
                } else if ("Bearer Token".equals(authType)) {
                    builder.header("Authorization", "Bearer " + authTokenField.getText().trim());
                }

                // XỬ LÝ BODY CHUYỂN ĐỔI LINH HOẠT GIỮA RAW VÀ FORM-DATA
                if (!method.equals("GET") && !method.equals("DELETE")) {
                    RequestBody requestBody;

                    if (formDataBtn.isSelected()) {
                        // Cấu hình Multipart Form-Data
                        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM);

                        boolean hasData = false;
                        for (DataRowModel row : formDataList) {
                            if (row.getKey() != null && !row.getKey().trim().isEmpty()) {
                                String value = row.getValue() != null ? row.getValue().trim() : "";
                                multipartBuilder.addFormDataPart(row.getKey().trim(), value);
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
                response.headers().forEach(pair -> {
                    headersTableView.getItems().add(new HeaderModel(pair.getFirst(), pair.getSecond()));
                });

                // Kích hoạt trình thông dịch chạy Script kiểm thử tự động
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
            statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            responseBodyTextArea.setText(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void runTestScripts(int statusCode, long duration, String responseBody) {
        Platform.runLater(() -> {
            String scriptText = testScriptTextArea.getText();
            if (scriptText == null || scriptText.trim().isEmpty() || scriptText.contains("// Viết các hàm")) {
                String method = methodComboBox.getValue();
                boolean pass = false;
                String message = "";

                switch (method) {
                    case "GET":
                        pass = (statusCode == 200);
                        message = "GET thành công (200 OK)";
                        break;
                    case "POST":
                        pass = (statusCode == 201);
                        message = "POST thành công (201 Created)";
                        break;
                    case "PUT":
                    case "PATCH":
                        pass = (statusCode == 200 || statusCode == 204);
                        message = "PUT/PATCH thành công (200/204)";
                        break;
                    case "DELETE":
                        pass = (statusCode == 204);
                        message = "DELETE thành công (204 No Content)";
                        break;
                    default:
                        pass = (statusCode >= 200 && statusCode < 300);
                        message = "Mặc định: HTTP Status trả về thành công (2xx)";
                }

                renderTestResult(pass, message);
                return;
            }

            // Phân tích cú pháp Script đơn giản dòng lệnh assert từ người dùng nhập
            String[] lines = scriptText.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("assert")) {
                    try {
                        boolean pass = false;
                        String conditionText = line.substring(6, line.contains(":") ? line.indexOf(":") : line.length()).trim();
                        String desc = line.contains(":") ? line.substring(line.indexOf(":") + 1).replace("\"", "").replace(";", "").trim() : conditionText;

                        if (conditionText.contains("status == 200")) {
                            String method = methodComboBox.getValue();

                            if ("POST".equals(method)) {
                                if (statusCode == 201 || statusCode == 200) {
                                    pass = true;
                                    desc = statusCode == 201 ? "POST thành công, tạo mới resource (201 Created)" :
                                            "POST thành công (200 OK)";
                                } else if (statusCode == 202) {
                                    pass = true;
                                    desc = "POST được chấp nhận, xử lý bất đồng bộ (202 Accepted)";
                                } else if (statusCode == 204) {
                                    pass = true;
                                    desc = "POST thành công, không trả về nội dung (204 No Content)";
                                } else {
                                    pass = false;
                                    desc = "POST thất bại (không phải 200/201/202/204)";
                                }
                            }
                            else if ("DELETE".equals(method)) {
                                if (statusCode == 204 || statusCode == 200 || statusCode == 202) {
                                    pass = true;
                                    desc = statusCode == 204 ? "DELETE thành công (204 No Content)" :
                                            statusCode == 200 ? "DELETE thành công (200 OK)" :
                                            "DELETE được chấp nhận, xóa bất đồng bộ (202 Accepted)";
                                } else {
                                    pass = false;
                                    desc = "DELETE thất bại (không phải 200/202/204)";
                                }
                            }
                            else if ("PUT".equals(method) || "PATCH".equals(method)) {
                                if (statusCode == 200 || statusCode == 202 || statusCode == 204) {
                                    pass = true;
                                    desc = "PUT/PATCH thành công: " +
                                            (statusCode == 200 ? "200 OK (có dữ liệu trả về)" :
                                                    statusCode == 202 ? "202 Accepted (bất đồng bộ)" :
                                                    "204 No Content (không trả về dữ liệu)");
                                } else {
                                    pass = false;
                                    desc = "PUT/PATCH thất bại (không phải 200/202/204)";
                                }
                            }
                            else if ("GET".equals(method)) {
                                if (statusCode == 200 || statusCode == 206) {
                                    pass = true;
                                    desc = statusCode == 200 ? "GET thành công (200 OK)" : "GET partial content (206)";
                                } else {
                                    pass = false;
                                    desc = "GET thất bại (không phải 200/206)";
                                }
                            }
                            else {
                                if (statusCode == 200 || statusCode == 202 || statusCode == 204) {
                                    pass = true;
                                    desc = "Thành công (" + statusCode + ")";
                                } else {
                                    pass = false;
                                    desc = "Thất bại (không phải 2xx thành công)";
                                }
                            }
                        }
                        else if (conditionText.contains("duration < 500")) {
                            pass = (duration < 500);
                        } else if (conditionText.contains("body contains")) {
                            String target = conditionText.substring(conditionText.indexOf("\"") + 1, conditionText.lastIndexOf("\""));
                            pass = responseBody.contains(target);
                        }
                        renderTestResult(pass, desc);
                    } catch (Exception ex) {
                        renderTestResult(false, "Lỗi cú pháp Script: " + line);
                    }
                }
            }
        });
    }

    private void renderTestResult(boolean isPassed, String message) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(35.0);
        box.setSpacing(5.0);

        Label statusSymbol = new Label(isPassed ? "[PASS] " : "[FAIL] ");
        Label statusDesc = new Label(message);

        if (isPassed) {
            box.setStyle("-fx-background-color: #f3faf5; -fx-background-radius: 5; -fx-padding: 0 10 0 10; -fx-border-color: #e6f4ea; -fx-border-radius: 5;");
            statusSymbol.setStyle("-fx-text-fill: #1e8e3e; -fx-font-weight: bold;");
            statusDesc.setStyle("-fx-text-fill: #1e8e3e;");
        } else {
            box.setStyle("-fx-background-color: #fce8e6; -fx-background-radius: 5; -fx-padding: 0 10 0 10; -fx-border-color: #fad2cf; -fx-border-radius: 5;");
            statusSymbol.setStyle("-fx-text-fill: #d93025; -fx-font-weight: bold;");
            statusDesc.setStyle("-fx-text-fill: #d93025;");
        }

        box.getChildren().addAll(statusSymbol, statusDesc);
        testsResultContainer.getChildren().add(box);
    }

    private void updateStatus(int code, String message) {
        statusLabel.setText(code + " " + message);
        if (code >= 200 && code < 300) {
            statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        }
    }

    private String formatJson(String json) {
        if (json == null || json.isBlank()) return "";
        String trimmed = json.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) return json;

        StringBuilder formatted = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char current = trimmed.charAt(i);
            if (escaped) { formatted.append(current); escaped = false; continue; }
            if (current == '\\') { formatted.append(current); escaped = true; continue; }
            if (current == '"') { formatted.append(current); inString = !inString; continue; }
            if (inString) { formatted.append(current); continue; }

            switch (current) {
                case '{', '[' -> {
                    formatted.append(current).append('\n');
                    indent++;
                    appendIndent(formatted, indent);
                }
                case '}', ']' -> {
                    formatted.append('\n');
                    indent = Math.max(0, indent - 1);
                    appendIndent(formatted, indent);
                    formatted.append(current);
                }
                case ',' -> {
                    formatted.append(current).append('\n');
                    appendIndent(formatted, indent);
                }
                case ':' -> formatted.append(": ");
                default -> {
                    if (!Character.isWhitespace(current)) formatted.append(current);
                }
            }
        }
        return formatted.toString();
    }

    private void appendIndent(StringBuilder builder, int indent) {
        builder.append("  ".repeat(Math.max(0, indent)));
    }

    private String resolveRequestUrl() {
        String enteredUrl = urlField.getText();
        if (enteredUrl == null || enteredUrl.trim().isEmpty()) return "";
        String trimmedUrl = enteredUrl.trim();
        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) return trimmedUrl;

        String configuredBaseUrl = AppRunConfig.getBaseUrl();
        String baseUrl = AppRunConfig.normalizeBaseUrl(configuredBaseUrl == null || configuredBaseUrl.isBlank()
                ? AppRunConfig.DEFAULT_BASE_URL
                : configuredBaseUrl);
        return trimmedUrl.startsWith("/") ? baseUrl + trimmedUrl : baseUrl + "/" + trimmedUrl;
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
            Label userLabel = new Label("Username");
            userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
            Label passLabel = new Label("Password");
            passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
            authConfigContainer.getChildren().addAll(userLabel, authUserField, passLabel, authPasswordField);
        } else if ("Bearer Token".equals(authType)) {
            Label tokenLabel = new Label("Token");
            tokenLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
            authConfigContainer.getChildren().addAll(tokenLabel, authTokenField);
        }
    }

    private void initBodyTab() {
        ToggleGroup bodyGroup = new ToggleGroup();
        rawBtn.setToggleGroup(bodyGroup);
        formDataBtn.setToggleGroup(bodyGroup);
        rawBtn.setSelected(true);
        rawTypeComboBox.getItems().addAll("JSON", "Text", "XML");
        rawTypeComboBox.setValue("JSON");

        // LOGIC CHUYỂN ĐỔI ẨN/HIỆN ĐỘNG GIỮA RAW VÀ FORM-DATA
        rawBtn.setOnAction(e -> {
            bodyTextArea.setVisible(true);
            bodyTextArea.setManaged(true);
            rawTypeComboBox.setVisible(true);
            rawTypeComboBox.setManaged(true);

            formDataContainer.setVisible(false);
            formDataContainer.setManaged(false);
        });

        formDataBtn.setOnAction(e -> {
            bodyTextArea.setVisible(false);
            bodyTextArea.setManaged(false);
            rawTypeComboBox.setVisible(false);
            rawTypeComboBox.setManaged(false);

            formDataContainer.setVisible(true);
            formDataContainer.setManaged(true);
        });
    }

    // ========== MODEL CLASS ==========

    public static class DataRowModel {
        private final SimpleStringProperty key;
        private final SimpleStringProperty value;
        private final SimpleStringProperty description;

        public DataRowModel(String key, String value, String description) {
            this.key = new SimpleStringProperty(key == null ? "" : key);
            this.value = new SimpleStringProperty(value == null ? "" : value);
            this.description = new SimpleStringProperty(description == null ? "" : description);
        }

        public String getKey() { return key.get(); }
        public void setKey(String v) { this.key.set(v == null ? "" : v); }
        public SimpleStringProperty keyProperty() { return key; }

        public String getValue() { return value.get(); }
        public void setValue(String v) { this.value.set(v == null ? "" : v); }
        public SimpleStringProperty valueProperty() { return value; }

        public String getDescription() { return description.get(); }
        public void setDescription(String v) { this.description.set(v == null ? "" : v); }
        public SimpleStringProperty descriptionProperty() { return description; }
    }

    public static class HeaderModel {
        private final String key;
        private final String value;
        public HeaderModel(String key, String value) {
            this.key = key == null ? "" : key;
            this.value = value == null ? "" : value;
        }
        public String getKey() { return key; }
        public String getValue() { return value; }
    }
}