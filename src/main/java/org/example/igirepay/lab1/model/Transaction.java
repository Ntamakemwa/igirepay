package org.example.igirepay.lab1.model;

import java.time.LocalDateTime;

public class Transaction {
    private String transactionId;
    private String referenceId;
    private double amount;
    private String transactionType;
    private LocalDateTime timestamp;

    public Transaction(String transactionId, String referenceId,
                       double amount, String transactionType) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", timestamp=" + timestamp + '}';
    }
}
