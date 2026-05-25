package org.example.igirepay.model;

import org.example.igirepay.exception.InsufficientBalanceException;

public class WalletAccount extends Account {

    public WalletAccount(String accountId, double balance, String pin) {
        super(accountId, "WALLET", balance, pin);
    }

    @Override
    public void deposit(double amount) {
        setBalance(getBalance() + amount);
        System.out.println("Deposited " + amount + " to Wallet. New balance: " + getBalance());
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount > getBalance()) {
            throw new InsufficientBalanceException("Insufficient balance in Wallet.");
        }
        setBalance(getBalance() - amount);
        System.out.println("Withdrawn " + amount + " from Wallet. New balance: " + getBalance());
    }

    @Override
    public void processTransaction(Transaction transaction) throws InsufficientBalanceException {
        switch (transaction.getTransactionType()) {
            case "DEPOSIT" -> deposit(transaction.getAmount());
            case "WITHDRAWAL" -> withdraw(transaction.getAmount());
            default -> System.out.println("Unknown transaction type.");
        }
    }

    @Override
    public String toString() {
        return "WalletAccount{" + super.toString() + '}';
    }
}
