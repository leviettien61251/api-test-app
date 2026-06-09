package com.example.apitestapp.repository;

import com.example.apitestapp.db.ConnectionManager;
import com.example.apitestapp.models.entity.ClientMachine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientMachineRepository {


    public void save(ClientMachine cm) throws SQLException {
        String sql = "INSERT INTO client_machines(user_id, machine_name, os, ip_address, hostname) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cm.getUserId());
            ps.setString(2, cm.getMachineName());
            ps.setString(3, cm.getOs());
            ps.setString(4, cm.getIpAddress());
            ps.setString(5, cm.getHostname());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
        }
    }
}
