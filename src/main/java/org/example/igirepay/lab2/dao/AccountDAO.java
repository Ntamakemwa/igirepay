package org.example.igirepay.lab2.dao;

import org.example.igirepay.lab2.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {


    public boolean createAccount(String accountId, String customerId,
                                 String accountType, double balance, String pin) {
        String sql = "INSERT INTO accounts (id, customer_id, account_type, balance, pin) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            stmt.setString(2, customerId);
            stmt.setString(3, accountType);
            stmt.setDouble(4, balance);
            stmt.setString(5, pin);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error creating account: " + e.getMessage());
            return false;
        }
    }


    public double getBalance(String accountId) {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("balance");

        } catch (SQLException e) {
            System.out.println("✗ Error fetching balance: " + e.getMessage());
        }
        return 0;
    }


    public boolean updateBalance(String accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error updating balance: " + e.getMessage());
            return false;
        }
    }


    public boolean updateLockStatus(String accountId, boolean isLocked, int failedAttempts) {
        String sql = "UPDATE accounts SET is_locked = ?, failed_pin_attempts = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isLocked);
            stmt.setInt(2, failedAttempts);
            stmt.setString(3, accountId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error updating lock status: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteAccount(String accountId) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("✗ Error deleting account: " + e.getMessage());
            return false;
        }
    }


    public boolean accountExists(String accountId) {
        String sql = "SELECT id FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("✗ Error checking account: " + e.getMessage());
            return false;
        }
    }


    public String getPin(String accountId) {
        String sql = "SELECT pin FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("pin");

        } catch (SQLException e) {
            System.out.println("✗ Error fetching PIN: " + e.getMessage());
        }
        return null;
    }


    public ResultSet getAccountByCustomerAndType(String customerId, String accountType) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ? AND account_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            stmt.setString(2, accountType);
            return stmt.executeQuery();

        } catch (SQLException e) {
            System.out.println("✗ Error fetching account: " + e.getMessage());
        }
        return null;
    }
}
