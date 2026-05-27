package org.example.igirepay.lab3.service;

import org.example.igirepay.lab1.exception.InsufficientBalanceException;
import org.example.igirepay.lab1.exception.InvalidAccountException;
import org.example.igirepay.lab1.model.FeeCalculator;
import org.example.igirepay.lab2.dao.AccountDAO;
import org.example.igirepay.lab2.dao.ProcessedRequestDAO;
import org.example.igirepay.lab2.dao.TransactionDAO;
import org.example.igirepay.lab2.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();

    public void sendMoneyLocal(String fromAccountId, String toPhone,
                               double amount, boolean isOnNet)
            throws InvalidAccountException, InsufficientBalanceException {
        if (!accountDAO.accountExists(fromAccountId)) {
            throw new InvalidAccountException("Sender account not found.");
        }
        String refId = "SND-" + UUID.randomUUID();
        if (processedRequestDAO.isAlreadyProcessed(refId)) {
            System.out.println("✗ Duplicate transaction detected.");
            return;
        }
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            double senderBalance = accountDAO.getBalance(fromAccountId);
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
            double newBalance = senderBalance - total;
            accountDAO.updateBalance(fromAccountId, newBalance);
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), fromAccountId,
                    refId, "SEND_LOCAL", amount, fee, "SUCCESS",
                    "Sent to " + toPhone
            );
            processedRequestDAO.saveProcessedRequest(refId);
            conn.commit();
            System.out.println("✓ Sent " + amount + " RWF to " + toPhone);
            System.out.println("  Fee: " + fee + " RWF");
            System.out.println("  New balance: " + newBalance + " RWF");
        } catch (SQLException e) {
            rollback(conn);
            System.out.println("✗ Transfer failed: " + e.getMessage());
        } finally {
            resetAutoCommit(conn);
        }
    }

    public void sendMoneyInternational(String fromAccountId, String countryCode,
                                       String recipientNumber, double amount)
            throws InvalidAccountException, InsufficientBalanceException {
        if (!accountDAO.accountExists(fromAccountId)) {
            throw new InvalidAccountException("Sender account not found.");
        }
        if (!FeeCalculator.isSupportedCountry(countryCode)) {
            String reason = "Unsupported country code: +" + countryCode;
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), fromAccountId,
                    "INT-ERR-" + UUID.randomUUID(), "SEND_INTL",
                    0, 0, "FAILED", reason
            );
            System.out.println("✗ " + reason);
            return;
        }
        String refId = "INT-" + UUID.randomUUID();
        if (processedRequestDAO.isAlreadyProcessed(refId)) {
            System.out.println("✗ Duplicate transaction detected.");
            return;
        }
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            double senderBalance = accountDAO.getBalance(fromAccountId);
            double fee = FeeCalculator.getInternationalFee(amount);
            double total = amount + fee;
            if (senderBalance < total) {
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), fromAccountId,
                        "INT-ERR-" + UUID.randomUUID(), "SEND_INTL",
                        amount, fee, "FAILED",
                        "Insufficient balance. Have: " + senderBalance + " Need: " + total
                );
                conn.commit();
                throw new InsufficientBalanceException(
                        "Insufficient balance. Have: " + senderBalance + " RWF, Need: " + total + " RWF");
            }
            double newBalance = senderBalance - total;
            accountDAO.updateBalance(fromAccountId, newBalance);
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), fromAccountId,
                    refId, "SEND_INTL", amount, fee, "SUCCESS",
                    "Sent to +" + countryCode + " " + recipientNumber +
                            " (" + FeeCalculator.getCountryName(countryCode) + ")"
            );
            processedRequestDAO.saveProcessedRequest(refId);
            conn.commit();
            System.out.println("✓ Sent " + amount + " RWF to +" + countryCode + " " + recipientNumber);
            System.out.println("  Country: " + FeeCalculator.getCountryName(countryCode));
            System.out.println("  Fee: " + fee + " RWF");
            System.out.println("  New balance: " + newBalance + " RWF");
        } catch (SQLException e) {
            rollback(conn);
            System.out.println("✗ International transfer failed: " + e.getMessage());
        } finally {
            resetAutoCommit(conn);
        }
    }

    public void viewTransactionHistory(String accountId) {
        transactionDAO.printTransactionHistory(accountId);
    }

    public void viewFailedTransactions(String accountId) {
        transactionDAO.printFailedTransactions(accountId);
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
