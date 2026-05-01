package com.example.apitestapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class TestcaseController implements Initializable {

    // --- Các thành phần chung ---
    @FXML private TreeView<String> testSuiteTree;
    @FXML private ComboBox<String> moduleComboBox;
    @FXML private ComboBox<String> methodComboBox;

    // --- Các thành phần Auth Tab ---
    @FXML private ComboBox<String> authTypeComboBox;
    @FXML private VBox authConfigContainer;

    // --- Các thành phần Body Tab ---
    @FXML private ToggleButton rawBtn;
    @FXML private ToggleButton formDataBtn;
    @FXML private ComboBox<String> rawTypeComboBox;
    @FXML private VBox bodyContentContainer;
    @FXML private TextArea bodyTextArea;

    private ToggleGroup bodyGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTreeView();
        initComboBoxes();
        initAuthTab();
        initBodyTab();
    }

    private void initTreeView() {
        TreeItem<String> root = new TreeItem<>("Root");
        TreeItem<String> authSuite = new TreeItem<>(" Authentication");
        TreeItem<String> navSuite = new TreeItem<>(" Navigation API");
        TreeItem<String> deptSuite = new TreeItem<>(" Department API");

        navSuite.getChildren().add(new TreeItem<>("📄 TC03 - Tìm đường đến khoa Tim mạch"));
        navSuite.getChildren().add(new TreeItem<>("📄 TC04 - Lấy danh sách sơ đồ"));
        authSuite.getChildren().add(new TreeItem<>("📄 TC01 - Đăng nhập hệ thống"));
        authSuite.getChildren().add(new TreeItem<>("📄 TC02 - Kiểm tra Token"));
        deptSuite.getChildren().add(new TreeItem<>("📄 TC05 - Tìm kiếm phòng khám khoa sản"));
        deptSuite.getChildren().add(new TreeItem<>("📄 TC06 - Kiểm tra phòng ban"));

        root.getChildren().addAll(authSuite, navSuite, deptSuite);
        testSuiteTree.setRoot(root);
        authSuite.setExpanded(true);
        navSuite.setExpanded(true);
        deptSuite.setExpanded(true);
    }

    private void initComboBoxes() {
        moduleComboBox.getItems().addAll("Authentication", "Navigation API", "Department API");
        methodComboBox.getItems().addAll("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
        methodComboBox.setValue("GET");
    }

    // --- Logic cho TAB AUTH ---
    private void initAuthTab() {
        authTypeComboBox.getItems().addAll("No Auth", "Bearer Token", "Basic Auth");
        authTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateAuthUI(newVal);
        });
        authTypeComboBox.setValue("No Auth");
    }

    private void updateAuthUI(String authType) {
        authConfigContainer.getChildren().clear();
        if (authType == null || authType.equals("No Auth")) return;

        if (authType.equals("Bearer Token")) {
            renderBearerTokenUI();
        } else if (authType.equals("Basic Auth")) {
            renderBasicAuthUI();
        }
    }

    private void renderBearerTokenUI() {
        Label label = new Label("Token");
        label.setStyle("-fx-text-fill: #555;");
        TextField tokenField = new TextField();
        tokenField.setPromptText("Enter token");
        tokenField.getStyleClass().add("input-field");
        authConfigContainer.setSpacing(8.0);
        authConfigContainer.getChildren().addAll(label, tokenField);
    }

    private void renderBasicAuthUI() {
        Label uLabel = new Label("Username");
        TextField uField = new TextField();
        uField.setPromptText("Enter username");
        uField.getStyleClass().add("input-field");

        Label pLabel = new Label("Password");
        PasswordField pField = new PasswordField();
        pField.setPromptText("Enter password");
        pField.getStyleClass().add("input-field");

        authConfigContainer.setSpacing(8.0);
        authConfigContainer.getChildren().addAll(uLabel, uField, pLabel, pField);
    }


    // --- Logic cho TAB BODY ---
    private void initBodyTab() {
        // Nhóm nút Raw và Form-Data
        bodyGroup = new ToggleGroup();
        rawBtn.setToggleGroup(bodyGroup);
        formDataBtn.setToggleGroup(bodyGroup);

        // Khởi tạo ComboBox Raw Type (như trong Screenshot 2026-04-28 220059.png)
        rawTypeComboBox.getItems().addAll("JSON", "Text", "JavaScript", "HTML", "XML");
        rawTypeComboBox.setValue("JSON");

        // Sự kiện chuyển đổi Tab con trong Body
        rawBtn.setOnAction(e -> {
            rawTypeComboBox.setVisible(true);
            renderRawUI();
        });

        formDataBtn.setOnAction(e -> {
            rawTypeComboBox.setVisible(false);
            renderFormDataUI();
        });

        // Mặc định ban đầu
        rawBtn.setSelected(true);
        renderRawUI();
    }

    private void renderRawUI() {
        bodyContentContainer.getChildren().clear();
        // Hiển thị lại TextArea cho dữ liệu thô
        bodyContentContainer.getChildren().add(bodyTextArea);
    }

    private void renderFormDataUI() {
        bodyContentContainer.getChildren().clear();
        bodyContentContainer.setSpacing(0);

        // Tạo Header cho Form-data (như trong Screenshot 2026-04-28 220114.png)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 8; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 1 0;");

        Region spacer = new Region(); spacer.setPrefWidth(25);
        Label kHeader = new Label("Key"); kHeader.setPrefWidth(150); kHeader.setStyle("-fx-font-weight: bold;");
        Label vHeader = new Label("Value"); vHeader.setPrefWidth(150); vHeader.setStyle("-fx-font-weight: bold;");
        Label dHeader = new Label("Description"); dHeader.setStyle("-fx-font-weight: bold;");

        header.getChildren().addAll(spacer, kHeader, vHeader, dHeader);

        // Tạo một dòng trống mặc định
        VBox rowsContainer = new VBox();
        rowsContainer.getChildren().add(createFormDataRow());

        Button addBtn = new Button("+ Add Row");
        addBtn.getStyleClass().add("btn-link");
        addBtn.setOnAction(e -> rowsContainer.getChildren().add(createFormDataRow()));

        bodyContentContainer.getChildren().addAll(header, rowsContainer, addBtn);
    }

    private HBox createFormDataRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");

        CheckBox cb = new CheckBox();
        cb.setSelected(true);

        TextField keyField = new TextField(); keyField.setPromptText("Key"); keyField.setPrefWidth(150);

        // ComboBox chọn kiểu (Text/File) như trong ảnh 220114
        ComboBox<String> typePicker = new ComboBox<>();
        typePicker.getItems().addAll("Text", "File");
        typePicker.setValue("Text");
        typePicker.setPrefWidth(80);

        TextField valField = new TextField(); valField.setPromptText("Value"); valField.setPrefWidth(150);
        TextField descField = new TextField(); descField.setPromptText("Description");
        HBox.setHgrow(descField, Priority.ALWAYS);

        Button delBtn = new Button("🗑");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            VBox parent = (VBox) row.getParent();
            if (parent.getChildren().size() > 1) parent.getChildren().remove(row);
        });

        row.getChildren().addAll(cb, keyField, typePicker, valField, descField, delBtn);
        return row;
    }
}