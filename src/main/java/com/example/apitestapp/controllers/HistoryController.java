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
    private TableColumn<TestRun, String> colId, colTime, colRunner, colMachine, colOs, colMode, colResult, colStatus;
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
                .sorted(Comparator.comparing(TestRun::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        applyFilters();
    }

    private void setupColumns() {
        colId.setCellValueFactory(data -> new ReadOnlyStringWrapper(shortId(data.getValue().getId())));
        colTime.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatTime(data.getValue())));
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
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Xóa lần chạy " + shortId(run.getId()) + "?", ButtonType.OK, ButtonType.CANCEL);
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

    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8);
    }
}
