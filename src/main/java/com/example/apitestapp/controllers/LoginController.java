package com.example.apitestapp.controllers;

import com.example.apitestapp.MainApplication;
import com.example.apitestapp.config.AppSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.getItems().setAll("Tester", "Admin");
        roleComboBox.setValue("Tester");
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username == null || username.isBlank()) {
            errorLabel.setText("Vui lòng nhập tên đăng nhập.");
            usernameField.requestFocus();
            return;
        }
        if (password == null || password.isBlank()) {
            errorLabel.setText("Vui lòng nhập mật khẩu.");
            passwordField.requestFocus();
            return;
        }

        // Demo: chấp nhận mọi tài khoản/mật khẩu để vào app.
        AppSession.setUsername(username);
        AppSession.setRole(role);
        errorLabel.setText("");
        MainApplication.showMainView();
    }
}

