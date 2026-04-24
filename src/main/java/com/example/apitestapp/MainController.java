package com.example.apitestapp;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final Map<String, Node> viewCache = new HashMap<>();
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnDashboard, btnTestcase, btnRequest, btnReport, btnCollections, btnEnvironments, btnHistory, btnProfile;
    private Button activeButton;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigateTo("views/dashboard-view.fxml", btnDashboard);
    }

    @FXML
    private void navigateDashboard() {
        navigateTo("views/dashboard-view.fxml", btnDashboard);
    }

    @FXML
    private void navigateTestcases() {
        navigateTo("views/testcase-view.fxml", btnTestcase);
    }

    @FXML
    private void navigateRequests() {
        navigateTo("views/request-view.fxml", btnRequest);
    }

    @FXML
    private void navigateReports() {
        navigateTo("views/report-view.fxml", btnReport);
    }

    @FXML
    private void navigateProfile() {
        navigateTo("views/profile-view.fxml", btnProfile);
    }

    @FXML
    private void navigateCollections() {
        navigateTo("views/collections-view.fxml", btnCollections);
    }

    @FXML
    private void navigateEnvironments() {
        navigateTo("views/environments-view.fxml", btnEnvironments);
    }

    @FXML
    private void navigateHistory() {
        navigateTo("views/history-view.fxml", btnHistory);
    }

    private void navigateTo(String fxmlPath, Button button) {
        try {
            if (!viewCache.containsKey(fxmlPath)) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(fxmlPath)
                );
                Node view = loader.load();
                viewCache.put(fxmlPath, view);
            }

            Node view = viewCache.get(fxmlPath);
            FadeTransition fade = new FadeTransition(Duration.millis(200), view);
            fade.setFromValue(0);
            fade.setToValue(1);
            contentArea.getChildren().setAll(view);
            fade.play();

            setActive(button);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button btn) {
        if (activeButton != null)
            activeButton.getStyleClass().remove("nav-button-active");
        activeButton = btn;
        btn.getStyleClass().add("nav-button-active");
    }
}
