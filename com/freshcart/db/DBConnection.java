package com.freshcart.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }

        Properties props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("db.properties not found. Create it in src/main/java.");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read db.properties", e);
        }

        URL = props.getProperty("db.url");
        USERNAME = props.getProperty("db.username");
        PASSWORD = props.getProperty("db.password");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}