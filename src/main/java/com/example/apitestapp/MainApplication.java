package com.example.apitestapp;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    private static Stage primaryStage;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        Parent root = loadRoot("login-view.fxml");
        scene = new Scene(root);
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        String CSS_PATH = "/com/example/apitestapp/styles/styles.css";
        java.net.URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl == null) {
            throw new IllegalStateException("Stylesheet not found: " + CSS_PATH);
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setTitle("API Test App");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

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
}
