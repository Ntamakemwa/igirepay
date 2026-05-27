package org.example.igirepay.lab2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:postgresql://localhost:1201/igirepay_db";

    private static final String USERNAME = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "postgres";

    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "Olive@123";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("✗ Database connection failed: " + e.getMessage());
            throw e;
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("✗ Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        // No-op: each connection is closed by its own try-with-resources block
    }
}