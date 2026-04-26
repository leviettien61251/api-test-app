package com.example.apitestapp.repository;

import com.example.apitestapp.models.Person;
import javafx.collections.ObservableList;

import java.util.List;

public interface DashboardRepository{
    List<Person> findAllPerson();
    ObservableList<Person> findAllPerson_();
    void savePerson(Person person);
    Person updatePerson(Person person);
    void deletePerson(Person person);
}
