package com.example.apitestapp.controllers;

import com.example.apitestapp.config.AppRunConfig;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import okhttp3.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RequestController implements Initializable {

    private final OkHttpClient client = new OkHttpClient();
    // --- FXML Controls ---
    @FXML
    private ComboBox<String> methodComboBox;
    @FXML
    private ComboBox<String> methodComboBox1;
    @FXML
    private ComboBox<String> methodComboBox2;
    @FXML
    private TextField baseUrlField;
    @FXML
    private TextField urlField;
    @FXML
    private Button sendButton;
    @FXML
    private Button runAllButton;
    @FXML
    private Button runSelectedButton;
    @FXML
    private Button saveRunButton;
    @FXML
    private ComboBox<String> authTypeComboBox;
    @FXML
    private VBox authConfigContainer;
    @FXML
    private ToggleButton rawBtn;
    @FXML
    private ToggleButton formDataBtn;
    @FXML
    private ComboBox<String> rawTypeComboBox;
    @FXML
    private VBox bodyContentContainer;
    @FXML
    private TextArea bodyTextArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private TextArea responseBodyTextArea;
    @FXML
    private TableView<HeaderModel> headersTableView;
    @FXML
    private TableColumn<HeaderModel, String> headerKeyCol;
    @FXML
    private TableColumn<HeaderModel, String> headerValueCol;
    private ToggleGroup bodyGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGeneralControls();
        initAuthTab();
        initBodyTab();
        initResponseTable();

        // Gán sự kiện cho nút Send
        sendButton.setOnAction(e -> handleSendRequest());
    }

    private void initGeneralControls() {
        String configuredBaseUrl = AppRunConfig.getBaseUrl();
        baseUrlField.setText(configuredBaseUrl == null || configuredBaseUrl.isBlank()
                ? AppRunConfig.DEFAULT_BASE_URL
                : configuredBaseUrl);
        methodComboBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodComboBox.setValue("POST");
        urlField.setText("/api/v1/signup");

        methodComboBox1.getItems().addAll("ALL", "SINGLE");
        methodComboBox2.getItems().addAll("Stop on fail", "Continue");
        methodComboBox1.setValue(AppRunConfig.getRunMode());
        methodComboBox2.setValue(AppRunConfig.getAlertMode());
    }

    private void initResponseTable() {
        headerKeyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("key"));
        headerValueCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("value"));
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

        long startTime = System.currentTimeMillis();

        Task<Response> task = new Task<>() {
            @Override
            protected Response call() throws Exception {
                Request.Builder builder = new Request.Builder()
                        .url(requestUrl)
                        .header("Accept", "*/*");

                if (!method.equals("GET") && !method.equals("DELETE")) {
                    RequestBody body = RequestBody.create(bodyText, mediaType);
                    builder.method(method, body);
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

    private void updateStatus(int code, String message) {
        statusLabel.setText(code + " " + message);
        if (code >= 200 && code < 300) {
            statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        }
    }

    private String formatJson(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }

        String trimmed = json.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            return json;
        }

        StringBuilder formatted = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char current = trimmed.charAt(i);

            if (escaped) {
                formatted.append(current);
                escaped = false;
                continue;
            }
            if (current == '\\') {
                formatted.append(current);
                escaped = true;
                continue;
            }
            if (current == '"') {
                formatted.append(current);
                inString = !inString;
                continue;
            }
            if (inString) {
                formatted.append(current);
                continue;
            }

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
                    if (!Character.isWhitespace(current)) {
                        formatted.append(current);
                    }
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
        if (enteredUrl == null || enteredUrl.trim().isEmpty()) {
            return "";
        }

        String trimmedUrl = enteredUrl.trim();
        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            return trimmedUrl;
        }

        String baseUrl = AppRunConfig.normalizeBaseUrl(baseUrlField.getText());
        if (trimmedUrl.startsWith("/")) {
            return baseUrl + trimmedUrl;
        }
        return baseUrl + "/" + trimmedUrl;
    }

    private String resolveMediaType() {
        String rawType = rawTypeComboBox.getValue();
        if ("Text".equals(rawType)) {
            return "text/plain; charset=utf-8";
        }
        if ("XML".equals(rawType)) {
            return "application/xml; charset=utf-8";
        }
        return "application/json; charset=utf-8";
    }

    // --- Các hàm khởi tạo UI (Giữ nguyên từ code cũ của bạn) ---
    private void initAuthTab() {
        authTypeComboBox.getItems().addAll("No Auth", "Basic Auth", "Bearer Token");
        authTypeComboBox.setValue("No Auth");
        authTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateAuthUI(newVal));
    }

    private void updateAuthUI(String authType) {
        authConfigContainer.getChildren().clear();
        if ("Basic Auth".equals(authType)) {
            authConfigContainer.getChildren().addAll(new Label("Username"), new TextField(), new Label("Password"), new PasswordField());
        } else if ("Bearer Token".equals(authType)) {
            authConfigContainer.getChildren().addAll(new Label("Token"), new TextField());
        }
    }

    private void initBodyTab() {
        bodyGroup = new ToggleGroup();
        rawBtn.setToggleGroup(bodyGroup);
        formDataBtn.setToggleGroup(bodyGroup);
        rawBtn.setSelected(true);
        rawTypeComboBox.getItems().addAll("JSON", "Text", "XML");
        rawTypeComboBox.setValue("JSON");
        bodyTextArea.setText("""
                {
                  "phoneNumber": "09811111",
                  "password": "truongson123"
                }""");
    }

    // Model để hiển thị Headers trong TableView
    public static class HeaderModel {
        private final String key;
        private final String value;

        public HeaderModel(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
