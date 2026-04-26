package com.example.apitestapp.repository;

import com.example.apitestapp.models.Person;

import java.util.List;

public interface DashboardRepository{
    List<Person> findAllPerson();
    void savePerson(Person person);
    Person updatePerson(Person person);
    void deletePerson(Person person);
}
