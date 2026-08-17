package com.freshcart.servlet;

import com.freshcart.dao.ProductDAO;
import com.freshcart.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String category = request.getParameter("category");
        String search = request.getParameter("search");

        try {
            List<Product> products = productDAO.getProducts(category, search);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < products.size(); i++) {
                json.append(products.get(i).toJson());
                if (i < products.size() - 1) json.append(",");
            }
            json.append("]");

            out.print(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }
}