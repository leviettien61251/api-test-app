package com.example.apitestapp.controllers;

import com.example.apitestapp.models.Person;
import com.example.apitestapp.services.DashboardService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    private final DashboardService dashboardService;

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

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
        System.out.println("DashboardController initialized.");
//        col1.setCellValueFactory(cellData -> cellData.getValue().getFirstName());
//        col2.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
//        table1.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
//            if (newSelection != null) {
//                // Fill the text fields with the data from the selected person
//                txtFirstName.setText(newSelection.getFirstName());
//                txtLastName.setText(newSelection.getLastName());
//            }
//        });
    }

    @FXML
    Button btnClick, btnAddNew;
    @FXML
    private TextField txtFirstName, txtLastName;
    @FXML
    private TableView<Person> table1;
    @FXML
    private TableColumn<Person, String> col1, col2;

    @FXML
    private void handleClick() {

        List<Person> list = dashboardService.getAllPerson();
        for(Person person : list){
            System.out.println(person.getFirstName() + " " + person.getLastName());
        }
    }

    @FXML
    private void AddNew() {
        Person person = new Person();
        dashboardService.savePerson(person);
        System.out.println("Person saved successfully.");

    }
    private void LoadData() {
    }

}
