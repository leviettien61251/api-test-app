package com.example.apitestapp.controllers;

import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.models.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label lblTenHienThiProfile, lblRoleProfile, lblEmailProfile, lblIconRoleProfile, lblIconNgayThamGiaProfile;
    @FXML
    private Button btnChinhSuaHoSo, btnLuuThayDoi, btnHuy, btnDoiMatKhau;
    @FXML
    private TextField txtTenHienThi, txtEmail, txtSoDienThoai, txtMatKhauHienTai, txtMatKhauMoi, txtXacNhanMatKhau;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProfileController initialized.");
        roleComboBox.getItems().addAll("Tester", "Admin");

        User user = AppSession.getInstance().getCurrentUser();

        // Lấy tên role từ user
        String roleName = resolveRoleName(user);

        // Set đồng bộ cho tất cả các thành phần hiển thị role
        roleComboBox.setValue(roleName);
        lblRoleProfile.setText(roleName);
        lblIconRoleProfile.setText("Vai trò: " + roleName);

        // Set các thông tin khác
        lblTenHienThiProfile.setText(user.getFullName());
        lblEmailProfile.setText(user.getEmail());
        lblIconNgayThamGiaProfile.setText(user.getCreatedAt().toString());
        txtTenHienThi.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtSoDienThoai.setText(user.getPhoneNumber());
    }

    /**
     * Xác định tên role dựa vào roleId của user
     * @param user đối tượng User hiện tại
     * @return "Admin" nếu roleId = 1, "Tester" nếu roleId = 2 hoặc user null
     */
    private String resolveRoleName(User user) {
        if (user != null && Integer.valueOf(1).equals(user.getRoleId())) {
            return "Admin";
        }
        return "Tester";
    }
}