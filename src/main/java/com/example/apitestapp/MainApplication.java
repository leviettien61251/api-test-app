package com.example.apitestapp;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class MainApplication extends Application {
    private static final String APP_ICON_PATH = "/com/example/apitestapp/icons/app-icon.png";
    private static Stage primaryStage;
    private static Scene scene;

    private static Parent loadRoot(String fxmlName) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlName));
        return loader.load();
    }

    public static void showMainView() {
        setRootSafely("main-view.fxml");
    }

    public static void showLoginView() {
        setRootSafely("login-view.fxml");
    }

    private static void setRootSafely(String fxmlName) {
        try {
            Parent root = loadRoot(fxmlName);
            scene.setRoot(root);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlName, e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        Parent root = loadRoot("login-view.fxml");
        scene = new Scene(root);
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        String CSS_PATH = "/com/example/apitestapp/styles/main.css";
        java.net.URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl == null) {
            throw new IllegalStateException("Stylesheet not found: " + CSS_PATH);
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setTitle("API Test App");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.getIcons().add(new Image(Objects.requireNonNull(
                MainApplication.class.getResource(APP_ICON_PATH),
                "App icon not found: " + APP_ICON_PATH
        ).toExternalForm()));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        stage.setOnCloseRequest(event -> {
            Alert closeAlert = new Alert(Alert.AlertType.CONFIRMATION);
            closeAlert.setTitle("Thoát ứng dụng");
            closeAlert.setHeaderText("Bạn có chắc chắn muốn thoát ứng dụng?");

            ButtonType yesButton = new ButtonType("Có");

            ButtonType cancelButton = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);

            closeAlert.getButtonTypes().setAll(yesButton, cancelButton);

            Optional<ButtonType> result = closeAlert.showAndWait();
            if (result.get() == yesButton) {
                Platform.exit();
            } else if (result.get() == cancelButton) {
                event.consume();
            }

        });
    }
}
