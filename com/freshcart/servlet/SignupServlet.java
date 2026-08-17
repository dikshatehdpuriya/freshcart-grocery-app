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

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Please fill in every field.\"}");
            return;
        }

        try {
            User existing = userDAO.findByEmail(email.trim());
            if (existing != null) {
                out.print("{\"success\":false,\"message\":\"An account with that email already exists.\"}");
                return;
            }

            User created = userDAO.createUser(name.trim(), email.trim(), password);
            out.print("{\"success\":true,\"user\":" + created.toJson() + "}");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Server error. Please try again.\"}");
        }
    }
}