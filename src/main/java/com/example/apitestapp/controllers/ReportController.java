package com.example.apitestapp.controllers;

import com.example.apitestapp.config.SelectedRunContext;
import com.example.apitestapp.models.dto.TestResult;
import com.example.apitestapp.models.dto.TestRun;
import com.example.apitestapp.services.RunStorage;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportController implements Initializable, RefreshableView {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
    private final RunStorage runStorage = RunStorage.getInstance();
    @FXML
    private Label lblRunId, lblRunner, lblMachine, lblOS, lblTime, lblOverall;
    @FXML
    private Label lblTotal, lblPass, lblFail;
    @FXML
    private PieChart pieChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private TableView<TestResult> reportTable;
    @FXML
    private TableColumn<TestResult, Number> colTc;
    @FXML
    private TableColumn<TestResult, String> colName, colStatus, colTime, colNote;

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8) + "...";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTc.setCellValueFactory(data -> {
            int index = data.getTableView() != null
                    ? data.getTableView().getItems().indexOf(data.getValue()) + 1
                    : 0;
            return new ReadOnlyObjectWrapper<>(index);
        });
        colName.setCellValueFactory(new PropertyValueFactory<>("caseName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTime.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getResponseTimeMs() + " ms"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("message"));
        refresh();
    }

    @Override
    public void refresh() {
        Optional<TestRun> opt = resolveRun();
        if (opt.isEmpty()) {
            showEmptyState();
            return;
        }
        bindRun(opt.get());
    }

    private Optional<TestRun> resolveRun() {
        String runId = SelectedRunContext.getSelectedRunId();
        if (runId != null) {
            Optional<TestRun> byId = runStorage.getById(runId);
            if (byId.isPresent()) {
                return byId;
            }
        }
        return runStorage.getAllRuns().stream().findFirst();
    }

    private void bindRun(TestRun run) {
        lblRunId.setText(shortId(run.getId()));
        lblRunner.setText(nullToDash(run.getUser()));
        lblMachine.setText(nullToDash(run.getMachine()));
        lblOS.setText(nullToDash(run.getOs()));
        lblTime.setText(run.getStartedAt() == null ? "-" : TIME_FMT.format(run.getStartedAt()));

        boolean failed = run.getFailedCases() > 0;
        lblOverall.setText(failed ? "FAIL" : "PASS");
        lblOverall.setStyle(failed
                ? "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11;"
                : "-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11;");

        lblTotal.setText(String.valueOf(run.getTotalCases()));
        lblPass.setText(String.valueOf(run.getPassedCases()));
        lblFail.setText(String.valueOf(run.getFailedCases()));

        pieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Pass", Math.max(run.getPassedCases(), 0)),
                new PieChart.Data("Fail", Math.max(run.getFailedCases(), 0))
        ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int index = 1;
        for (TestResult result : run.getResults()) {
            series.getData().add(new XYChart.Data<>("TC" + index++, result.getResponseTimeMs()));
        }
        barChart.getData().setAll(series);

        reportTable.setItems(FXCollections.observableArrayList(run.getResults()));
    }

    private void showEmptyState() {
        lblRunId.setText("-");
        lblRunner.setText("-");
        lblMachine.setText("-");
        lblOS.setText("-");
        lblTime.setText("-");
        lblOverall.setText("Chưa có run");
        lblTotal.setText("0");
        lblPass.setText("0");
        lblFail.setText("0");
        pieChart.setData(FXCollections.observableArrayList());
        barChart.getData().clear();
        reportTable.setItems(FXCollections.observableArrayList());
    }
}
