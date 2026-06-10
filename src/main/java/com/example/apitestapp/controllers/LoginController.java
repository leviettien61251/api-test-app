package com.example.apitestapp.controllers;

import com.example.apitestapp.MainApplication;
import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.models.entity.User;
import com.example.apitestapp.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private final UserService userService = new UserService();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button togglePasswordBtn;
    @FXML
    private Label errorLabel;

    private boolean isPasswordVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setText("");
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());

        // Mặc định ẩn mật khẩu
        setPasswordVisibility(false);
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        setPasswordVisibility(isPasswordVisible);
    }

    private void setPasswordVisibility(boolean visible) {
        passwordField.setVisible(!visible);
        passwordField.setManaged(!visible);

        passwordTextField.setVisible(visible);
        passwordTextField.setManaged(visible);

        togglePasswordBtn.setText(visible ? "👁" : "🙈");

        TextField activeField = visible ? passwordTextField : passwordField;
        activeField.requestFocus();
        activeField.positionCaret(activeField.getText().length());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText(); // đã bind 2 chiều

        if (username == null || username.isBlank()) {
            errorLabel.setText("Vui lòng nhập tên đăng nhập.");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.isBlank()) {
            errorLabel.setText("Vui lòng nhập mật khẩu.");
            (isPasswordVisible ? passwordTextField : passwordField).requestFocus();
            return;
        }

        List<User> user;
        try {
            user = userService.findUserByEmailAndPassword(username, password);
        } catch (SQLException e) {
            errorLabel.setText("Không thể kết nối cơ sở dữ liệu.");
            return;
        }

        if (user.isEmpty()) {
            errorLabel.setText("Không tìm thấy tài khoản");
            return;
        }

        AppSession.getInstance().setCurrentUser(user.get(0));
        AppSession.setUsername(username);
        errorLabel.setText("");
        MainApplication.showMainView();
    }
}
