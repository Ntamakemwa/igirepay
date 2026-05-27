package org.example.igirepay.lab1.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.example.igirepay.lab3.auth.PinHasher;

public abstract class Account {
    private String accountId;
    private String accountType;
    private double balance;
    private LocalDateTime createdAt;
    private String pin;
    private int failedPinAttempts;
    private boolean locked;

    protected List<String> transactionHistory;

    public Account(String accountId, String accountType, double balance, String pin) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.balance = balance;
        this.pin = pin;
        this.createdAt = LocalDateTime.now();
        this.failedPinAttempts = 0;
        this.locked = false;
        this.transactionHistory = new ArrayList<>();
    }

    public boolean validatePin(String inputPin) {
        if (locked) return false;
        boolean valid = PinHasher.isHashed(inputPin)
                ? inputPin.equals(this.pin)
                : PinHasher.verify(inputPin, this.pin);
        if (valid) {
            failedPinAttempts = 0;
            return true;
        } else {
            failedPinAttempts++;
            if (failedPinAttempts >= 3) locked = true;
            return false;
        }
    }

    public void changePin(String oldPin, String newPin) {
        if (validatePin(oldPin)) {
            this.pin = PinHasher.hash(newPin);
            System.out.println("PIN changed successfully.");
        } else {
            System.out.println("Incorrect PIN. Cannot change.");
        }
    }

    public List<String> getTransactionHistory() { return transactionHistory; }

    public abstract void deposit(double amount);
    public abstract void withdraw(double amount) throws Exception;
    public abstract void processTransaction(Transaction transaction) throws Exception;

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public int getFailedPinAttempts() { return failedPinAttempts; }
    public void setFailedPinAttempts(int failedPinAttempts) { this.failedPinAttempts = failedPinAttempts; }

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", locked=" + locked +
                ", createdAt=" + createdAt + '}';
    }
}