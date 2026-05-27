package org.example.igirepay.lab3.reports;

import org.example.igirepay.lab2.db.DatabaseConnection;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReportGenerator {

    public void exportTransactionHistoryToCSV(String accountId) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        String fileName = "transactions_" + accountId + "_" + LocalDate.now() + ".csv";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             FileWriter writer = new FileWriter(fileName)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            writer.write("Transaction ID,Account ID,Reference ID,Type,Amount,Fee,Status,Description,Date\n");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                writer.write(
                        rs.getString("id") + "," +
                                rs.getString("account_id") + "," +
                                rs.getString("reference_id") + "," +
                                rs.getString("transaction_type") + "," +
                                rs.getDouble("amount") + "," +
                                rs.getDouble("fee") + "," +
                                rs.getString("status") + "," +
                                rs.getString("description") + "," +
                                rs.getTimestamp("created_at") + "\n"
                );
            }

            if (!hasRecords) {
                System.out.println("✗ No transactions found for account: " + accountId);
                return;
            }

            System.out.println("✓ Transaction history exported to: " + fileName);

        } catch (SQLException | IOException e) {
            System.out.println("✗ Export failed: " + e.getMessage());
        }
    }

    public void viewDailyTransactionSummary(String accountId) {
        String sql = "SELECT transaction_type, COUNT(*) as total_count, " +
                "SUM(amount) as total_amount, SUM(fee) as total_fees " +
                "FROM transactions " +
                "WHERE account_id = ? AND DATE(created_at) = CURRENT_DATE " +
                "GROUP BY transaction_type";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n=== Daily Transaction Summary (" + LocalDate.now() + ") ===");
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.println("Type:         " + rs.getString("transaction_type"));
                System.out.println("Count:        " + rs.getInt("total_count"));
                System.out.println("Total Amount: " + rs.getDouble("total_amount") + " RWF");
                System.out.println("Total Fees:   " + rs.getDouble("total_fees") + " RWF");
                System.out.println("----------------------------");
            }
            if (!hasRecords) System.out.println("No transactions today.");
            System.out.println("============================================");

        } catch (SQLException e) {
            System.out.println("✗ Error fetching daily summary: " + e.getMessage());
        }
    }

    public void viewCustomerStatement(String accountId, String fullName) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n========================================");
            System.out.println("         IGIREPAY ACCOUNT STATEMENT     ");
            System.out.println("========================================");
            System.out.println("Customer: " + fullName);
            System.out.println("Account:  " + accountId);
            System.out.println("Date:     " + LocalDate.now());
            System.out.println("----------------------------------------");

            double totalIn = 0;
            double totalOut = 0;
            boolean hasRecords = false;

            while (rs.next()) {
                hasRecords = true;
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                double fee = rs.getDouble("fee");
                String status = rs.getString("status");
                String date = rs.getTimestamp("created_at").toString();

                if (type.equals("DEPOSIT") || type.equals("RECEIVE") ||
                        type.equals("MOKASH_WITHDRAWAL")) {
                    totalIn += amount;
                } else {
                    totalOut += (amount + fee);
                }

                System.out.println("Date:   " + date);
                System.out.println("Type:   " + type);
                System.out.println("Amount: " + amount + " RWF");
                System.out.println("Fee:    " + fee + " RWF");
                System.out.println("Status: " + status);
                System.out.println("----------------------------------------");
            }

            if (!hasRecords) {
                System.out.println("No transactions found.");
            } else {
                System.out.println("Total Money In:  " + totalIn + " RWF");
                System.out.println("Total Money Out: " + totalOut + " RWF");
                System.out.println("Net:             " + (totalIn - totalOut) + " RWF");
            }
            System.out.println("========================================");

        } catch (SQLException e) {
            System.out.println("✗ Error generating statement: " + e.getMessage());
        }
    }

    public void searchTransactions(String accountId, String keyword) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? " +
                "AND (transaction_type ILIKE ? OR description ILIKE ? OR status ILIKE ?) " +
                "ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            stmt.setString(4, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n=== Search Results for: " + keyword + " ===");
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
            if (!hasRecords) System.out.println("No results found for: " + keyword);
            System.out.println("===========================");

        } catch (SQLException e) {
            System.out.println("✗ Error searching transactions: " + e.getMessage());
        }
    }

    public void filterByStatus(String accountId, String status) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? AND status = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            stmt.setString(2, status.toUpperCase());
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n=== Transactions with status: " + status.toUpperCase() + " ===");
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.println("----------------------------");
                System.out.println("Type:   " + rs.getString("transaction_type"));
                System.out.println("Amount: " + rs.getDouble("amount") + " RWF");
                System.out.println("Fee:    " + rs.getDouble("fee") + " RWF");
                System.out.println("Desc:   " + rs.getString("description"));
                System.out.println("Date:   " + rs.getTimestamp("created_at"));
            }
            if (!hasRecords) System.out.println("No " + status + " transactions found.");
            System.out.println("===========================");

        } catch (SQLException e) {
            System.out.println("✗ Error filtering transactions: " + e.getMessage());
        }
    }

    public void exportDailySummaryToCSV(String accountId) {
        String sql = "SELECT transaction_type, COUNT(*) as total_count, " +
                "SUM(amount) as total_amount, SUM(fee) as total_fees " +
                "FROM transactions " +
                "WHERE account_id = ? AND DATE(created_at) = CURRENT_DATE " +
                "GROUP BY transaction_type";
        String fileName = "daily_summary_" + accountId + "_" + LocalDate.now() + ".csv";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             FileWriter writer = new FileWriter(fileName)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            writer.write("Type,Count,Total Amount,Total Fees\n");
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                writer.write(
                        rs.getString("transaction_type") + "," +
                                rs.getInt("total_count") + "," +
                                rs.getDouble("total_amount") + "," +
                                rs.getDouble("total_fees") + "\n"
                );
            }

            if (!hasRecords) {
                System.out.println("✗ No transactions today to export.");
                return;
            }
            System.out.println("✓ Daily summary exported to: " + fileName);

        } catch (SQLException | IOException e) {
            System.out.println("✗ Export failed: " + e.getMessage());
        }
    }
}
