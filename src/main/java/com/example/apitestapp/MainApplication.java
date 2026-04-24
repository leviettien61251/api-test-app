package com.example.apitestapp;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        String CSS_PATH = "/com/example/apitestapp/styles/styles.css";
        java.net.URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl == null) {
            throw new IllegalStateException("Stylesheet not found: " + CSS_PATH);
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setTitle("Main");
        stage.setScene(scene);
        stage.show();
    }
}
