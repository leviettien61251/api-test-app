package com.example.apitestapp.repository;

import com.example.apitestapp.db.ConnectionManager;
import com.example.apitestapp.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {


    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";

        List<User> users = new ArrayList<>();

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)
        ) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = User.builder()
                            .id(rs.getString("id"))
                            .roleId(rs.getInt("role_id"))
                            .fullName(rs.getString("full_name"))
                            .phoneNumber(rs.getString("phone"))
                            .email(rs.getString("email"))
                            .password(rs.getString("password"))
                            .isActive(rs.getBoolean("is_active"))
                            .createdAt(rs.getTimestamp("created_at"))
                            .updatedAt(rs.getTimestamp("updated_at"))
                            .build();

                    users.add(user);
                }
            }
        }

        return users;
    }

    public boolean existsUserByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT *  FROM users u WHERE u.email = ? AND u.password = ?";

        List<User> users = new ArrayList<>();

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = User.builder()
                            .id(rs.getString("id"))
                            .roleId(rs.getInt("role_id"))
                            .fullName(rs.getString("full_name"))
                            .phoneNumber(rs.getString("phone"))
                            .email(rs.getString("email"))
                            .password(rs.getString("password"))
                            .isActive(rs.getBoolean("is_active"))
                            .createdAt(rs.getTimestamp("created_at"))
                            .updatedAt(rs.getTimestamp("updated_at"))
                            .build();

                    users.add(user);
                }
            }
        }

        return users.size() > 0;
    }

    public List<User> findUserByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT *  FROM users u WHERE u.email = ? AND u.password = ?";

        List<User> users = new ArrayList<>();

        try (Connection c = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = User.builder()
                            .id(rs.getString("id"))
                            .roleId(rs.getInt("role_id"))
                            .fullName(rs.getString("full_name"))
                            .phoneNumber(rs.getString("phone"))
                            .email(rs.getString("email"))
                            .password(rs.getString("password"))
                            .isActive(rs.getBoolean("is_active"))
                            .createdAt(rs.getTimestamp("created_at"))
                            .updatedAt(rs.getTimestamp("updated_at"))
                            .build();

                    users.add(user);
                }
            }
        }

        return users;
    }
}
