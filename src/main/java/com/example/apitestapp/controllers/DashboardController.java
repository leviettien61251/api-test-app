package com.example.apitestapp.controllers;

import com.example.apitestapp.models.Person;
import com.example.apitestapp.models.Role;
import com.example.apitestapp.services.DashboardService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
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

    }

    @FXML
    Button btnGet;
    @FXML
    TableView<Person> tableViewTest = new TableView<Person>();
    @FXML
    TableColumn<Person, String> colId, colFirstname, colLastname;

    @FXML
    private void handleClick() {

        List<Person> list = dashboardService.getAllPerson();
        ObservableList<Person> items = dashboardService.getAllPerson_();
        for (Person person : list) {
            System.out.println(person.getId() + " " + person.getFirstName() + " " + person.getLastName());
        }
        tableViewTest.setItems(items);
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colFirstname.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        colLastname.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        tableViewTest.getColumns().clear();
        tableViewTest.getColumns().addAll(colId, colFirstname, colLastname);
    }

    @FXML
    private void AddNew() {
        Person person = new Person();
        dashboardService.savePerson(person);
        System.out.println("Person saved successfully.");
    }

}
