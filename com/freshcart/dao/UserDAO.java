package com.freshcart.dao;

import com.freshcart.db.DBConnection;
import com.freshcart.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    return user;
                }
            }
        }
        return null;
    }

    public User createUser(String name, String email, String password) throws SQLException {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                User user = new User(name, email, password);
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
                return user;
            }
        }
    }
}