package com.example.apitestapp.controllers;

import com.example.apitestapp.models.TestRun;
import com.example.apitestapp.services.RunStorage;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DashboardController implements Initializable, RefreshableView {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    @FXML
    private Label lblTotalTc, lblTotalRun, lblTotalPass, lblTotalFail;
    @FXML
    private PieChart passFailChart;
    @FXML
    private TableView<TestRun> historyTable;
    @FXML
    private TableColumn<TestRun, String> colTime, colUser, colMachine, colResult, colDetail;

    private final RunStorage runStorage = RunStorage.getInstance();
    private Consumer<String> onOpenReport;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRecentTable();
        refresh();
    }

    public void setOnOpenReport(Consumer<String> onOpenReport) {
        this.onOpenReport = onOpenReport;
    }

    @Override
    public void refresh() {
        List<TestRun> runs = runStorage.getAllRuns();

        int totalTc = runs.stream().mapToInt(TestRun::getTotalCases).sum();
        int totalPass = runs.stream().mapToInt(TestRun::getPassedCases).sum();
        int totalFail = runs.stream().mapToInt(TestRun::getFailedCases).sum();

        lblTotalTc.setText(String.valueOf(totalTc));
        lblTotalRun.setText(String.valueOf(runs.size()));
        lblTotalPass.setText(String.valueOf(totalPass));
        lblTotalFail.setText(String.valueOf(totalFail));

        passFailChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Pass", Math.max(totalPass, 0)),
                new PieChart.Data("Fail", Math.max(totalFail, 0))
        ));
        if (totalPass + totalFail == 0) {
            passFailChart.setTitle("Chưa có dữ liệu");
        } else {
            passFailChart.setTitle(null);
        }

        List<TestRun> recent = runs.stream()
                .sorted(Comparator.comparing(TestRun::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();
        historyTable.setItems(FXCollections.observableArrayList(recent));
    }

    private void setupRecentTable() {
        colTime.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatTime(data.getValue())));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colMachine.setCellValueFactory(new PropertyValueFactory<>("machine"));
        colResult.setCellValueFactory(data -> new ReadOnlyStringWrapper(summaryResult(data.getValue())));
        colDetail.setCellValueFactory(data -> new ReadOnlyStringWrapper(shortId(data.getValue().getId())));

        historyTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TestRun selected = historyTable.getSelectionModel().getSelectedItem();
                if (selected != null && onOpenReport != null) {
                    onOpenReport.accept(selected.getId());
                }
            }
        });
    }

    private static String formatTime(TestRun run) {
        if (run.getStartedAt() == null) {
            return "-";
        }
        return TIME_FMT.format(run.getStartedAt());
    }

    private static String summaryResult(TestRun run) {
        return run.getFailedCases() > 0 ? "FAIL (" + run.getPassedCases() + "/" + run.getTotalCases() + ")" :
                "PASS (" + run.getPassedCases() + "/" + run.getTotalCases() + ")";
    }

    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8);
    }
}
