package com.example.apitestapp.controllers;

import com.example.apitestapp.MainApplication;
import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.models.User;
import com.example.apitestapp.repository.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
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

    UserRepository userRepository = new UserRepository();

    @FXML
    private void handleLogin() throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        List<User> user = userRepository.findUserByEmailAndPassword(username, password);
        AppSession.getInstance().setCurrentUser(user.get(0));

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
        if (user.isEmpty()) {
            errorLabel.setText("Không tìm thấy tài khoản");
            return;
        }
        AppSession.setUsername(username);
        AppSession.setRole(role);
        errorLabel.setText("");
        MainApplication.showMainView();


    }
}

