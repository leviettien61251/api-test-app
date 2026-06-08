package com.example.apitestapp.services;

import com.example.apitestapp.models.User;
import com.example.apitestapp.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public UserService() {
    }

    public List<User> findUserByEmailAndPassword(String email, String password) throws SQLException {
        return userRepository.findUserByEmailAndPassword(email, password);
    }
}
