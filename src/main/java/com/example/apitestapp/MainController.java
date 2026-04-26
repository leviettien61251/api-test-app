package com.example.apitestapp;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Mặc định nạp Dashboard khi ứng dụng khởi chạy
        navigateTo("views/dashboard-view.fxml", btnDashboard);
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
        navigateTo("views/profile-view.fxml", btnProfile);
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
        btnProfile.setSelected(false);
        
        // Set the clicked button as selected to apply CSS styling
        button.setSelected(true);
    }
}