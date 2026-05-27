package org.example.igirepay.lab2.dao;

import org.example.igirepay.lab2.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {

    public static class AccountInfo {
        public final String id;
        public final String customerId;
        public final String accountType;
        public final double balance;
        public final String pin;
        public final boolean isLocked;
        public final int failedPinAttempts;

        public AccountInfo(String id, String customerId, String accountType,
                           double balance, String pin, boolean isLocked, int failedPinAttempts) {
            this.id = id;
            this.customerId = customerId;
            this.accountType = accountType;
            this.balance = balance;
            this.pin = pin;
            this.isLocked = isLocked;
            this.failedPinAttempts = failedPinAttempts;
        }
    }

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
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error fetching balance: " + e.getMessage());
        }
        return 0;
    }

    public String getPin(String accountId) {
        String sql = "SELECT pin FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("pin");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error fetching PIN: " + e.getMessage());
        }
        return null;
    }

    public boolean accountExists(String accountId) {
        String sql = "SELECT id FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("✗ Error checking account: " + e.getMessage());
            return false;
        }
    }

    public AccountInfo getAccountByCustomerAndType(String customerId, String accountType) {
        String sql = "SELECT id, customer_id, account_type, balance, pin, " +
                "COALESCE(is_locked, false) AS is_locked, " +
                "COALESCE(failed_pin_attempts, 0) AS failed_pin_attempts " +
                "FROM accounts WHERE customer_id = ? AND account_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            stmt.setString(2, accountType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AccountInfo(
                            rs.getString("id"),
                            rs.getString("customer_id"),
                            rs.getString("account_type"),
                            rs.getDouble("balance"),
                            rs.getString("pin"),
                            rs.getBoolean("is_locked"),
                            rs.getInt("failed_pin_attempts")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("✗ Error fetching account: " + e.getMessage());
        }
        return null;
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

    public boolean updatePin(String accountId, String newHashedPin) {
        String sql = "UPDATE accounts SET pin = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPin);
            stmt.setString(2, accountId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("✗ Error updating PIN: " + e.getMessage());
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
}