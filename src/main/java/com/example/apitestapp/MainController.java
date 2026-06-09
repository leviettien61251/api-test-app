package com.example.apitestapp;

import com.example.apitestapp.config.AppRunConfig;
import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.config.SelectedRunContext;
import com.example.apitestapp.controllers.DashboardController;
import com.example.apitestapp.controllers.HistoryController;
import com.example.apitestapp.controllers.RefreshableView;
import com.example.apitestapp.models.entity.ClientMachine;
import com.example.apitestapp.models.entity.User;
import com.example.apitestapp.services.ClientMachineService;
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

        Platform.runLater(() -> {
            setupShortcuts();
            setupCloseRequest();
        });
    }

    private void setupCloseRequest() {
        if (btnDashboard.getScene() == null) return;

        Stage stage = (Stage) btnDashboard.getScene().getWindow();

        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận thoát");
            alert.setHeaderText("Bạn có chắc chắn muốn thoát ứng dụng?");

            ButtonType btnYes = new ButtonType("Có", ButtonBar.ButtonData.YES);
            ButtonType btnNo = new ButtonType("Không", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(btnYes, btnNo);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == btnYes) {
                Platform.exit();
                System.exit(0);
            } else {
                event.consume();
            }
        });
    }

    private void setupShortcuts() {
        if (btnDashboard.getScene() == null) return;

        Scene scene = btnDashboard.getScene();

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                this::navigateDashboard
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN),
                this::navigateTestcases
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                this::navigateRequests
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
                this::navigateReports
        );

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

            setActiveButton(button);

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

        if (button != null) {
            button.setSelected(true);
        }
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
        dialog.setHeaderText("Hãy nhập cấu hình mặc định trước khi chạy testcase");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        // Background giống trong ảnh - xanh dương đậm
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #1e3a5f;" +
                        "-fx-background-radius: 0;"
        );

        TextField baseUrlField = new TextField(AppRunConfig.getBaseUrl().isEmpty() ? AppRunConfig.DEFAULT_BASE_URL : AppRunConfig.getBaseUrl());
        baseUrlField.setPromptText("http://localhost:8080");
        baseUrlField.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 3;" +
                        "-fx-background-radius: 3;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-prompt-text-fill: #999999;" +
                        "-fx-padding: 6 10;" +
                        "-fx-font-size: 13px;"
        );

        // Focus effect
        baseUrlField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                baseUrlField.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-border-color: #4a9eff;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-border-radius: 3;" +
                                "-fx-background-radius: 3;" +
                                "-fx-text-fill: #333333;" +
                                "-fx-prompt-text-fill: #999999;" +
                                "-fx-padding: 6 10;" +
                                "-fx-font-size: 13px;"
                );
            } else {
                baseUrlField.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-border-color: #cccccc;" +
                                "-fx-border-radius: 3;" +
                                "-fx-background-radius: 3;" +
                                "-fx-text-fill: #333333;" +
                                "-fx-prompt-text-fill: #999999;" +
                                "-fx-padding: 6 10;" +
                                "-fx-font-size: 13px;"
                );
            }
        });

        ComboBox<String> alertModeBox = new ComboBox<>();
        alertModeBox.getItems().addAll("Stop on fail", "Continue");
        alertModeBox.setValue(AppRunConfig.getAlertMode());
        alertModeBox.setMaxWidth(Double.MAX_VALUE);
        alertModeBox.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 3;" +
                        "-fx-background-radius: 3;" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: #333333;"
        );

        // Style cho dropdown
        alertModeBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    setStyle(
                            "-fx-background-color: #ffffff;" +
                                    "-fx-text-fill: #333333;" +
                                    "-fx-padding: 6 10;"
                    );
                    setOnMouseEntered(e -> setStyle(
                            "-fx-background-color: #e6f0ff;" +
                                    "-fx-text-fill: #333333;" +
                                    "-fx-padding: 6 10;"
                    ));
                    setOnMouseExited(e -> setStyle(
                            "-fx-background-color: #ffffff;" +
                                    "-fx-text-fill: #333333;" +
                                    "-fx-padding: 6 10;"
                    ));
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 15, 20));
        grid.setStyle("-fx-background-color: transparent;");

        // Style cho label - màu trắng nhạt
        Label baseUrlLabel = new Label("Base URL");
        baseUrlLabel.setStyle("-fx-text-fill: #e8eef4; -fx-font-size: 13px;");

        Label alertLabel = new Label("Alert mode");
        alertLabel.setStyle("-fx-text-fill: #e8eef4; -fx-font-size: 13px;");

        Label machineLabel = new Label("Machine");
        machineLabel.setStyle("-fx-text-fill: #e8eef4; -fx-font-size: 13px;");

        Label osLabel = new Label("OS");
        osLabel.setStyle("-fx-text-fill: #e8eef4; -fx-font-size: 13px;");

        // Style cho info values - màu trắng
        Label machineValue = new Label(AppRunConfig.getMachineName());
        machineValue.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        Label osValue = new Label(AppRunConfig.getOs());
        osValue.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        grid.add(baseUrlLabel, 0, 0);
        grid.add(baseUrlField, 1, 0);

        grid.add(alertLabel, 0, 1);
        grid.add(alertModeBox, 1, 1);

        grid.add(machineLabel, 0, 2);
        grid.add(machineValue, 1, 2);

        grid.add(osLabel, 0, 3);
        grid.add(osValue, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Style cho header - màu xanh đậm hơn
        dialog.getDialogPane().lookup(".header-panel").setStyle(
                "-fx-background-color: #163a5f;" +
                        "-fx-border-bottom: 1px solid #2a4a6f;"
        );

        dialog.getDialogPane().lookup(".header-panel .label").setStyle(
                "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        // Style buttons giống trong ảnh
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);

        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: #2a6a9e;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-border-radius: 3;" +
                            "-fx-background-radius: 3;" +
                            "-fx-padding: 6 20;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            );

            okButton.setOnMouseEntered(e -> okButton.setStyle(
                    "-fx-background-color: #3a7aae;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-border-radius: 3;" +
                            "-fx-background-radius: 3;" +
                            "-fx-padding: 6 20;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            ));

            okButton.setOnMouseExited(e -> okButton.setStyle(
                    "-fx-background-color: #2a6a9e;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-border-radius: 3;" +
                            "-fx-background-radius: 3;" +
                            "-fx-padding: 6 20;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            ));
        }

        if (cancelButton != null) {
            cancelButton.setStyle(
                    "-fx-background-color: #f0f0f0;" +
                            "-fx-text-fill: #333333;" +
                            "-fx-border-color: #cccccc;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 3;" +
                            "-fx-background-radius: 3;" +
                            "-fx-padding: 6 18;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            );

            cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                    "-fx-background-color: #e6e6e6;" +
                            "-fx-text-fill: #333333;" +
                            "-fx-border-color: #bbbbbb;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 3;" +
                            "-fx-background-radius: 3;" +
                            "-fx-padding: 6 18;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            ));

            cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                    "-fx-background-color: #f0f0f0;" +
                            "-fx-text-fill: #333333;" +
                            "-fx-border-color: #cccccc;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 3;" +
                            "-fx-background-radius: 3;" +
                            "-fx-padding: 6 18;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            ));
        }

        dialog.showAndWait().ifPresent(button -> {
            if (button == okButtonType) {
                AppRunConfig.configure(
                        baseUrlField.getText(),
                        alertModeBox.getValue()
                );

                viewCache.remove("views/testcase-view.fxml");
                viewCache.remove("views/request-view.fxml");

                navigateTo("views/testcase-view.fxml", btnTestcase);
            }
        });
    }

    private void getClientMachineInfo() throws Exception {
        ClientMachineService clientMachineService = new ClientMachineService();
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

        clientMachineService.save(cm);
    }
}