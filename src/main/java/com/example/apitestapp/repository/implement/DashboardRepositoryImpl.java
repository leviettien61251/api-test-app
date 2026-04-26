package com.example.apitestapp.repository.implement;

import com.example.apitestapp.db.ConnectionManager;
import com.example.apitestapp.models.Person;
import com.example.apitestapp.repository.DashboardRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepositoryImpl implements DashboardRepository {
    /**
     * @return
     */
    @Override
    public List<Person> findAllPerson() {
        String query = "SELECT * FROM person";
        List<Person> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Person person = Person.builder()
                        .id(rs.getString("id"))
                        .firstName(rs.getString("firstname"))
                        .lastName(rs.getString("lastname"))
                        .build();
                list.add(person);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    @Override
    public ObservableList<Person> findAllPerson_() {
        String query = "SELECT * FROM person";
        ObservableList<Person> list = FXCollections.observableArrayList();
        try (Connection conn = ConnectionManager.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Person person = Person.builder()
                        .id(rs.getString("id"))
                        .firstName(rs.getString("firstname"))
                        .lastName(rs.getString("lastname"))
                        .build();
                list.add(person);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    /**
     * @param person
     */
    @Override
    public void savePerson(Person person) {
        String query = "INSERT INTO person (firstname, lastname) VALUES ('ABCD', 'EFGH')";
        try (Connection conn = ConnectionManager.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param person
     * @return
     */
    @Override
    public Person updatePerson(Person person) {
        return person;
    }

    /**
     * @param person
     */
    @Override
    public void deletePerson(Person person) {

    }
}
