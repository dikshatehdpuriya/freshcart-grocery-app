package com.freshcart.dao;

import com.freshcart.db.DBConnection;
import com.freshcart.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getProducts(String category, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, name, category, price, unit, image_url FROM products WHERE 1=1");

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            sql.append(" AND category = ?");
        }
        if (search != null && !search.isEmpty()) {
            sql.append(" AND LOWER(name) LIKE ?");
        }

        List<Product> products = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
                stmt.setString(paramIndex++, category);
            }
            if (search != null && !search.isEmpty()) {
                stmt.setString(paramIndex++, "%" + search.toLowerCase() + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getString("unit"),
                            rs.getString("image_url")
                    ));
                }
            }
        }
        return products;
    }

    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT id, name, category, price, unit, image_url FROM products WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getString("unit"),
                            rs.getString("image_url")
                    );
                }
            }
        }
        return null;
    }
}