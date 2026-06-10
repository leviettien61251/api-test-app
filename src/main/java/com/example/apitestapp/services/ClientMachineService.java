package com.example.apitestapp.services;

import com.example.apitestapp.models.entity.ClientMachine;
import com.example.apitestapp.repository.ClientMachineRepository;

import java.sql.SQLException;

public class ClientMachineService {
    private final ClientMachineRepository clientMachineRepository = new ClientMachineRepository();

    public ClientMachineService() {
    }

    public void save(ClientMachine cm) throws SQLException {
        try {
            clientMachineRepository.save(cm);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
