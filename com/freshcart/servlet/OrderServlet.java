package com.freshcart.servlet;

import com.freshcart.dao.OrderDAO;
import com.freshcart.dao.ProductDAO;
import com.freshcart.model.Order;
import com.freshcart.model.OrderItem;
import com.freshcart.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private static final double DELIVERY_FEE = 1.90;

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String userIdParam = request.getParameter("userId");
        if (userIdParam == null) {
            out.print("[]");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdParam);
            List<Order> orders = orderDAO.getOrdersByUser(userId);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < orders.size(); i++) {
                json.append(orders.get(i).toJson());
                if (i < orders.size() - 1) json.append(",");
            }
            json.append("]");

            out.print(json.toString());

        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            out.print("[]");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String name = request.getParameter("name");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");
            String deliveryDate = request.getParameter("deliveryDate");
            String deliverySlot = request.getParameter("deliverySlot");
            String itemsParam = request.getParameter("items");

            if (name == null || phone == null || address == null
                    || deliveryDate == null || deliverySlot == null
                    || itemsParam == null || itemsParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Missing required fields.\"}");
                return;
            }

            List<OrderItem> items = new ArrayList<>();
            double subtotal = 0;

            for (String pair : itemsParam.split(",")) {
                String[] parts = pair.split(":");
                int productId = Integer.parseInt(parts[0].trim());
                int quantity = Integer.parseInt(parts[1].trim());

                Product product = productDAO.getProductById(productId);
                if (product == null) continue;

                OrderItem item = new OrderItem(productId, product.getName(), product.getPrice(), quantity);
                items.add(item);
                subtotal += item.getLineTotal();
            }

            if (items.isEmpty()) {
                out.print("{\"success\":false,\"message\":\"Your cart is empty.\"}");
                return;
            }

            subtotal = Math.round(subtotal * 100.0) / 100.0;
            double total = Math.round((subtotal + DELIVERY_FEE) * 100.0) / 100.0;

            Order order = new Order();
            order.setOrderCode(generateOrderCode());
            order.setUserId(userId);
            order.setCustomerName(name);
            order.setPhone(phone);
            order.setAddress(address);
            order.setDeliveryDate(deliveryDate);
            order.setDeliverySlot(deliverySlot);
            order.setSubtotal(subtotal);
            order.setDeliveryFee(DELIVERY_FEE);
            order.setTotal(total);
            order.setItems(items);

            Order saved = orderDAO.createOrder(order);
            saved.setPlacedAt(java.time.LocalDateTime.now().toString());

            out.print("{\"success\":true,\"order\":" + saved.toJson() + "}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Server error placing order.\"}");
        }
    }

    private String generateOrderCode() {
        int num = 1000 + new Random().nextInt(9000);
        return "GCR-" + num;
    }
}