package com.freshcart.dao;

import com.freshcart.db.DBConnection;
import com.freshcart.model.Order;
import com.freshcart.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public Order createOrder(Order order) throws SQLException {
        String insertOrderSql = "INSERT INTO orders "
                + "(order_code, user_id, customer_name, phone, address, delivery_date, delivery_slot, subtotal, delivery_fee, total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertItemSql = "INSERT INTO order_items "
                + "(order_id, product_id, product_name, price, quantity, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int orderId;
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, order.getOrderCode());
                stmt.setInt(2, order.getUserId());
                stmt.setString(3, order.getCustomerName());
                stmt.setString(4, order.getPhone());
                stmt.setString(5, order.getAddress());
                stmt.setString(6, order.getDeliveryDate());
                stmt.setString(7, order.getDeliverySlot());
                stmt.setDouble(8, order.getSubtotal());
                stmt.setDouble(9, order.getDeliveryFee());
                stmt.setDouble(10, order.getTotal());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    orderId = keys.getInt(1);
                    order.setId(orderId);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertItemSql)) {
                for (OrderItem item : order.getItems()) {
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, item.getProductId());
                    stmt.setString(3, item.getProductName());
                    stmt.setDouble(4, item.getPrice());
                    stmt.setInt(5, item.getQuantity());
                    stmt.setDouble(6, item.getLineTotal());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return order;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Order> getOrdersByUser(int userId) throws SQLException {
        String orderSql = "SELECT * FROM orders WHERE user_id = ? ORDER BY placed_at DESC";
        String itemSql = "SELECT * FROM order_items WHERE order_id = ?";

        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {

            orderStmt.setInt(1, userId);

            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setOrderCode(rs.getString("order_code"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setCustomerName(rs.getString("customer_name"));
                    order.setPhone(rs.getString("phone"));
                    order.setAddress(rs.getString("address"));
                    order.setDeliveryDate(rs.getString("delivery_date"));
                    order.setDeliverySlot(rs.getString("delivery_slot"));
                    order.setSubtotal(rs.getDouble("subtotal"));
                    order.setDeliveryFee(rs.getDouble("delivery_fee"));
                    order.setTotal(rs.getDouble("total"));
                    order.setPlacedAt(rs.getTimestamp("placed_at").toString());

                    List<OrderItem> items = new ArrayList<>();
                    try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                        itemStmt.setInt(1, order.getId());
                        try (ResultSet itemRs = itemStmt.executeQuery()) {
                            while (itemRs.next()) {
                                OrderItem item = new OrderItem();
                                item.setProductId(itemRs.getInt("product_id"));
                                item.setProductName(itemRs.getString("product_name"));
                                item.setPrice(itemRs.getDouble("price"));
                                item.setQuantity(itemRs.getInt("quantity"));
                                item.setLineTotal(itemRs.getDouble("line_total"));
                                items.add(item);
                            }
                        }
                    }
                    order.setItems(items);
                    orders.add(order);
                }
            }
        }
        return orders;
    }
}