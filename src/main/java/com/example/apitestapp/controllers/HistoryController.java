package com.example.apitestapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {
    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @FXML
    private ComboBox<String> cbResult;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("HistoryController initialized.");


        cbResult.getItems().addAll(
                "Tất cả",
                "Fail",
                "Pass"
        );
    }
}
