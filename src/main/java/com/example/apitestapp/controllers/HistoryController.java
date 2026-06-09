package com.example.apitestapp.controllers;

import com.example.apitestapp.models.TestRun;
import com.example.apitestapp.services.RunStorage;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.models.User;

public class HistoryController implements Initializable, RefreshableView {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    @FXML
    private ComboBox<String> cbResult;
    @FXML
    private DatePicker dpFrom, dpTo;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnFilter;
    @FXML
    private TableView<TestRun> historyTable;
    @FXML
    private TableColumn<TestRun, String> colId, colTime, colTestSuite, colRunner, colMachine, colOs, colMode, colResult, colStatus;
    @FXML
    private TableColumn<TestRun, TestRun> colDetails, colDelete;

    private final RunStorage runStorage = RunStorage.getInstance();
    private List<TestRun> allRuns = List.of();
    private Consumer<String> onOpenReport;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbResult.getItems().addAll("Tất cả", "Pass", "Fail");
        cbResult.setValue("Tất cả");

        setupColumns();
        setupFilters();
        historyTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TestRun selected = historyTable.getSelectionModel().getSelectedItem();
                if (selected != null && onOpenReport != null) {
                    onOpenReport.accept(selected.getId());
                }
            }
        });

        refresh();
    }

    public void setOnOpenReport(Consumer<String> onOpenReport) {
        this.onOpenReport = onOpenReport;
    }

    @FXML
    private void handleFilter() {
        applyFilters();
    }

    @Override
    public void refresh() {
        allRuns = runStorage.getAllRuns().stream()
                .filter(this::canCurrentUserSeeRun)
                .sorted(Comparator.comparing(TestRun::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        applyFilters();
    }

    private boolean canCurrentUserSeeRun(TestRun run) {
        User currentUser = AppSession.getInstance().getCurrentUser();

        if (currentUser != null && Integer.valueOf(1).equals(currentUser.getRoleId())) {
            return true; // Admin xem tất cả
        }

        String currentEmail = currentUser != null ? currentUser.getEmail() : AppSession.getUsername();
        return normalize(run.getUser()).equals(normalize(currentEmail));
    }

    private void setupColumns() {
        colId.setCellValueFactory(data -> new ReadOnlyStringWrapper(shortId(data.getValue().getId())));
        colTime.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatTime(data.getValue())));
        colTestSuite.setCellValueFactory(data -> new ReadOnlyStringWrapper(displayTestSuite(data.getValue())));
        colRunner.setCellValueFactory(new PropertyValueFactory<>("user"));
        colMachine.setCellValueFactory(new PropertyValueFactory<>("machine"));
        colOs.setCellValueFactory(new PropertyValueFactory<>("os"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("runMode"));
        colResult.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getPassedCases() + " / " + data.getValue().getFailedCases()));
        colStatus.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getFailedCases() > 0 ? "FAIL" : "PASS"));

        colDetails.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colDetails.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("👁");

            {
                btn.setOnAction(e -> {
                    TestRun run = getTableView().getItems().get(getIndex());
                    if (run != null && onOpenReport != null) {
                        onOpenReport.accept(run.getId());
                    }
                });
            }

            @Override
            protected void updateItem(TestRun item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        colDelete.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colDelete.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑");

            {
                btn.setOnAction(e -> {
                    TestRun run = getTableView().getItems().get(getIndex());
                    if (run == null) {
                        return;
                    }

                    // 1. Dùng AlertType.NONE để xóa sạch icon chấm hỏi mặc định thô kệch
                    Alert confirm = new Alert(Alert.AlertType.NONE);
                    confirm.setTitle("Xác nhận xóa");
                    confirm.setHeaderText(null); // Ẩn hoàn toàn vùng header trùng lặp chữ
                    confirm.setContentText("Bạn có chắc chắn muốn xóa lần chạy " + shortId(run.getId()) + "?");

                    // 2. Set các nút chức năng
                    confirm.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

                    // 3. Tinh chỉnh tổng thể DialogPane bằng Inline CSS
                    DialogPane dialogPane = confirm.getDialogPane();
                    dialogPane.setStyle(
                            "-fx-background-color: #ffffff; " +
                                    "-fx-padding: 20px; " +
                                    "-fx-font-family: 'Segoe UI', system-ui, sans-serif; " +
                                    "-fx-font-size: 14px;"
                    );

                    // Định dạng lại nhãn chữ nội dung (Màu xám đậm quyến rũ, tạo khoảng cách dưới)
                    dialogPane.lookup(".content.label").setStyle(
                            "-fx-text-fill: #2d3748; " +
                                    "-fx-padding: 5px 0 18px 0; " +
                                    "-fx-font-weight: 500;"
                    );

                    // 4. Lấy nút ra cấu hình giao diện & hiệu ứng rê chuột (Hover)
                    Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                    Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

                    // Nút OK - Đổi sang màu XANH BIỂN (Primary Action)
                    if (okButton != null) {
                        okButton.setText("Xóa"); // Đổi chữ "OK" thành "Xóa" cho rõ nghĩa tiếng Việt
                        okButton.setStyle(
                                "-fx-background-color: #3b82f6; " + // Xanh biển hiện đại (Tailwind blue-500)
                                        "-fx-text-fill: white; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 8px 22px; " +
                                        "-fx-background-radius: 6px; " +
                                        "-fx-cursor: hand;"
                        );
                        okButton.setOnMouseEntered(ev -> okButton.setStyle(
                                "-fx-background-color: #2563eb; " + // Xanh đậm hơn khi hover
                                        "-fx-text-fill: white; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 8px 22px; " +
                                        "-fx-background-radius: 6px; " +
                                        "-fx-cursor: hand;"
                        ));
                        okButton.setOnMouseExited(ev -> okButton.setStyle(
                                "-fx-background-color: #3b82f6; " + // Trở lại xanh gốc
                                        "-fx-text-fill: white; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 8px 22px; " +
                                        "-fx-background-radius: 6px; " +
                                        "-fx-cursor: hand;"
                        ));
                    }

                    // Nút Cancel - Giữ màu xám sáng thanh lịch
                    if (cancelButton != null) {
                        cancelButton.setText("Hủy"); // Đổi "Cancel" thành "Hủy"
                        cancelButton.setStyle(
                                "-fx-background-color: #f1f5f9; " + // Xám nhẹ nhạt
                                        "-fx-text-fill: #475569; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 8px 22px; " +
                                        "-fx-background-radius: 6px; " +
                                        "-fx-cursor: hand;"
                        );
                        cancelButton.setOnMouseEntered(ev -> cancelButton.setStyle(
                                "-fx-background-color: #e2e8f0; " +
                                        "-fx-text-fill: #475569; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 8px 22px; " +
                                        "-fx-background-radius: 6px; " +
                                        "-fx-cursor: hand;"
                        ));
                        cancelButton.setOnMouseExited(ev -> cancelButton.setStyle(
                                "-fx-background-color: #f1f5f9; " +
                                        "-fx-text-fill: #475569; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 8px 22px; " +
                                        "-fx-background-radius: 6px; " +
                                        "-fx-cursor: hand;"
                        ));
                    }

                    // 5. Xử lý sự kiện bấm nút
                    confirm.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.OK) {
                            runStorage.deleteRun(run.getId());
                            refresh();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(TestRun item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void setupFilters() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbResult.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        dpFrom.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        dpTo.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String resultFilter = cbResult.getValue();
        String keyword = normalize(txtSearch.getText());
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        List<TestRun> filtered = allRuns.stream()
                .filter(run -> matchDate(run, from, to))
                .filter(run -> matchResult(run, resultFilter))
                .filter(run -> matchKeyword(run, keyword))
                .collect(Collectors.toList());

        historyTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private static boolean matchDate(TestRun run, LocalDate from, LocalDate to) {
        if (run.getStartedAt() == null) {
            return true;
        }
        LocalDate runDate = run.getStartedAt().atZone(ZoneId.systemDefault()).toLocalDate();
        if (from != null && runDate.isBefore(from)) {
            return false;
        }
        if (to != null && runDate.isAfter(to)) {
            return false;
        }
        return true;
    }

    private static boolean matchResult(TestRun run, String filter) {
        if (filter == null || "Tất cả".equals(filter)) {
            return true;
        }
        if ("Pass".equals(filter)) {
            return run.getFailedCases() == 0;
        }
        if ("Fail".equals(filter)) {
            return run.getFailedCases() > 0;
        }
        return true;
    }

    private static boolean matchKeyword(TestRun run, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return contains(run.getUser(), keyword)
                || contains(run.getMachine(), keyword)
                || contains(run.getOs(), keyword)
                || contains(run.getRunMode(), keyword)
                || contains(run.getRunName(), keyword)
                || contains(run.getTestSuite(), keyword)
                || contains(run.getId(), keyword)
                || contains(formatTime(run), keyword)
                || contains(run.getPassedCases() + " / " + run.getFailedCases(), keyword)
                || contains(run.getFailedCases() > 0 ? "FAIL" : "PASS", keyword);
    }

    private static boolean contains(String value, String keyword) {
        return normalize(value).contains(keyword);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String formatTime(TestRun run) {
        if (run.getStartedAt() == null) {
            return "-";
        }
        return TIME_FMT.format(run.getStartedAt());
    }

    private static String displayTestSuite(TestRun run) {
        if (run == null || run.getTestSuite() == null || run.getTestSuite().isBlank()) {
            return "-";
        }
        return run.getTestSuite();
    }

    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8);
    }
}
