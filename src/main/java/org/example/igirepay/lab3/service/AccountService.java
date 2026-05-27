package org.example.igirepay.lab3.service;

import org.example.igirepay.lab1.exception.InsufficientBalanceException;
import org.example.igirepay.lab1.exception.InvalidAccountException;
import org.example.igirepay.lab1.model.FeeCalculator;
import org.example.igirepay.lab2.dao.AccountDAO;
import org.example.igirepay.lab2.dao.TransactionDAO;
import org.example.igirepay.lab2.db.DatabaseConnection;
import org.example.igirepay.lab1.model.Customer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class AccountService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public boolean createAccount(String accountId, String customerId,
                                 String accountType, double balance, String pin) {
        if (!Customer.isValidPin(pin)) {
            System.out.println("✗ Invalid PIN.");
            return false;
        }
        boolean created = accountDAO.createAccount(accountId, customerId, accountType, balance, pin);
        if (created) System.out.println("✓ Account created: " + accountType);
        return created;
    }

    public double getBalance(String accountId) throws InvalidAccountException {
        if (!accountDAO.accountExists(accountId)) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        return accountDAO.getBalance(accountId);
    }

    public void deposit(String accountId, double amount) throws InvalidAccountException {
        if (!accountDAO.accountExists(accountId)) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            double currentBalance = accountDAO.getBalance(accountId);
            double newBalance = currentBalance + amount;
            accountDAO.updateBalance(accountId, newBalance);
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), accountId,
                    "DEP-" + UUID.randomUUID(), "DEPOSIT",
                    amount, 0, "SUCCESS", "Deposit"
            );
            conn.commit();
            System.out.println("✓ Deposited " + amount + " RWF. New balance: " + newBalance + " RWF");
        } catch (SQLException e) {
            rollback(conn);
            System.out.println("✗ Deposit failed: " + e.getMessage());
        } finally {
            resetAutoCommit(conn);
        }
    }

    public void withdraw(String accountId, double amount)
            throws InvalidAccountException, InsufficientBalanceException {
        if (!accountDAO.accountExists(accountId)) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            double currentBalance = accountDAO.getBalance(accountId);
            double fee = FeeCalculator.getWithdrawFee(amount);
            double total = amount + fee;
            if (currentBalance < total) {
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), accountId,
                        "WIT-ERR-" + UUID.randomUUID(), "WITHDRAWAL",
                        amount, fee, "FAILED",
                        "Insufficient balance. Have: " + currentBalance + " Need: " + total
                );
                conn.commit();
                throw new InsufficientBalanceException(
                        "Insufficient balance. Have: " + currentBalance + " RWF, Need: " + total + " RWF");
            }
            double newBalance = currentBalance - total;
            accountDAO.updateBalance(accountId, newBalance);
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), accountId,
                    "WIT-" + UUID.randomUUID(), "WITHDRAWAL",
                    amount, fee, "SUCCESS", "Withdrawal"
            );
            conn.commit();
            System.out.println("✓ Withdrawn " + amount + " RWF (fee: " + fee + " RWF)");
            System.out.println("  New balance: " + newBalance + " RWF");
        } catch (SQLException e) {
            rollback(conn);
            System.out.println("✗ Withdrawal failed: " + e.getMessage());
        } finally {
            resetAutoCommit(conn);
        }
    }

    public void transfer(String fromAccountId, String toAccountId, double amount)
            throws InvalidAccountException, InsufficientBalanceException {
        if (!accountDAO.accountExists(fromAccountId)) {
            throw new InvalidAccountException("Sender account not found.");
        }
        if (!accountDAO.accountExists(toAccountId)) {
            throw new InvalidAccountException("Recipient account not found.");
        }
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            double senderBalance = accountDAO.getBalance(fromAccountId);
            boolean isOnNet = toAccountId.startsWith("W-078") || toAccountId.startsWith("W-079");
            double fee = isOnNet ?
                    FeeCalculator.getSendOnNetFee(amount) :
                    FeeCalculator.getSendOffNetFee(amount);
            double total = amount + fee;
            if (senderBalance < total) {
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), fromAccountId,
                        "SND-ERR-" + UUID.randomUUID(), "SEND_LOCAL",
                        amount, fee, "FAILED",
                        "Insufficient balance. Have: " + senderBalance + " Need: " + total
                );
                conn.commit();
                throw new InsufficientBalanceException(
                        "Insufficient balance. Have: " + senderBalance + " RWF, Need: " + total + " RWF");
            }
            double newSenderBalance = senderBalance - total;
            double receiverBalance = accountDAO.getBalance(toAccountId);
            double newReceiverBalance = receiverBalance + amount;
            accountDAO.updateBalance(fromAccountId, newSenderBalance);
            accountDAO.updateBalance(toAccountId, newReceiverBalance);
            String refId = "SND-" + UUID.randomUUID();
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), fromAccountId,
                    refId, "SEND_LOCAL", amount, fee, "SUCCESS",
                    "Sent to " + toAccountId
            );
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), toAccountId,
                    "RCV-" + UUID.randomUUID(), "RECEIVE", amount, 0, "SUCCESS",
                    "Received from " + fromAccountId
            );
            conn.commit();
            System.out.println("✓ Transferred " + amount + " RWF (fee: " + fee + " RWF)");
            System.out.println("  Your new balance: " + newSenderBalance + " RWF");
        } catch (SQLException e) {
            rollback(conn);
            System.out.println("✗ Transfer failed: " + e.getMessage());
        } finally {
            resetAutoCommit(conn);
        }
    }

    public boolean deleteInactiveAccount(String accountId) throws InvalidAccountException {
        if (!accountDAO.accountExists(accountId)) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        if (accountDAO.getBalance(accountId) > 0) {
            System.out.println("✗ Cannot delete account with remaining balance.");
            return false;
        }
        boolean deleted = accountDAO.deleteAccount(accountId);
        if (deleted) System.out.println("✓ Inactive account deleted.");
        return deleted;
    }

    private void rollback(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
                System.out.println("✗ Transaction rolled back.");
            }
        } catch (SQLException e) {
            System.out.println("✗ Rollback failed: " + e.getMessage());
        }
    }

    private void resetAutoCommit(Connection conn) {
        try {
            if (conn != null) conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("✗ Error resetting connection: " + e.getMessage());
        }
    }
}
