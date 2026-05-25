package org.example.igirepay.lab1.model;

import java.time.LocalDateTime;

public abstract class Account {
    private String accountId;
    private String accountType;
    private double balance;
    private LocalDateTime createdAt;
    private String pin;
    private int failedPinAttempts;
    private boolean locked;

    public Account(String accountId, String accountType, double balance, String pin) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.balance = balance;
        this.pin = pin;
        this.createdAt = LocalDateTime.now();
        this.failedPinAttempts = 0;
        this.locked = false;
    }

    public boolean validatePin(String inputPin) {
        if (locked) {
            System.out.println("Account is locked. Contact support.");
            return false;
        }
        if (this.pin.equals(inputPin)) {
            failedPinAttempts = 0;
            return true;
        } else {
            failedPinAttempts++;
            System.out.println("Wrong PIN. Attempts: " + failedPinAttempts + "/3");
            if (failedPinAttempts >= 3) {
                locked = true;
                System.out.println("Account locked after 3 failed attempts.");
            }
            return false;
        }
    }

    public void changePin(String oldPin, String newPin) {
        if (validatePin(oldPin)) {
            this.pin = newPin;
            System.out.println("PIN changed successfully.");
        } else {
            System.out.println("Incorrect PIN. Cannot change.");
        }
    }

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
    public int getFailedPinAttempts() { return failedPinAttempts; }

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
