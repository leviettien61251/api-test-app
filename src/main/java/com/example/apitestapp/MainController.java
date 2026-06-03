package com.example.apitestapp;

import com.example.apitestapp.config.AppRunConfig;
import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.config.SelectedRunContext;
import com.example.apitestapp.controllers.DashboardController;
import com.example.apitestapp.controllers.HistoryController;
import com.example.apitestapp.controllers.RefreshableView;
import com.example.apitestapp.models.ClientMachine;
import com.example.apitestapp.models.User;
import com.example.apitestapp.repository.ClientMachineRepository;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final Map<String, Node> viewCache = new HashMap<>();

    @FXML
    private StackPane contentArea;

    @FXML
    private ToggleButton btnDashboard, btnTestcase, btnRequest, btnReport;

    @FXML
    private ToggleButton btnHistory;

    @FXML
    private MenuButton userMenuButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigateTo("views/dashboard-view.fxml", btnDashboard);
        Platform.runLater(this::showDefaultRunConfigDialog);

        try {
            getClientMachineInfo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (userMenuButton != null) {
            userMenuButton.textProperty().bind(Bindings.concat("👤 User: ", AppSession.usernameProperty()));
        }
    }

    @FXML
    private void navigateDashboard() {
        navigateTo("views/dashboard-view.fxml", btnDashboard);
    }

    @FXML
    private void navigateTestcases() {
        navigateTo("views/testcase-view.fxml", btnTestcase);
    }

    @FXML
    private void navigateRequests() {
        navigateTo("views/request-view.fxml", btnRequest);
    }

    @FXML
    private void navigateReports() {
        navigateTo("views/report-view.fxml", btnReport);
    }

    @FXML
    private void navigateHistory() {
        navigateTo("views/history-view.fxml", btnHistory);
    }

    @FXML
    private void navigateProfile() {
        navigateTo("views/profile-view.fxml", null);
    }

    @FXML
    private void handleLogout() {
        AppSession.clear();
        AppRunConfig.reset();
        viewCache.clear();
        MainApplication.showLoginView();
    }

    private void navigateTo(String fxmlPath, ToggleButton button) {
        try {
            if (!viewCache.containsKey(fxmlPath)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node view = loader.load();
                wireViewController(fxmlPath, loader.getController());
                view.setUserData(loader.getController());
                viewCache.put(fxmlPath, view);
            }

            Node view = viewCache.get(fxmlPath);
            Object controller = view.getUserData();
            if (controller instanceof RefreshableView refreshable) {
                refreshable.refresh();
            }

            FadeTransition fade = new FadeTransition(Duration.millis(250), view);
            fade.setFromValue(0);
            fade.setToValue(1);

            contentArea.getChildren().setAll(view);
            fade.play();

            if (button != null) {
                setActiveButton(button);
            }

        } catch (IOException e) {
            System.err.println("Không tìm thấy file FXML: " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Đường dẫn file FXML bị sai: " + fxmlPath);
        }
    }

    public void openReportForRun(String runId) {
        SelectedRunContext.setSelectedRunId(runId);
        navigateTo("views/report-view.fxml", btnReport);
    }

    private void wireViewController(String fxmlPath, Object controller) {
        if (controller instanceof HistoryController historyController) {
            historyController.setOnOpenReport(this::openReportForRun);
        }
        if (controller instanceof DashboardController dashboardController) {
            dashboardController.setOnOpenReport(this::openReportForRun);
        }
    }

    private void setActiveButton(ToggleButton button) {
        btnDashboard.setSelected(false);
        btnTestcase.setSelected(false);
        btnRequest.setSelected(false);
        btnReport.setSelected(false);
        btnHistory.setSelected(false);
        button.setSelected(true);
    }

    private void setTestNavigationEnabled(boolean enabled) {
        btnTestcase.setDisable(!enabled);
        btnRequest.setDisable(!enabled);
        btnReport.setDisable(!enabled);
        btnHistory.setDisable(!enabled);
    }

    private void showDefaultRunConfigDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Default run config");
        dialog.setHeaderText("Nhap cau hinh mac dinh truoc khi chay testcase");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        TextField baseUrlField = new TextField(AppRunConfig.getBaseUrl().isEmpty() ? AppRunConfig.DEFAULT_BASE_URL : AppRunConfig.getBaseUrl());
        baseUrlField.setPromptText("http://localhost:8080");

        // ĐÃ XÓA TOÀN BỘ ĐOẠN KHỞI TẠO runModeBox TẠI ĐÂY

        ComboBox<String> alertModeBox = new ComboBox<>();
        alertModeBox.getItems().addAll("Stop on fail", "Continue");
        alertModeBox.setValue(AppRunConfig.getAlertMode());
        alertModeBox.setMaxWidth(Double.MAX_VALUE);

        TextField runnerField = new TextField(AppRunConfig.getRunner());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 0, 10));

        // Hàng 0: Base URL
        grid.add(new Label("Base URL"), 0, 0);
        grid.add(baseUrlField, 1, 0);

        // ĐÃ XÓA: Dòng add Run mode ở hàng 1 cũ.

        // Cập nhật lại số hàng (Row Index) dịch lên trên 1 dòng để tránh trống khoảng cách
        grid.add(new Label("Alert mode"), 0, 1);
        grid.add(alertModeBox, 1, 1);

        grid.add(new Label("Runner"), 0, 2);
        grid.add(runnerField, 1, 2);

        grid.add(new Label("Machine"), 0, 3);
        grid.add(new Label(AppRunConfig.getMachineName()), 1, 3);

        grid.add(new Label("OS"), 0, 4);
        grid.add(new Label(AppRunConfig.getOs()), 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(button -> {
            if (button == okButtonType) {
                // Gọi hàm configure mới đã bỏ đi đối số runModeBox.getValue()
                AppRunConfig.configure(
                        baseUrlField.getText(),
                        alertModeBox.getValue(),
                        runnerField.getText()
                );

                viewCache.remove("views/testcase-view.fxml");
                viewCache.remove("views/request-view.fxml");

                navigateTo("views/testcase-view.fxml", btnTestcase);
            }
        });
    }

    private void getClientMachineInfo() throws Exception {
        ClientMachineRepository clientMachineRepository = new ClientMachineRepository();
        ClientMachine cm;

        User user = AppSession.getInstance().getCurrentUser();
        String userId = user.getId();
        InetAddress localHost = InetAddress.getLocalHost();

        String hostname = localHost.getHostName();
        String machineName = hostname;
        String os = System.getProperty("os.name");
        String ipAddress = localHost.getHostAddress();

        cm = ClientMachine.builder()
                .userId(userId)
                .hostname(hostname)
                .machineName(machineName)
                .os(os)
                .ipAddress(ipAddress)
                .build();

        clientMachineRepository.save(cm);
    }

    
}