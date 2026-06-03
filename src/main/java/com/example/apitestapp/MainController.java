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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

        // Kích hoạt phím tắt và sự kiện chặn nút X tắt ứng dụng
        Platform.runLater(() -> {
            setupShortcuts();
            setupCloseRequest();
        });
    }

    /**
     * Chặn sự kiện bấm nút X để hiển thị Dialog xác nhận thoát
     */
    private void setupCloseRequest() {
        if (btnDashboard.getScene() == null) return;

        // Lấy ra Stage (Cửa sổ chính) từ Scene hiện tại
        Stage stage = (Stage) btnDashboard.getScene().getWindow();

        stage.setOnCloseRequest(event -> {
            // Tạo một Alert Confirmation đẹp mắt theo chuẩn JavaFX
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận thoát");
            alert.setHeaderText("Bạn có chắc chắn muốn thoát ứng dụng?");


            // Tùy chỉnh text cho 2 nút bấm hiển thị bằng Tiếng Việt
            ButtonType btnYes = new ButtonType("Có", ButtonBar.ButtonData.YES);
            ButtonType btnNo = new ButtonType("Không", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(btnYes, btnNo);

            // Hiển thị dialog và đợi người dùng chọn
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == btnYes) {
                // Người dùng chọn Có -> Cho phép đóng ứng dụng bình thường
                Platform.exit();
                System.exit(0);
            } else {
                // Người dùng chọn Không (hoặc bấm X ẩn dialog) -> Tiêu hủy sự kiện đóng, giữ ứng dụng lại
                event.consume();
            }
        });
    }

    /**
     * Cài đặt bộ lắng nghe phím tắt trên toàn bộ ứng dụng
     */
    private void setupShortcuts() {
        if (btnDashboard.getScene() == null) return;

        Scene scene = btnDashboard.getScene();

        // Ctrl + D -> Chuyển sang Dashboard
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                this::navigateDashboard
        );

        // Ctrl + T -> Chuyển sang Testcase
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN),
                this::navigateTestcases
        );

        // Ctrl + R -> Chuyển sang Request
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                this::navigateRequests
        );

        // Ctrl + E -> Chuyển sang Report
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
                this::navigateReports
        );

        // Ctrl + H -> Chuyển sang History
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
                this::navigateHistory
        );
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

        ComboBox<String> alertModeBox = new ComboBox<>();
        alertModeBox.getItems().addAll("Stop on fail", "Continue");
        alertModeBox.setValue(AppRunConfig.getAlertMode());
        alertModeBox.setMaxWidth(Double.MAX_VALUE);

        TextField runnerField = new TextField(AppRunConfig.getRunner());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 0, 10));

        grid.add(new Label("Base URL"), 0, 0);
        grid.add(baseUrlField, 1, 0);

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