package org.example.igirepay.lab2.dao;

import org.example.igirepay.lab2.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessedRequestDAO {


    public boolean saveProcessedRequest(String referenceId) {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error saving processed request: " + e.getMessage());
            return false;
        }
    }


    public boolean isAlreadyProcessed(String referenceId) {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("✗ Error checking processed request: " + e.getMessage());
            return false;
        }
    }


    public void printAllProcessedRequests() {
        String sql = "SELECT * FROM processed_requests ORDER BY processed_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            System.out.println("\n=== Processed Requests ===");
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.println("Ref: " + rs.getString("reference_id") +
                        " | At: " + rs.getTimestamp("processed_at"));
            }
            if (!hasRecords) System.out.println("No processed requests.");
            System.out.println("==========================");

        } catch (SQLException e) {
            System.out.println("✗ Error fetching processed requests: " + e.getMessage());
        }
    }


    public boolean deleteProcessedRequest(String referenceId) {
        String sql = "DELETE FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error deleting processed request: " + e.getMessage());
            return false;
        }
    }
}
