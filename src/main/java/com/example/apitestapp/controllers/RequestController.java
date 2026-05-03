package com.example.apitestapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

//import okhttp3.OkHttpClient;

public class RequestController implements Initializable {

    // --- ComboBox chính ---
    @FXML
    private ComboBox<String> methodComboBox;
    @FXML
    private ComboBox<String> methodComboBox1;
    @FXML
    private ComboBox<String> methodComboBox2;

    // --- Các thành phần Auth Tab ---
    @FXML
    private ComboBox<String> authTypeComboBox;
    @FXML
    private VBox authConfigContainer;

    // --- Các thành phần Body Tab ---
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

    //Request Bar
    @FXML
    private TextField txtRequestURL;
    @FXML
    private Button btnSendRequest;
    @FXML
    private TextArea responseBodyTextArea;
    private ToggleGroup bodyGroup;


    OkHttpClient client = new OkHttpClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("RequestController initialized.");

        initGeneralControls();
        initAuthTab();
        initBodyTab();
    }

    private void initGeneralControls() {
        methodComboBox.getItems().addAll("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
        methodComboBox.setValue("GET");

        methodComboBox1.getItems().addAll("ALL", "SINGLE");
        methodComboBox2.getItems().addAll("Stop on fail", "Continue");
    }

    // --- LOGIC TAB AUTH (Giống Postman) ---
    private void initAuthTab() {
        authTypeComboBox.getItems().addAll("No Auth", "Basic Auth", "Bearer Token");
        authTypeComboBox.setValue("No Auth");

        authTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateAuthUI(newVal);
        });
    }

    private void updateAuthUI(String authType) {
        authConfigContainer.getChildren().clear();
        if (authType == null || authType.equals("No Auth")) return;

        authConfigContainer.setSpacing(8.0);

        if (authType.equals("Basic Auth")) {
            renderBasicAuth();
        } else if (authType.equals("Bearer Token")) {
            renderBearerToken();
        }
    }

    private void renderBasicAuth() {
        authConfigContainer.getChildren().addAll(
                new Label("Username"), createStyledTextField("Enter username"),
                new Label("Password"), createStyledPasswordField("Enter password")
        );
    }

    private void renderBearerToken() {
        authConfigContainer.getChildren().addAll(
                new Label("Token"), createStyledTextField("Enter token")
        );
    }

    // --- LOGIC TAB BODY (Raw vs Form-Data) ---
    private void initBodyTab() {
        bodyGroup = new ToggleGroup();
        rawBtn.setToggleGroup(bodyGroup);
        formDataBtn.setToggleGroup(bodyGroup);

        rawTypeComboBox.getItems().addAll("JSON", "Text", "JavaScript", "HTML", "XML");
        rawTypeComboBox.setValue("JSON");

        // Sự kiện khi nhấn nút Raw
        rawBtn.setOnAction(e -> {
            rawTypeComboBox.setVisible(true);
            showRawBody();
        });

        // Sự kiện khi nhấn nút Form-Data
        formDataBtn.setOnAction(e -> {
            rawTypeComboBox.setVisible(false);
            showFormDataBody();
        });
    }

    private void showRawBody() {
        bodyContentContainer.getChildren().clear();
        bodyContentContainer.getChildren().add(bodyTextArea);
    }

    private void showFormDataBody() {
        bodyContentContainer.getChildren().clear();

        // Tạo bảng Form-data giả lập
        VBox tableContainer = new VBox();

        // Header row
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        Label kH = new Label("Key");
        kH.setPrefWidth(150);
        kH.setStyle("-fx-font-weight: bold;");
        Label vH = new Label("Value");
        vH.setPrefWidth(150);
        vH.setStyle("-fx-font-weight: bold;");
        header.getChildren().addAll(new Region(), kH, vH, new Label("Description"));

        VBox rows = new VBox(5);
        rows.getChildren().add(createFormDataRow()); // Dòng đầu tiên

        Button addBtn = new Button("+ Add Row");
        addBtn.getStyleClass().add("btn-link");
        addBtn.setOnAction(e -> rows.getChildren().add(createFormDataRow()));

        bodyContentContainer.getChildren().addAll(header, rows, addBtn);
    }

    private HBox createFormDataRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5;");

        CheckBox cb = new CheckBox();
        cb.setSelected(true);
        TextField k = new TextField();
        k.setPromptText("Key");
        k.setPrefWidth(150);
        TextField v = new TextField();
        v.setPromptText("Value");
        v.setPrefWidth(150);
        TextField d = new TextField();
        d.setPromptText("Description");
        HBox.setHgrow(d, Priority.ALWAYS);

        Button del = new Button("🗑");
        del.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: transparent; -fx-cursor: hand;");
        del.setOnAction(e -> {
            if (((VBox) row.getParent()).getChildren().size() > 1) {
                ((VBox) row.getParent()).getChildren().remove(row);
            }
        });

        row.getChildren().addAll(cb, k, v, d, del);
        return row;
    }

    // Helper methods tạo UI nhanh
    private TextField createStyledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("input-field");
        return f;
    }

    private PasswordField createStyledPasswordField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.getStyleClass().add("input-field");
        return f;
    }

    @FXML
    private void sentRequest() {
        btnSendRequest.setOnAction(event -> {

            System.out.println("Url: " + txtRequestURL.getText());
            try {
                String result = run(txtRequestURL.getText());
                responseBodyTextArea.setText(result);
                System.out.println("Result; " + result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    private String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)

                .build();

        try (Response res = client.newCall(request).execute()) {
            return res.body().string();
        }
    }
}