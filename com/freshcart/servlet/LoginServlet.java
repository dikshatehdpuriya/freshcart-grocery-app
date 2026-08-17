package com.freshcart.servlet;

import com.freshcart.dao.UserDAO;
import com.freshcart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Please fill in every field.\"}");
            return;
        }

        try {
            User user = userDAO.findByEmail(email.trim());

            if (user == null || !user.getPassword().equals(password)) {
                out.print("{\"success\":false,\"message\":\"Incorrect email or password.\"}");
                return;
            }

            out.print("{\"success\":true,\"user\":" + user.toJson() + "}");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Server error. Please try again.\"}");
        }
    }
}