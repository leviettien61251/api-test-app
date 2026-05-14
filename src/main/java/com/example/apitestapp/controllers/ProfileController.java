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
    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     */

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

        // 2. Chọn giá trị mặc định (để nó hiện chữ Tester ngay khi mới vào)
        roleComboBox.setValue("Tester");


        User user = AppSession.getInstance().getCurrentUser();
        lblTenHienThiProfile.setText(user.getFullName());
        lblRoleProfile.setText("Tester");
        lblEmailProfile.setText(user.getEmail());
        lblIconRoleProfile.setText("Vai trò: " + "Tester");
        lblIconNgayThamGiaProfile.setText(user.getCreatedAt().toString());
        txtTenHienThi.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtSoDienThoai.setText(user.getPhoneNumber());

    }


}
