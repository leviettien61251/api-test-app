package com.example.apitestapp;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        String CSS_PATH = "/com/example/apitestapp/styles/styles.css";
        java.net.URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl == null) {
            throw new IllegalStateException("Stylesheet not found: " + CSS_PATH);
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setTitle("Main");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }
}
