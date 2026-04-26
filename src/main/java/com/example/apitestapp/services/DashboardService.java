package com.example.apitestapp.services;

import com.example.apitestapp.models.Person;
import com.example.apitestapp.repository.implement.DashboardRepositoryImpl;
import javafx.collections.ObservableList;

import java.util.List;

public class DashboardService {
    private final DashboardRepositoryImpl dashboardRepositoryImpl;

    public DashboardService() {
        this.dashboardRepositoryImpl = new DashboardRepositoryImpl();
    }

    public List<Person> getAllPerson() {
        return dashboardRepositoryImpl.findAllPerson();
    }

    public ObservableList<Person> getAllPerson_() {
        return dashboardRepositoryImpl.findAllPerson_();
    }

    public void savePerson(Person person) {
        dashboardRepositoryImpl.savePerson(person);
    }

}
