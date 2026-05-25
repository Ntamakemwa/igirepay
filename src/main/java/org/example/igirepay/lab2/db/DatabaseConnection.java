package org.example.igirepay.lab2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:1201/igirepay_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "Olive@123";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (SQLException e) {
                System.out.println("✗ Database connection failed: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();

            }
        } catch (SQLException e) {
            System.out.println("✗ Error closing connection: " + e.getMessage());
        }
    }
}