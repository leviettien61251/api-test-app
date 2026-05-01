package com.example.apitestapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class EnvironmentsController implements Initializable {

    @FXML private Label lblSelectedEnv;
    @FXML private HBox envLocal, envDev, envStaging, envProduction;

    private List<HBox> allEnvItems;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allEnvItems = Arrays.asList(envLocal, envDev, envStaging, envProduction);

        // Mặc định cho Staging lún xuống ngay khi mở màn hình (nếu muốn)
        updateSidebarSelection(envStaging);
    }

    @FXML
    private void onEnvironmentClick(MouseEvent event) {
        // 1. Lấy đúng cái HBox mà m vừa click vào
        HBox clickedBox = (HBox) event.getSource();

        // 2. Lấy tên từ Label bên trong HBox (giả sử Label là node đầu tiên)
        // M dùng getChildren().stream() để lọc cho chắc ăn nếu sau này m thêm icon
        Label envNameLabel = (Label) clickedBox.getChildren().filtered(node -> node instanceof Label).get(0);
        String envName = envNameLabel.getText();

        // 3. Đổi tên ở tiêu đề bên phải
        lblSelectedEnv.setText(envName);

        // 4. Kích hoạt hiệu ứng lún (CSS class)
        updateSidebarSelection(clickedBox);
    }

    private void updateSidebarSelection(HBox selectedBox) {
        // Duyệt qua tất cả để xóa class cũ, tránh việc nhiều cái cùng lún
        for (HBox box : allEnvItems) {
            if (box != null) {
                box.getStyleClass().remove("env-item-selected");
            }
        }

        // Thêm class tạo hiệu ứng lún vào cái vừa chọn
        if (!selectedBox.getStyleClass().contains("env-item-selected")) {
            selectedBox.getStyleClass().add("env-item-selected");
        }
    }
}