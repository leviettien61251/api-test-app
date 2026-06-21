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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
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
    private Button btnExport;
    @FXML
    private TableView<TestResult> reportTable;
    @FXML
    private TableColumn<TestResult, Number> colTc;
    @FXML
    private TableColumn<TestResult, String> colName, colStatus, colTime, colNote;
    private TestRun currentRun;

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
        currentRun = run;
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
        currentRun = null;
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

    @FXML
    private void handleExportExcel() {
        if (currentRun == null) {
            showAlert("Thông báo", "Không có dữ liệu báo cáo để xuất!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("BaoCaoKiemThu_" + shortId(currentRun.getId()).replace("...", "") + "_" + LocalDate.now() + ".xlsx");

        File file = fileChooser.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            exportToExcel(currentRun, file.getAbsolutePath());
            showAlert("Thành công", "Đã xuất file thành công tại: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể xuất file: " + e.getMessage());
        }
    }

    private void exportToExcel(TestRun run, String filePath) throws Exception {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            applyThinBorder(headerStyle);

            org.apache.poi.ss.usermodel.CellStyle dataStyle = workbook.createCellStyle();
            applyThinBorder(dataStyle);

            org.apache.poi.ss.usermodel.CellStyle passStyle = workbook.createCellStyle();
            passStyle.cloneStyleFrom(dataStyle);
            org.apache.poi.ss.usermodel.Font passFont = workbook.createFont();
            passFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.GREEN.getIndex());
            passFont.setBold(true);
            passStyle.setFont(passFont);

            org.apache.poi.ss.usermodel.CellStyle failStyle = workbook.createCellStyle();
            failStyle.cloneStyleFrom(dataStyle);
            org.apache.poi.ss.usermodel.Font failFont = workbook.createFont();
            failFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.RED.getIndex());
            failFont.setBold(true);
            failStyle.setFont(failFont);

            createSummarySheet(workbook, run, titleStyle, headerStyle, dataStyle, passStyle, failStyle);
            createDetailSheet(workbook, run, headerStyle, dataStyle, passStyle, failStyle);

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }

    private void createSummarySheet(org.apache.poi.ss.usermodel.Workbook workbook,
                                    TestRun run,
                                    org.apache.poi.ss.usermodel.CellStyle titleStyle,
                                    org.apache.poi.ss.usermodel.CellStyle headerStyle,
                                    org.apache.poi.ss.usermodel.CellStyle dataStyle,
                                    org.apache.poi.ss.usermodel.CellStyle passStyle,
                                    org.apache.poi.ss.usermodel.CellStyle failStyle) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Tổng quan");
        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Báo cáo kiểm thử chi tiết");
        titleCell.setCellStyle(titleStyle);

        String[][] overview = {
                {"Mã run", nullToDash(run.getId())},
                {"Người chạy", nullToDash(run.getUser())},
                {"Máy thực thi", nullToDash(run.getMachine())},
                {"Hệ điều hành", nullToDash(run.getOs())},
                {"Thời gian", run.getStartedAt() == null ? "-" : TIME_FMT.format(run.getStartedAt())},
                {"Chế độ", nullToDash(run.getRunMode())},
                {"Test suite", nullToDash(run.getTestSuite())},
                {"Tổng số testcase", String.valueOf(run.getTotalCases())},
                {"Pass", String.valueOf(run.getPassedCases())},
                {"Fail", String.valueOf(run.getFailedCases())},
                {"Kết quả chung", run.getFailedCases() > 0 ? "FAIL" : "PASS"}
        };

        int rowIdx = 2;
        for (String[] item : overview) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
            createStyledCell(row, 0, item[0], headerStyle);
            org.apache.poi.ss.usermodel.CellStyle valueStyle = switch (item[0]) {
                case "Pass" -> passStyle;
                case "Fail" -> failStyle;
                case "Kết quả chung" -> "FAIL".equals(item[1]) ? failStyle : passStyle;
                default -> dataStyle;
            };
            createStyledCell(row, 1, item[1], valueStyle);
        }

        autoSizeColumns(sheet, 2);
    }

    private void createDetailSheet(org.apache.poi.ss.usermodel.Workbook workbook,
                                   TestRun run,
                                   org.apache.poi.ss.usermodel.CellStyle headerStyle,
                                   org.apache.poi.ss.usermodel.CellStyle dataStyle,
                                   org.apache.poi.ss.usermodel.CellStyle passStyle,
                                   org.apache.poi.ss.usermodel.CellStyle failStyle) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Chi tiết testcase");
        String[] columns = {"Mã TC", "Tên testcase", "Kết quả", "Expected Code", "Actual Code", "Thời gian", "Thời điểm chạy", "Ghi chú lỗi / Chi tiết"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            createStyledCell(headerRow, i, columns[i], headerStyle);
        }

        int rowIdx = 1;
        int tcIndex = 1;
        for (TestResult result : run.getResults()) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
            boolean failed = "FAILED".equalsIgnoreCase(result.getStatus()) || "FAIL".equalsIgnoreCase(result.getStatus());
            createStyledCell(row, 0, "TC" + tcIndex++, dataStyle);
            createStyledCell(row, 1, result.getCaseName(), dataStyle);
            createStyledCell(row, 2, result.getStatus(), failed ? failStyle : passStyle);
            createStyledCell(row, 3, String.valueOf(result.getExpectedCode()), dataStyle);
            createStyledCell(row, 4, String.valueOf(result.getActualCode()), dataStyle);
            createStyledCell(row, 5, result.getResponseTimeMs() + " ms", dataStyle);
            createStyledCell(row, 6, result.getExecutedAt() == null ? "-" : TIME_FMT.format(result.getExecutedAt()), dataStyle);
            createStyledCell(row, 7, result.getMessage(), dataStyle);
        }

        autoSizeColumns(sheet, columns.length);
    }

    private void applyThinBorder(org.apache.poi.ss.usermodel.CellStyle style) {
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    }

    private void createStyledCell(org.apache.poi.ss.usermodel.Row row, int column, String value, org.apache.poi.ss.usermodel.CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(column);
        cell.setCellValue(value == null || value.isBlank() ? "-" : value);
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(org.apache.poi.ss.usermodel.Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 2000, 20000));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
