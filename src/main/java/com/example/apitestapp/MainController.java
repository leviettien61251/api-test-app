package com.example.apitestapp;

import com.example.apitestapp.config.AppRunConfig;
import com.example.apitestapp.config.AppSession;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final Map<String, Node> viewCache = new HashMap<>();

    @FXML
    private StackPane contentArea;

    // Khai báo đầy đủ các ToggleButton để điều khiển trạng thái "lún" (selected)
    @FXML
    private ToggleButton btnDashboard, btnTestcase, btnRequest, btnReport;

    @FXML
    private ToggleButton btnCollections, btnEnvironments, btnHistory, btnProfile;

    @FXML
    private MenuButton userMenuButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Mặc định nạp Dashboard khi ứng dụng khởi chạy
        navigateTo("views/dashboard-view.fxml", btnDashboard);
        Platform.runLater(this::showDefaultRunConfigDialog);

        if (userMenuButton != null) {
            userMenuButton.textProperty().bind(Bindings.concat("👤 User: ", AppSession.usernameProperty()));
        }
    }

    // Các hàm chuyển đổi View cho Header
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

    // Các hàm chuyển đổi View cho Sidebar
    @FXML
    private void navigateCollections() {
        navigateTo("views/collections-view.fxml", btnCollections);
    }

    @FXML
    private void navigateEnvironments() {
        navigateTo("views/environments-view.fxml", btnEnvironments);
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
        AppRunConfig.reset(); // THÊM DÒNG NÀY: Xóa sạch cấu hình cũ khi đăng xuất

        // Xóa luôn cache các view để khi người sau vào nó nạp mới hoàn toàn
        viewCache.clear();

        MainApplication.showLoginView();
    }

    /**
     * Hàm dùng chung để nạp file FXML vào vùng nội dung chính
     * @param fxmlPath Đường dẫn file fxml tính từ thư mục resources
     * @param button Nút được nhấn để cập nhật trạng thái hiển thị
     */
    private void navigateTo(String fxmlPath, ToggleButton button) {
        try {
            // Sử dụng Cache để tránh việc nạp lại file FXML nhiều lần gây lag
            if (!viewCache.containsKey(fxmlPath)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node view = loader.load();
                viewCache.put(fxmlPath, view);
            }

            Node view = viewCache.get(fxmlPath);

            // Tạo hiệu ứng mờ dần (Fade) khi đổi trang cho chuyên nghiệp
            FadeTransition fade = new FadeTransition(Duration.millis(250), view);
            fade.setFromValue(0);
            fade.setToValue(1);

            contentArea.getChildren().setAll(view);
            fade.play();

            // Cập nhật trạng thái "đang chọn" cho nút bấm và thay đổi màu sắc
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

    private void setActiveButton(ToggleButton button) {
        // Deselect all buttons then select the active one
        btnDashboard.setSelected(false);
        btnTestcase.setSelected(false);
        btnRequest.setSelected(false);
        btnReport.setSelected(false);
        btnCollections.setSelected(false);
        btnEnvironments.setSelected(false);
        btnHistory.setSelected(false);
        
        // Set the clicked button as selected to apply CSS styling
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

        // 1. Thêm nút Cancel để khi ấn X hoặc Cancel sẽ không chạy logic lưu
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        // 2. Lấy giá trị hiện tại (nếu trống thì hiện mặc định làm gợi ý)
        TextField baseUrlField = new TextField(AppRunConfig.getBaseUrl().isEmpty() ? AppRunConfig.DEFAULT_BASE_URL : AppRunConfig.getBaseUrl());
        baseUrlField.setPromptText("http://localhost:8080");

        // ... (Giữ nguyên phần khởi tạo ComboBox runModeBox, alertModeBox và GridPane như cũ) ...
        ComboBox<String> runModeBox = new ComboBox<>();
        runModeBox.getItems().addAll("ALL", "SINGLE");
        runModeBox.setValue(AppRunConfig.getRunMode());
        runModeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> alertModeBox = new ComboBox<>();
        alertModeBox.getItems().addAll("Stop on fail", "Continue");
        alertModeBox.setValue(AppRunConfig.getAlertMode());
        alertModeBox.setMaxWidth(Double.MAX_VALUE);

        TextField runnerField = new TextField(AppRunConfig.getRunner());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 0, 10));
        grid.add(new Label("Base URL"), 0, 0); grid.add(baseUrlField, 1, 0);
        grid.add(new Label("Run mode"), 0, 1); grid.add(runModeBox, 1, 1);
        grid.add(new Label("Alert mode"), 0, 2); grid.add(alertModeBox, 1, 2);
        grid.add(new Label("Runner"), 0, 3); grid.add(runnerField, 1, 3);
        grid.add(new Label("Machine"), 0, 4); grid.add(new Label(AppRunConfig.getMachineName()), 1, 4);
        grid.add(new Label("OS"), 0, 5); grid.add(new Label(AppRunConfig.getOs()), 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(button -> {
            if (button == okButtonType) {
                // CHỈ LƯU KHI ẤN OK
                AppRunConfig.configure(
                        baseUrlField.getText(),
                        runModeBox.getValue(),
                        alertModeBox.getValue(),
                        runnerField.getText()
                );

                // 3. XÓA CACHE ĐỂ TRANG TESTCASE HIỆN DỮ LIỆU MỚI
                viewCache.remove("views/testcase-view.fxml");
                viewCache.remove("views/request-view.fxml");

                navigateTo("views/testcase-view.fxml", btnTestcase);
            }
            // Nhấn X hoặc Cancel sẽ không chạy vào đây -> Không bao giờ lưu!
        });
    }
}
