package org.example.igirepay.lab2.dao;

import org.example.igirepay.lab2.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionDAO {


    public boolean createTransaction(String transactionId, String accountId,
                                     String referenceId, String transactionType,
                                     double amount, double fee,
                                     String status, String description) {
        String sql = "INSERT INTO transactions (id, account_id, reference_id, " +
                "transaction_type, amount, fee, status, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transactionId);
            stmt.setString(2, accountId);
            stmt.setString(3, referenceId);
            stmt.setString(4, transactionType);
            stmt.setDouble(5, amount);
            stmt.setDouble(6, fee);
            stmt.setString(7, status);
            stmt.setString(8, description);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error saving transaction: " + e.getMessage());
            return false;
        }
    }


    public void printTransactionHistory(String accountId) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n=== Transaction History ===");
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.println("----------------------------");
                System.out.println("ID:     " + rs.getString("id"));
                System.out.println("Type:   " + rs.getString("transaction_type"));
                System.out.println("Amount: " + rs.getDouble("amount") + " RWF");
                System.out.println("Fee:    " + rs.getDouble("fee") + " RWF");
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Desc:   " + rs.getString("description"));
                System.out.println("Date:   " + rs.getTimestamp("created_at"));
            }
            if (!hasRecords) System.out.println("No transactions found.");
            System.out.println("===========================");

        } catch (SQLException e) {
            System.out.println("✗ Error fetching transactions: " + e.getMessage());
        }
    }


    public void printFailedTransactions(String accountId) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? AND status = 'FAILED' ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n=== Failed Transactions ===");
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.println("----------------------------");
                System.out.println("Ref:    " + rs.getString("reference_id"));
                System.out.println("Type:   " + rs.getString("transaction_type"));
                System.out.println("Amount: " + rs.getDouble("amount") + " RWF");
                System.out.println("Reason: " + rs.getString("description"));
                System.out.println("Date:   " + rs.getTimestamp("created_at"));
            }
            if (!hasRecords) System.out.println("No failed transactions.");
            System.out.println("===========================");

        } catch (SQLException e) {
            System.out.println("✗ Error fetching failed transactions: " + e.getMessage());
        }
    }


    public boolean deleteTransaction(String transactionId) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transactionId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error deleting transaction: " + e.getMessage());
            return false;
        }
    }


    public boolean transactionExists(String referenceId) {
        String sql = "SELECT id FROM transactions WHERE reference_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("✗ Error checking transaction: " + e.getMessage());
            return false;
        }
    }
}
